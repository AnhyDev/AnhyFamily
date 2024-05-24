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

public class FamilyTreeCommandHandler extends Sender {

	private AnhyFamily familyPlugin;

    public FamilyTreeCommandHandler(AnhyFamily familyPlugin) {
    	super(familyPlugin.getGlobalManager());
		this.familyPlugin = familyPlugin;
    }

    public boolean handleTreeCommand(CommandSender sender, String[] args, boolean isInteractive) {
    	
		if (!(sender instanceof Player)) {
			isInteractive = false;
		}
		
    	PlayerFamily playerFamily;
        if (args.length > 1) {
        	playerFamily = FamilyUtils.getFamily(args[1]);
        } else {
            if (!(sender instanceof Player)) {
                sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
                return false;
            }
            playerFamily = FamilyUtils.getFamily((Player) sender);
        }
        
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        FamilyTree familyTree = new FamilyTree(playerFamily);
        String treeInfo = familyTree.buildFamilyTreeString();

        if (isInteractive) {
        	String command  = "/family trees";
        	String playerName = (args.length > 1) ? args[1] : sender.getName();
        	command = (args.length > 1) ? (command + " " + playerName) : command;
            MessageForFormatting message = new MessageForFormatting("family_tree_component", new String[] {playerName});
            MessageForFormatting hoverText = new MessageForFormatting(treeInfo, new String[] {});
            MessageChat.sendMessage(familyPlugin.getGlobalManager(), sender, message, hoverText, command, MessageType.NORMAL, false);
        } else {
            sendMessage(new MessageForFormatting(treeInfo, new String[] {}), MessageType.NORMAL, false, sender);
        }

        return true;
    }
}
