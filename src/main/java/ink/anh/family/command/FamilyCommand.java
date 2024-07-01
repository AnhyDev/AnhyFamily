package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.FirstName;
import ink.anh.family.fplayer.Surname;
import ink.anh.family.fplayer.info.FamilyInfo;
import ink.anh.family.fplayer.info.FamilyProfileHandler;
import ink.anh.family.fplayer.info.FamilyTreeHandler;
import ink.anh.family.marriage.Divorce;
import ink.anh.family.separate.Separation;
import ink.anh.api.messages.MessageForFormatting;

public class FamilyCommand extends Sender implements CommandExecutor {

	private AnhyFamily familyPlugin;
	
	public FamilyCommand(AnhyFamily familyPlugin) {
		super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    CompletableFuture.runAsync(() -> {
            try {
    	        if (args.length > 0) {
    	            switch (args[0].toLowerCase()) {
                    	case "firstname":
                    		new FirstName().setFirstName(sender, args);
                    		break;
    	                case "surname":
    	                    new Surname().setSurname(sender, args);
    	                    break;
    	                case "divorce":
    	                    new Divorce(familyPlugin).separate(sender);
    	                    break;
    	                case "separate":
    	                    new Separation(familyPlugin).separate(sender, args);
    	                    break;
    	                case "info":
    	                    new FamilyInfo().handleInfoCommand(sender, args, true);
    	                    break;
    	                case "profile":
    	                    new FamilyProfileHandler().handleCommand(sender, args);
    	                    break;
    	                case "tree":
    	                    new FamilyTreeHandler().handleTreeCommand(sender, args);
    	                    break;
    	                default:
    	                    sendMessage(new MessageForFormatting("family_err_command_format /family <param>", new String[] {}), MessageType.WARNING, sender);
    	            }
    	        }
            } catch (Exception e) {
                e.printStackTrace();
            }
	    });
	    return true;
	}
}
