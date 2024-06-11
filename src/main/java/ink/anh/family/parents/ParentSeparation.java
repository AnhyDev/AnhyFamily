package ink.anh.family.parents;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.FamilySeparationEvent;
import ink.anh.family.events.FamilySeparationReason;
import ink.anh.family.fplayer.FamilySeparation;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.marriage.FamilyHandler;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.util.FamilySeparationUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.utils.SyncExecutor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class ParentSeparation extends Sender {

    private AnhyFamily familyPlugin;

    public ParentSeparation(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    public boolean separate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format /family separate parent <player>", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        PlayerFamily playerFamily = FamilyUtils.getFamily(playerUUID);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_info_family_not_found", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        String targetPlayerName = args[2];
        PlayerFamily targetFamily = FamilyUtils.getFamily(targetPlayerName);

        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_no_family_found_for_target", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        UUID targetUUID = targetFamily.getRoot();
        Player targetPlayer = Bukkit.getPlayer(targetUUID);

        // Виклик обробки події
        ActionInitiator initiator = ActionInitiator.PLAYER_SELF;
        FamilyDetails familyDetails = FamilyDetailsGet.getRootFamilyDetails(playerFamily);
        Set<PlayerFamily> modifiedFamilies = FamilySeparationUtils.getRelatives(playerFamily, FamilySeparationReason.DISOWN_PARENT);
        
        SyncExecutor.runSync(() -> {
            FamilySeparationEvent event = new FamilySeparationEvent(playerFamily, familyDetails, modifiedFamilies, FamilySeparationReason.DISOWN_PARENT, initiator);
            handleParentSeparation(event, playerFamily, targetFamily, sender, targetPlayer);
        });

        return true;
    }

    private void handleParentSeparation(FamilySeparationEvent event, PlayerFamily senderFamily, PlayerFamily targetFamily, CommandSender sender, Player targetPlayer) {
        final MessageType[] messageType = {MessageType.IMPORTANT, MessageType.WARNING};
        try {
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                	
                    FamilySeparation familySeparation = new FamilySeparation(familyPlugin);
                    boolean success;

                    UUID playerUUID = senderFamily.getRoot();
                    UUID targetUUID = targetFamily.getRoot();

                    if (playerUUID.equals(targetFamily.getFather()) || playerUUID.equals(targetFamily.getMother())) {
                        // Якщо виконавець команди є одним із батьків цільового гравця
                        success = familySeparation.separateChildFromParent(targetUUID, playerUUID);
                    } else {
                        sendMessage(new MessageForFormatting("family_err_no_parent_child_relationship", new String[] {}), MessageType.WARNING, sender);
                        return;
                    }

                    MessageForFormatting messageTrue = new MessageForFormatting("family_success_separation_completed", new String[] {});
                    MessageForFormatting messageFalse = new MessageForFormatting("family_err_separation_failed", new String[] {});
                    CommandSender[] senders = {sender, targetPlayer};

                    if (success) {
                        FamilyHandler.removeCrossFamilyRelations(senderFamily, event.getModifiedFamilies(), true, true);
                        messageType[0] = MessageType.IMPORTANT;
                        sendMessage(messageTrue, messageType[0], senders);
                    } else {
                        sendMessage(messageFalse, messageType[1], senders);
                    }
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[] {}), MessageType.WARNING, sender);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in handleParentSeparation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
