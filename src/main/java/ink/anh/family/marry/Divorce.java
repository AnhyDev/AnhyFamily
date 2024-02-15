package ink.anh.family.marry;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Permissions;
import ink.anh.family.Sender;
import ink.anh.family.common.Family;
import ink.anh.family.common.FamilySeparation;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

public class Divorce extends Sender {

    public Divorce(AnhyFamily familiPlugin) {
        super(familiPlugin);
    }

	public boolean separate(CommandSender sender) {
        String sendername = sender.getName();
        Player player = null;
        
        if (sender instanceof Player) {
            player = (Player) sender;
            if (!player.hasPermission(Permissions.FAMILY_USER)) {
                sendMessage(new MessageForFormatting("family_err_not_have_permission", null), MessageType.WARNING, sender);
                return false;
            }
        } else if (sendername.equalsIgnoreCase("CONSOLE")) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", null), MessageType.WARNING, sender);
            return false;
        }
		
        String playerName = player.getName();
        
        Family family = FamilyUtils.getFamily(player);
        
        if (family == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {playerName}), MessageType.WARNING, sender);
            return false;
        }

        UUID spouseUUID = family.getSpouse();
        if (spouseUUID == null) {
            sendMessage(new MessageForFormatting("family_spouse_not_found", null), MessageType.WARNING, sender);
            return false;
        }
        
        if (new FamilySeparation(familiPlugin).separateSpouses(family)) {
        	Player spousePlayer = Bukkit.getPlayer(spouseUUID);
            sendMessage(new MessageForFormatting("family_separation_spouse_successful", null), MessageType.IMPORTANT, player, spousePlayer);
            return true;
        }

        sendMessage(new MessageForFormatting("family_error_generic", null), MessageType.WARNING, sender);
        return false;
	}
}
