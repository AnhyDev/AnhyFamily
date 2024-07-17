package ink.anh.family.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.TableRegistry;
import ink.anh.family.fplayer.FirstName;
import ink.anh.family.fplayer.Surname;
import ink.anh.family.fplayer.gender.GenderCommandHandler;
import ink.anh.family.marriage.MarriageManager;
import ink.anh.family.parents.Adopt;
import ink.anh.family.parents.ParentManager;
import ink.anh.family.separate.ClearAllRelatives;

public class AnhyFamilyCommand extends Sender implements CommandExecutor {

	private AnhyFamily familyPlugin;
	
	public AnhyFamilyCommand(AnhyFamily familyPlugin) {
		super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    CompletableFuture.runAsync(() -> {
            try {
    	        if (args.length > 0) {
    	            switch (args[0].toLowerCase()) {
	                	case "reload":
	                		reload(sender);
	                		break;
    	                case "parentinfo":
    	                    infoParentElement(sender);
    	                    break;
    	                case "marriageinfo":
    	                    infoMarryElement(sender);
    	                    break;
                        case "forcefirstname":
                            new FirstName().setFirstNameFromConsole(sender, args);
                            break;
    	                case "forcesurname":
    	                    new Surname().setSurnameFromConsole(sender, args);
    	                    break;
    	                case "clearfamily":
    	                    new ClearAllRelatives(familyPlugin).exeClearFamily(sender, args);
    	                    break;
    	                case "forceadopt":
    	                    new Adopt(familyPlugin).forceAdopt(sender, args);
    	                    break;
    	                case "forcegender":
    	                    if (args.length >= 3) {
    	                    	new GenderCommandHandler(familyPlugin).handleForceSetGender(sender, args[1], args[2]);
    	                    }
    	                    break;
                        case "genderreset":
                            if (args.length >= 2) {
                            	new GenderCommandHandler(familyPlugin).handleResetGender(sender, args[1]);
                            }
                            break;
    	                default:
    	                    sendMessage(new MessageForFormatting(
    	                    		"family_err_command_format /anhyfamily <reload|parentinfo|marriageinfo|forcesurname|clearfamily|forceadopt|forcegender|genderreset>",
    	                    		new String[] {}), MessageType.WARNING, sender);
    	            }
    	        }
            } catch (Exception e) {
                e.printStackTrace(); // Вивід виключення в лог
            }
	    });
	    return true;
	}


	private boolean reload(CommandSender sender) {
		if (!(sender instanceof Player) && sender.getName().equalsIgnoreCase("CONSOLE")) {
			GlobalManager manager = GlobalManager.getInstance();
			manager.reload();

			manager.getDatabaseManager().reload(manager, new TableRegistry(familyPlugin));
	        MarriageManager.getInstance(familyPlugin).reload();
	        ParentManager.getInstance(familyPlugin).reload();

	        Logger.info(familyPlugin, Translator.translateKyeWorld(manager, "family_plugin_reloaded" , null));
			return true;
		}
		return false;
	}

	private boolean infoMarryElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return GlobalManager.getInstance().getMarriageManager().infoMarryElement();
		}
		return false;
	}
	
	private boolean infoParentElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return GlobalManager.getInstance().getParentManager().infoParentElement();
		}
		return false;
	}
}
