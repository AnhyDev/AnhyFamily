package ink.anh.family.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.home.FamilyHomeSubCommand;

public class FamilyHomeCommand extends Sender implements CommandExecutor {
	
    private AnhyFamily familyPlugin;

    public FamilyHomeCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

    	Player player = null;
    	
        if (sender instanceof Player ) {
        	
        	player = (Player) sender;

            if (player != null) {
                return new FamilyHomeSubCommand(familyPlugin).onCommand(player, args);
            }
        }
        
        sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
        return true;
    }
}
