package ink.anh.family.separate;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.FamilyDetailsService;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.marriage.Divorce;
import ink.anh.family.parents.ChildSeparation;
import ink.anh.family.parents.ParentSeparation;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class Separation extends Sender {

	private AnhyFamily familyPlugin;

    public Separation(AnhyFamily familyPlugin) {
    	super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
    }

	public boolean separate(CommandSender sender, String[] args) {
        
        if (args.length < 2 || args[1].equalsIgnoreCase("spouse")) {
            return new Divorce(familyPlugin).separate(sender);
        } else if (args.length > 2 && args[1].equalsIgnoreCase("child")) {
        	return new ChildSeparation(familyPlugin).separate(sender, args);
        } else if (args.length > 2 && args[1].equalsIgnoreCase("parent")) {
        	return new ParentSeparation(familyPlugin).separate(sender, args);
        } else if (args.length > 2) {
        	return separateNickName(sender, args);
        }
        
        sendMessage(new MessageForFormatting("family_error_command_line", new String[] {}), MessageType.WARNING, sender);
        return false;
	}
	
	public boolean separateNickName(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }
		
        Player player = (Player) sender;
        
		PlayerFamily senderFamily = FamilyUtils.getFamily(player);
		PlayerFamily targetFamily = FamilyUtils.getFamily(args[1]);
		
        if (targetFamily == null || senderFamily == null) {
            sendMessage(new MessageForFormatting("family_info_family_not_found", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        UUID playerUUID = senderFamily.getRoot();
        UUID targetUUID = targetFamily.getRoot();
        
        if (senderFamily.getSpouse().equals(targetUUID)) {
        	return new Divorce(familyPlugin).separate(sender);
        } else if (senderFamily.getChildren().contains(targetUUID)) {
        	return new ChildSeparation(familyPlugin).separate(player, senderFamily, targetFamily);
        } else if (targetFamily.getChildren().contains(playerUUID)) {
        	return new ParentSeparation(familyPlugin).separate(player, senderFamily, targetFamily);
        }
        FamilyDetailsService.removeCrossFamilyRelations(senderFamily, new HashSet<>(Collections.singleton(targetFamily)), true, true);
        
		return false;
	}
}
