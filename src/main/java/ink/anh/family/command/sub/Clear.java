package ink.anh.family.command.sub;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Permissions;
import ink.anh.family.common.Family;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class Clear extends Sender {
	
	public Clear(AnhyFamily familiPlugin) {
		super(familiPlugin.getGlobalManager());
	}
	
	public boolean exeClearFamily(CommandSender sender, String[] args) {
		
		String sendername = sender.getName();
		Player player = null;
		
		if (sender instanceof Player) {
			player = (Player) sender;
			if (!player.hasPermission(Permissions.FAMILY_ADMIN)) {
	            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, sender);
	            return false;
			}
		} else if(!sendername.equalsIgnoreCase("CONSOLE") && player == null) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, sender);
            return false;
		}
		
        if (args.length <= 1) {
            sendMessage(new MessageForFormatting("family_err_command_format  /family clear <player1>", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        String name1 = args[1];
        Family family1 = FamilyUtils.getFamily(name1);
        
        if (family1 == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        Set<Family> modifiedFamilies = FamilyUtils.clearRelatives(family1);
        Set<Player> playersSet = new HashSet<>();

        for (Family family : modifiedFamilies) {
            UUID playerId = family.getRoot();
            Player pl = Bukkit.getPlayer(playerId);
            if (pl != null && pl.isOnline()) {
                playersSet.add(pl);
            }
        }

        Player[] players = playersSet.toArray(new Player[0]);
        
        if (players.length > 0) {
        	sendMessage(new MessageForFormatting("family_clear_relative_success", new String[] {}), MessageType.WARNING, players);
    		return true;
        }
    	sendMessage(new MessageForFormatting("family_clear_relative_missing", new String[] {}), MessageType.WARNING, players);
		return false;
	}
}
