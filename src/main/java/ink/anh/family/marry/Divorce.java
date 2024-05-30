package ink.anh.family.marry;

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
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;

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
                sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, sender);
                return false;
            }
        } else if (sendername.equalsIgnoreCase("CONSOLE")) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }
		
        String playerName = player.getName();
        
        PlayerFamily playerFamily = FamilyUtils.getFamily(player);
        
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_full", new String[] {playerName}), MessageType.WARNING, sender);
            return false;
        }

        UUID spouseUUID = playerFamily.getSpouse();
        if (spouseUUID == null) {
            sendMessage(new MessageForFormatting("family_spouse_not_found", new String[] {}), MessageType.WARNING, sender);
            return false;
        }
        
        if (new FamilySeparation(familyPlugin).separateSpouses(playerFamily)) {
        	Player spousePlayer = Bukkit.getPlayer(spouseUUID);
            sendMessage(new MessageForFormatting("family_separation_spouse_successful", new String[] {}), MessageType.IMPORTANT, player, spousePlayer);
            return true;
        }

        sendMessage(new MessageForFormatting("family_error_generic", new String[] {}), MessageType.WARNING, sender);
        return false;
	}
}
