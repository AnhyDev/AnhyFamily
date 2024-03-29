package ink.anh.family.info;

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
            sendMessage(new MessageForFormatting("family_err_command_format /family surname <Surname male version[/Surname female version]>", null), MessageType.WARNING, sender);
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
		if (newFamily == null) {
	        sendMessage(new MessageForFormatting("family_surname_build_failed", null), MessageType.WARNING, sender);
	        return false;
		}

        
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
        	sendMessage(new MessageForFormatting("family_surname_selected", new String[] {myfam}), MessageType.IMPORTANT, sender);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_surname_already_exists", new String[] {String.join(" / ", family.getLastName())}), MessageType.WARNING, sender);
            return true;
        }
	}

	public boolean setSurnameFromConsole(CommandSender sender, String[] args) {
		
	    if (args.length < 3) {
	        sendMessage(new MessageForFormatting("family_err_command_format /family setsurname <PlayerName> <Surname male version[/Surname female version]>", null), MessageType.WARNING, sender);
	        return false;
	    }
	    
	    String playerName = args[1];

	    StringBuilder surnameBuilder = new StringBuilder();
	    for (int i = 2; i < args.length; i++) {
	        surnameBuilder.append(args[i]);
	        if (i < args.length - 1) {
	            surnameBuilder.append(" ");
	        }
	    }
	    
	    String stringSurname = surnameBuilder.toString();
		
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
	    

		String[] newSurname = buildSurname(stringSurname);
		if (newSurname == null) {
	        sendMessage(new MessageForFormatting("family_surname_build_failed", null), MessageType.WARNING, sender);
	        return false;
		}

	    // Зміна прізвища
	    family.setLastName(newSurname);
	    family.setOldLastName(null);
	    FamilyUtils.saveFamily(family);
	    sendMessage(new MessageForFormatting("family_surname_forced_change", new String[] {playerName, String.join(" / ", newSurname)}), MessageType.IMPORTANT, sender);
	    return true;
	}

	private String[] buildSurname(String input) {
	    String processedInput = input.replaceAll("\\s+", " ");
	    String[] newFamily;
	    int slashIndex = processedInput.indexOf("/");

	    if (slashIndex != -1) {
	        newFamily = new String[2];
	        newFamily[0] = processedInput.substring(0, slashIndex).trim();
	        newFamily[1] = processedInput.substring(slashIndex + 1).trim();
	        if (!checkMaxLengthSurname(newFamily)) {
	        	return null;
	        }
	    } else {
	        newFamily = new String[1];
	        newFamily[0] = processedInput.trim();
	        if (!checkMaxLengthSurname(newFamily)) {
	        	return null;
	        }
	    }
	    
	    return newFamily;
	}
	
	private boolean checkMaxLengthSurname(String[] newFamily) {
		if (newFamily == null || newFamily.length == 0) return false;
		final int MAX_LENGTH = 21;
		for (String familyString : newFamily) {
		    if (familyString.length() > MAX_LENGTH) {
		    	return false;
		    }
		}
		return true;
	}
}
