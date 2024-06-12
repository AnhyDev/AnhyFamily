package ink.anh.family.marriage;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.fplayer.FamilySeparation;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsService;
import ink.anh.family.util.FamilySeparationUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.FamilySeparationEvent;
import ink.anh.family.events.FamilySeparationReason;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.utils.SyncExecutor;

public class Divorce extends Sender {

    private AnhyFamily familyPlugin;

    public Divorce(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    public boolean separate(CommandSender sender) {
        String sendername = sender.getName();
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
            if (!player.hasPermission(Permissions.FAMILY_USER)) {
                sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[]{}), MessageType.WARNING, sender);
                return false;
            }
        } else if (sendername.equalsIgnoreCase("CONSOLE")) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        String playerName = player.getName();
        PlayerFamily playerFamily = FamilyUtils.getFamily(player);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[]{playerName}), MessageType.WARNING, sender);
            return false;
        }

        UUID spouseUUID = playerFamily.getSpouse();
        if (spouseUUID == null) {
            sendMessage(new MessageForFormatting("family_spouse_not_found", new String[]{}), MessageType.WARNING, sender);
            return false;
        }

        PlayerFamily spouseFamily = FamilyUtils.getFamily(Bukkit.getPlayer(spouseUUID));
        FamilyDetails familyDetails = FamilyDetailsGet.getRootFamilyDetails(playerFamily);
        ActionInitiator initiatorAction = ActionInitiator.PLAYER_SELF;

        MessageForFormatting messageTrue = new MessageForFormatting("family_separation_spouse_successful", new String[]{});
        MessageForFormatting messageFalse = new MessageForFormatting("family_error_generic", new String[]{});
        CommandSender[] senders = {sender, Bukkit.getPlayer(spouseUUID)};

        Set<PlayerFamily> modifiedFamilies = FamilySeparationUtils.getRelatives(playerFamily, FamilySeparationReason.DIVORCE);
        
        SyncExecutor.runSync(() -> {
    	    FamilySeparationEvent event = new FamilySeparationEvent(playerFamily, FamilyDetailsGet.getRootFamilyDetails(playerFamily), modifiedFamilies,
    	    		FamilySeparationReason.DIVORCE, initiatorAction);
        	handleDivorce(event, playerFamily, spouseFamily, familyDetails, initiatorAction, senders, messageTrue, messageFalse);
        	});
        return true;
    }

    private void handleDivorce(FamilySeparationEvent event, PlayerFamily initiatorFamily, PlayerFamily spouseFamily, FamilyDetails familyDetails,
            ActionInitiator initiatorAction, CommandSender[] senders, MessageForFormatting messageTrue, MessageForFormatting messageFalse) {
        final MessageType[] messageType = {MessageType.WARNING};
        try {
            Bukkit.getPluginManager().callEvent(event);

            Set<PlayerFamily> modifiedFamilies = event.getModifiedFamilies();

            if (!event.isCancelled()) {
                SyncExecutor.runAsync(() -> {
                    FamilyDetailsService.removeCrossFamilyRelations(initiatorFamily, modifiedFamilies, false, false);
                    FamilyDetailsService.removeCrossFamilyRelations(spouseFamily, modifiedFamilies, false, false);

                    FamilyDetailsService.handleDivorce(spouseFamily);

                    FamilySeparation utilsDivorce = new FamilySeparation(familyPlugin);

                    if (utilsDivorce.separateSpouses(initiatorFamily)) {
                        messageType[0] = MessageType.IMPORTANT;
                        sendMessage(messageTrue, messageType[0], senders);
                    } else {
                        sendMessage(messageFalse, messageType[0], senders);
                    }
                });
            } else {
                sendMessage(new MessageForFormatting("family_err_event_is_canceled", new String[]{}), MessageType.WARNING, senders);
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe("Exception in handleDivorce: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
