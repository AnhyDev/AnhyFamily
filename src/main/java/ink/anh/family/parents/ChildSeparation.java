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
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsHandler;
import ink.anh.family.util.FamilySeparationUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.utils.SyncExecutor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class ChildSeparation extends Sender {

	private AnhyFamily familyPlugin;

    public ChildSeparation(AnhyFamily familyPlugin) {
    	super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
    }

    public boolean separate(CommandSender sender, String[] args) {
        // Перевірка правил використання команди
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format /family separate child <player>", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID senderUUID = player.getUniqueId();
        PlayerFamily senderFamily = FamilyUtils.getFamily(senderUUID);

        if (senderFamily == null) {
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
        FamilyDetails familyDetails = FamilyDetailsGet.getRootFamilyDetails(senderFamily);
        Set<PlayerFamily> modifiedFamilies = FamilySeparationUtils.getRelatives(senderFamily, FamilySeparationReason.DISOWN_CHILD);
        
        SyncExecutor.runSync(() -> {
            FamilySeparationEvent event = new FamilySeparationEvent(senderFamily, familyDetails, modifiedFamilies, FamilySeparationReason.DISOWN_CHILD, initiator);
            handleChildSeparation(event, senderFamily, targetFamily, sender, targetPlayer);
        });

        return true;
    }

    private void handleChildSeparation(FamilySeparationEvent event, PlayerFamily senderFamily, PlayerFamily targetFamily, CommandSender sender, Player targetPlayer) {
        final MessageType[] messageType = {MessageType.IMPORTANT, MessageType.WARNING};
        try {
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                	
                    FamilySeparation familySeparation = new FamilySeparation(familyPlugin);
                    boolean success;

                    UUID senderUUID = senderFamily.getRoot();
                    UUID targetUUID = targetFamily.getRoot();

                    if (senderUUID.equals(targetFamily.getFather()) || senderUUID.equals(targetFamily.getMother())) {
                        // Якщо виконавець команди є одним із батьків
                        success = familySeparation.separateParentFromChild(senderUUID, targetUUID);
                    } else if (targetUUID.equals(senderFamily.getFather()) || targetUUID.equals(senderFamily.getMother())) {
                        // Якщо ціль команди є одним із батьків виконавця
                        success = familySeparation.separateChildFromParent(senderUUID, targetUUID);
                    } else {
                        sendMessage(new MessageForFormatting("family_err_no_parent_child_relationship", new String[] {}), MessageType.WARNING, sender);
                        return;
                    }

                    MessageForFormatting messageTrue = new MessageForFormatting("family_success_separation_completed", new String[] {});
                    MessageForFormatting messageFalse = new MessageForFormatting("family_err_separation_failed", new String[] {});
                    CommandSender[] senders = {sender, targetPlayer};

                    if (success) {
                        FamilyDetailsHandler.removeCrossFamilyRelations(senderFamily, event.getModifiedFamilies(), true, true);
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
            Bukkit.getLogger().severe("Exception in handleChildSeparation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
