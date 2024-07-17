package ink.anh.family.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.FirstName;
import ink.anh.family.fplayer.PriestNameChanger;
import ink.anh.family.fplayer.Surname;
import ink.anh.family.fplayer.info.FamilyInfo;
import ink.anh.family.fplayer.info.FamilyProfileHandler;
import ink.anh.family.fplayer.info.FamilyTreeHandler;
import ink.anh.family.separate.Divorce;
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
                    	case "sugges":
                    		sugges(sender, args);
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
    	                    new FamilyTreeHandler().handleCommand(sender, args);
    	                    break;
    	                default:
    	                    sendMessage(new MessageForFormatting("family_err_command_format ", new String[] {"/family <param>"}), MessageType.WARNING, sender);
    	            }
    	        }
            } catch (Exception e) {
                e.printStackTrace();
            }
	    });
	    return true;
	}
	
	private void sugges(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return;
        }
        
	    boolean isFirstOrSurname = args.length > 3 && 
	                               (args[1].equalsIgnoreCase("firstname") || args[1].equalsIgnoreCase("surname"));
	    boolean isAcceptOrRefuse = args.length == 2 && 
	                               (args[1].equalsIgnoreCase("accept") || args[1].equalsIgnoreCase("refuse"));
	    
	    if (isFirstOrSurname || isAcceptOrRefuse) {
	        if (PriestNameChanger.getInstance(familyPlugin).sugges(sender, args)) {
	        	return;
	        }
	    }
	    
	    sendMessage(new MessageForFormatting("family_err_command_format ", 
	        new String[] {"\n/family sugges [firstname|surname] <PlayerName> <param> \n/family sugges [accept|refuse]"}), 
	        MessageType.WARNING, sender);
	}
}
