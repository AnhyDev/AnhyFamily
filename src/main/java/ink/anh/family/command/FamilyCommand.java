package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.database.DatabaseManager;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.command.sub.Clear;
import ink.anh.family.command.sub.Separation;
import ink.anh.family.db.TableRegistry;
import ink.anh.family.info.FamilyInfoCommandHandler;
import ink.anh.family.info.FamilyTreeCommandHandler;
import ink.anh.family.info.Surname;
import ink.anh.family.marry.Divorce;
import ink.anh.family.marry.MarriageManager;
import ink.anh.family.parents.ParentManager;
import ink.anh.family.marry.ActionsPriest;
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
	        if (args.length > 0) {
	            switch (args[0].toLowerCase()) {
	                case "surname":
	                    new Surname().setSurname(sender, args);
	                    break;
	                case "setsurname":
	                    new Surname().setSurnameFromConsole(sender, args);
	                    break;
	                case "marry":
	                    new ActionsPriest(familyPlugin).marry(sender, args);
	                    break;
	                case "clear":
	                    new Clear(familyPlugin).exeClearFamily(sender, args);
	                    break;
	                case "divorce":
	                    new Divorce(familyPlugin).separate(sender);
	                    break;
	                case "separate":
	                    new Separation(familyPlugin).separate(sender, args);
	                    break;
	                case "info":
	                    new FamilyInfoCommandHandler().handleCommand(sender, args, true);
	                    break;
	                case "infos":
	                    new FamilyInfoCommandHandler().handleCommand(sender, args, false);
	                    break;
	                case "tree":
	                    new FamilyTreeCommandHandler().handleTreeCommand(sender, args, true);
	                    break;
	                case "trees":
	                    new FamilyTreeCommandHandler().handleTreeCommand(sender, args, false);
	                    break;
	                case "parentelement":
	                    infoParentElement(sender);
	                    break;
	                case "marryelement":
	                    infoMarryElement(sender);
	                    break;
	                case "reload":
	                    reload(sender);
	                    break;
	                default:
	                    sendMessage(new MessageForFormatting("family_err_command_format /family <param>", new String[] {}), MessageType.WARNING, sender);
	            }
	        }
	    });
	    return true;
	}


	private boolean reload(CommandSender sender) {
		if (!(sender instanceof Player) && sender.getName().equalsIgnoreCase("CONSOLE")) {
			GlobalManager manager = GlobalManager.getInstance();
			manager.reload();

		    DatabaseManager.reload(manager, new TableRegistry(familyPlugin));
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
