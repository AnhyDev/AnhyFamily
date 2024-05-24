package ink.anh.family.info;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.messages.MessageChat;

public class FamilyInfoCommandHandler extends Sender {

	private AnhyFamily familyPlugin;

    public FamilyInfoCommandHandler(AnhyFamily familyPlugin) {
    	super(familyPlugin.getGlobalManager());
		this.familyPlugin = familyPlugin;
    }

    public boolean handleCommand(CommandSender sender, String[] args, boolean isInteractive) {
    	
		if (!(sender instanceof Player)) {
			isInteractive = false;
		}
		
    	PlayerFamily playerFamily = getTargetFamily(sender, args);
        if (playerFamily == null) return false;

        String familyInfo = translate(sender, new InfoGenerator(familyPlugin).generateFamilyInfo(playerFamily));

        if (isInteractive) {
        	String command  = "/family infos";
        	String playerName = (args.length > 1) ? args[1] : sender.getName();
        	command = (args.length > 1) ? (command + " " + playerName) : command;
            MessageForFormatting message = new MessageForFormatting(translate(sender, "family_info_component"), new String[] {playerName});
            MessageForFormatting hoverText = new MessageForFormatting(familyInfo, null);
            MessageChat.sendMessage(familyPlugin.getGlobalManager(), sender, message, hoverText, command, MessageType.NORMAL, false);
        } else {
            sendMessage(new MessageForFormatting(familyInfo, new String[] {}), MessageType.NORMAL, false, sender);
        }

        return true;
    }

    private PlayerFamily getTargetFamily(CommandSender sender, String[] args) {
        if (args.length > 1) {
            PlayerFamily playerFamily = FamilyUtils.getFamily(args[1]);
            if (playerFamily == null) {
                sendMessage(new MessageForFormatting("family_player_not_found_db", new String[] {}), MessageType.WARNING, sender);
                return null;
            }
            return playerFamily;
        } else if (sender instanceof Player) {
            return FamilyUtils.getFamily((Player) sender);
        } else {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return null;
        }
    }
}
