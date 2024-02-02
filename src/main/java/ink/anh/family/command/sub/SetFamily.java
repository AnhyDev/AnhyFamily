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
import ink.anh.family.gender.Gender;
import ink.anh.family.gender.GenderUtils;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class SetFamily extends Sender {

	
	public SetFamily(AnhyFamily familiPlugin) {
		super(familiPlugin);
	}
	
	public boolean exeSetFamily(CommandSender sender, String[] args) {
		
		if (!(sender instanceof Player)) {
			return false;
		}
		
        if (!sender.hasPermission(Permissions.FAMILY_USER)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", null), MessageType.WARNING, sender);
            return false;
        }
		
		Player player = (Player) sender;
		
		if (args.length <= 1) {
            sendMessage(new MessageForFormatting("family_err_command_format /family surname <myfamily>", null), MessageType.WARNING, sender);
            return false;
        }
        
        String input = args[1];
        String[] splitParts = input.split("/");
        String[] myfamily = new String[2];
        if (splitParts.length >= 2) {
        	myfamily[0] = splitParts[0];
        	myfamily[1] = splitParts[1];
        } else {
        	myfamily[0] = input;
        	myfamily[1] = input;
        }
        
        UUID uuid = player.getUniqueId();
        Family family = new FamilyDataHandler().getFamilyData(uuid);
        
        if (family == null) {
            sendMessage(new MessageForFormatting("family_setfamily_player_not_found", null), MessageType.WARNING, sender);
            return false;
        }
        
        if (family.getLastName()[0] == null) {
        	family.setLastName(myfamily);
        	FamilyUtils.saveFamily(family);
            Gender gender = GenderUtils.getGender(uuid);
        	String myfam = gender == Gender.MALE ? myfamily[0] : myfamily[1];
        	sendMessage(new MessageForFormatting("family_setfamily_family_selected" + myfam, null), MessageType.IMPORTANT, sender);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_setfamily_family_already_exists1" + family.getLastName() + "family_setfamily_family_already_exists2", null), MessageType.WARNING, sender);
            return true;
        }
	}
}
