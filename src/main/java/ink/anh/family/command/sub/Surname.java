package ink.anh.family.command.sub;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Permissions;
import ink.anh.family.Sender;
import ink.anh.family.common.Family;
import ink.anh.family.common.FamilyDataHandler;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class Surname extends Sender {

	
	public Surname(AnhyFamily familiPlugin) {
		super(familiPlugin);
	}
	
	public boolean setSurname(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player)) {
			return false;
		}
		
        if (!sender.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", null), MessageType.WARNING, sender);
            return false;
        }
		
		Player player = (Player) sender;
		
		if (args.length <= 1) {
            sendMessage(new MessageForFormatting("family_err_command_format /family surname <Surname1[/Surname2]>", null), MessageType.WARNING, sender);
            return false;
        }
        
		// Об'єднання елементів масиву args, починаючи з індексу 1
		StringBuilder inputBuilder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
		    inputBuilder.append(args[i]);
		    if (i < args.length - 1) {
		        inputBuilder.append(" ");
		    }
		}
		
		String input = inputBuilder.toString();

		String[] newFamily = buildSurname(input);

        
        UUID uuid = player.getUniqueId();
        Family family = new FamilyDataHandler().getFamilyData(uuid);
        
        if (family == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", null), MessageType.WARNING, sender);
            return false;
        }
        
        if (family.getLastName() == null || family.getLastName()[0] == null) {
        	family.setLastName(newFamily);
        	FamilyUtils.saveFamily(family);

        	String myfam = String.join(" / ", newFamily);
        	sendMessage(new MessageForFormatting("family_surname_selected " + myfam, null), MessageType.IMPORTANT, sender);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_surname_already_exists", new String[] {String.join(" / ", family.getLastName())}), MessageType.WARNING, sender);
            return true;
        }
	}

	public boolean setSurnameFromConsole(CommandSender sender, String playerName, String newSurname) {
	    // Перевірка, чи команда виконується від імені консолі
	    if (sender instanceof Player) {
	        sendMessage(new MessageForFormatting("family_err_not_have_permission", null), MessageType.WARNING, sender);
	        return false;
	    }

	    Family family = FamilyUtils.getFamily(playerName);
	    if (family == null) {
	        sendMessage(new MessageForFormatting("family_player_not_found_db", null), MessageType.WARNING, sender);
	        return false;
	    }
	    

		String[] newFamily = buildSurname(newSurname);

	    // Зміна прізвища
	    family.setLastName(new String[]{newSurname, null});
	    FamilyUtils.saveFamily(family);
	    sendMessage(new MessageForFormatting("family_surname_forced_change", new String[] {playerName, String.join(" / ", newFamily)}), MessageType.IMPORTANT, sender);
	    return true;
	}

	private String[] buildSurname(String input) {
		String[] newFamily;
		int slashIndex = input.indexOf("/");

		if (slashIndex != -1) {
			newFamily = new String[2];
		    newFamily[0] = input.substring(0, slashIndex);
		    newFamily[1] = input.substring(slashIndex + 1);
		} else {
			newFamily = new String[1];
		    newFamily[0] = input;
		    newFamily[1] = null;
		}
		
		return newFamily;
	}
}
