package ink.anh.family.fplayer.info;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.OtherComponentBuilder;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.api.messages.MessageComponents;

public class FamilyInfo extends Sender {

    public FamilyInfo() {
    	super(GlobalManager.getInstance());
    }

    public boolean handleInfoCommand(CommandSender sender, String[] args, boolean isInteractive) {
    	
    	if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return false;
        }
    	
    	Player player = (Player) sender;
		
    	PlayerFamily playerFamily = null;
    	
    	String targetName = null;
    	
        if (args.length > 1) {
        	targetName = args[1];
        	
    		playerFamily = FamilyUtils.getFamily(targetName);
        }
        
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_player_not_found_db", new String[] {}), MessageType.WARNING, sender);
            return false;
        }

        String familyInfo = new ProfileStringGenerator().generateFamilyInfo(playerFamily);
        String treeInfo = new TreeStringGenerator(playerFamily).buildFamilyTreeString();
        
        MessageComponents messageComponents = OtherComponentBuilder.infoDoubleComponent("family_tree_status", "/family profile [PlayerName]", "/family tree [PlayerName]", "family_info_component", "family_tree_component",
        		familyInfo + "\n family_print_component", treeInfo + "\n family_print_component", player);

        Messenger.sendMessage(libraryManager.getPlugin(), player, messageComponents, "MessageComponents");

        return true;
    }
}
