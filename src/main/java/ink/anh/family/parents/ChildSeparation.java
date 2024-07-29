package ink.anh.family.parents;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.FamilySeparationEvent;
import ink.anh.family.events.FamilySeparationReason;
import ink.anh.family.fplayer.FamilySeparationUtils;
import ink.anh.family.fplayer.FamilyUtils;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsService;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.utils.SyncExecutor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChildSeparation extends Sender {

	public ChildSeparation(AnhyFamily familyPlugin) {
    	super(GlobalManager.getInstance());
    }

    public boolean separate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        Player player = (Player) sender;
        UUID senderUUID = player.getUniqueId();
        PlayerFamily senderFamily = FamilyUtils.getFamily(senderUUID);

        if (senderFamily == null) {
            sendMessage(new MessageForFormatting("family_info_family_not_found", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        if (senderFamily.getChildren() == null || senderFamily.getChildren().isEmpty()) {
            sendMessage(new MessageForFormatting("family_err_dont_have_children", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        return separate(player, senderFamily);
    }

    public boolean separate(Player player, PlayerFamily senderFamily, PlayerFamily targetFamily) {
        final Set<PlayerFamily> modifiedFamilies;

        if (targetFamily != null) {
            modifiedFamilies = new HashSet<>();
            modifiedFamilies.add(targetFamily);
        } else {
            modifiedFamilies = FamilySeparationUtils.getRelatives(senderFamily, FamilySeparationReason.DISOWN_CHILD);
        }
        
        ActionInitiator initiator = ActionInitiator.PLAYER_SELF;
        FamilyDetails familyDetails = FamilyDetailsGet.getRootFamilyDetails(senderFamily);

        SyncExecutor.runSync(() -> {
            FamilySeparationEvent event = new FamilySeparationEvent(senderFamily, familyDetails, modifiedFamilies, FamilySeparationReason.DISOWN_CHILD, initiator);
            handleChildSeparation(event, senderFamily, player, targetFamily == null);
        });

        return true;
    }

    public boolean separate(Player player, PlayerFamily senderFamily) {
        return separate(player, senderFamily, null);
    }

    private void handleChildSeparation(FamilySeparationEvent event, PlayerFamily senderFamily, Player player, boolean everyone) {
        try {
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                    Set<Player> uniqueMembers = new HashSet<>();
                    boolean success = false;

                    boolean resultRemoveChildren = false;
                    boolean resultRemoveParents = false;

                    for (PlayerFamily family : event.getModifiedFamilies()) {
                        Player member = Bukkit.getPlayer(family.getRoot());
                        if (member != null) {
                            uniqueMembers.add(member);
                        }

                        if (!everyone) {
                            resultRemoveChildren |= FamilySeparationUtils.removeOneChildren(senderFamily, family, true);
                        }

                        resultRemoveParents |= FamilySeparationUtils.removeOneParents(senderFamily, family, true) != null;
                    }

                    if (everyone) {
                        senderFamily.setChildren(new HashSet<>());
                        PlayerFamilyDBService.savePlayerFamily(senderFamily, FamilyPlayerField.CHILDREN);
                        resultRemoveChildren = true;
                    }

                    success = resultRemoveParents || resultRemoveChildren;

                    uniqueMembers.add(player);
                    Player[] members = uniqueMembers.toArray(new Player[0]);

                    if (success) {
                        FamilyDetailsService.removeCrossFamilyRelations(senderFamily, event.getModifiedFamilies(), true, true);
                        sendMessage(new MessageForFormatting("family_success_separation_completed", new String[] {}), MessageType.IMPORTANT, members);
                    } else {
                        sendMessage(new MessageForFormatting("family_err_separation_failed", new String[] {}), MessageType.WARNING, members);
                    }
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[] {}), MessageType.WARNING, player);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in handleChildSeparation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
