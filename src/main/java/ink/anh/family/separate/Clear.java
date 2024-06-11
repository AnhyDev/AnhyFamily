package ink.anh.family.separate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.events.ActionInitiator;
import ink.anh.family.events.FamilySeparationEvent;
import ink.anh.family.events.FamilySeparationReason;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.marriage.FamilyHandler;
import ink.anh.family.util.FamilySeparationUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class Clear extends Sender {
	
	public Clear(AnhyFamily familiPlugin) {
		super(GlobalManager.getInstance());
	}
	
	public void exeClearFamily(CommandSender sender, String[] args) {
		
		String sendername = sender.getName();
		Player player = null;
		
		if (sender instanceof Player) {
			player = (Player) sender;
			if (!player.hasPermission(Permissions.FAMILY_ADMIN)) {
	            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, sender);
	            return;
			}
		} else if(!sendername.equalsIgnoreCase("CONSOLE") && player == null) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, sender);
            return;
		}
		
        if (args.length <= 1) {
            sendMessage(new MessageForFormatting("family_err_command_format  /family clear <player1>", new String[] {}), MessageType.WARNING, sender);
            return;
        }

        String name1 = args[1];
        PlayerFamily family1 = FamilyUtils.getFamily(name1);
        
        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[] {}), MessageType.WARNING, sender);
            return;
        }

        ActionInitiator initiator = player != null ? ActionInitiator.PLAYER_WITH_PERMISSION : ActionInitiator.CONSOLE;
        Set<PlayerFamily> modifiedFamilies = FamilySeparationUtils.getRelatives(family1, FamilySeparationReason.FULL_SEPARATION);

        SyncExecutor.runSync(() -> {
    	    FamilySeparationEvent event = new FamilySeparationEvent(family1, FamilyDetailsGet.getRootFamilyDetails(family1), modifiedFamilies, FamilySeparationReason.FULL_SEPARATION, initiator);
    	    handleFamilySeparation(event, family1, initiator, sender);
        });
        
		return;
	}

    private void handleFamilySeparation(FamilySeparationEvent event, PlayerFamily playerFamily, ActionInitiator initiator, CommandSender sender) {
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            SyncExecutor.runAsync(() -> {

                // Викликаємо метод для повного розриву зв'язків, передаючи модифіковані сім'ї
            	separateAllRelations(playerFamily, event.getModifiedFamilies());

                Set<Player> playersSet = new HashSet<>();
                Set<PlayerFamily> modifiedFamilies = FamilySeparationUtils.clearRelatives(playerFamily, FamilySeparationReason.FULL_SEPARATION);

                for (PlayerFamily modifieFamily : modifiedFamilies) {
                    UUID playerId = modifieFamily.getRoot();
                    Player pl = Bukkit.getPlayer(playerId);
                    if (pl != null && pl.isOnline()) {
                        playersSet.add(pl);
                    }
                }

                Player[] players = playersSet.toArray(new Player[0]);

                if (players.length > 0) {
                    sendMessage(new MessageForFormatting("family_clear_relative_success", new String[] {}), MessageType.IMPORTANT, players);
                    return;
                }
                sendMessage(new MessageForFormatting("family_clear_relative_missing", new String[] {}), MessageType.WARNING, players);
            });
        }
    }

    private static void separateAllRelations(PlayerFamily playerFamily, Set<PlayerFamily> modifiedFamilies) {
        // Видалення всіх родичів з сім'ї
        FamilyHandler.removeCrossFamilyRelations(playerFamily, modifiedFamilies, true, true);
    }
}
