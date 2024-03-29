package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Sender;
import ink.anh.family.command.sub.Clear;
import ink.anh.family.command.sub.Separation;
import ink.anh.family.db.DatabaseManager;
import ink.anh.family.info.FamilyInfoCommandHandler;
import ink.anh.family.info.FamilyTreeCommandHandler;
import ink.anh.family.info.Surname;
import ink.anh.family.marry.Divorce;
import ink.anh.family.marry.MarriageManager;
import ink.anh.family.parents.ParentManager;
import ink.anh.family.marry.ActionsPriest;
import ink.anh.api.messages.MessageForFormatting;

public class FamilyCommand extends Sender implements CommandExecutor {

	
	public FamilyCommand(AnhyFamily familyPlugin) {
		super(familyPlugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        
        if (args.length > 0) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                switch (args[0].toLowerCase()) {
                case "surname":
                    return new Surname(familyPlugin).setSurname(sender, args);
                case "setsurname":
                    return new Surname(familyPlugin).setSurnameFromConsole(sender, args);
                case "marry":
                    return new ActionsPriest(familyPlugin).marry(sender, args);
                case "clear":
                    return new Clear(familyPlugin).exeClearFamily(sender, args);
                case "divorce":
                    return new Divorce(familyPlugin).separate(sender);
                case "separate":
                    return new Separation(familyPlugin).separate(sender, args);
                case "info":
                    return new FamilyInfoCommandHandler(familyPlugin).handleCommand(sender, args, true);
                case "infos":
                    return new FamilyInfoCommandHandler(familyPlugin).handleCommand(sender, args, false);
                case "tree":
                	return new FamilyTreeCommandHandler(familyPlugin).handleTreeCommand(sender, args, true);
                case "trees":
                	return new FamilyTreeCommandHandler(familyPlugin).handleTreeCommand(sender, args, false);
                case "parentelement":
                	return infoParentElement(sender);
                case "marryelement":
                	return infoMarryElement(sender);
                case "reload":
                	return reload(sender);
                default:
                	sendMessage(new MessageForFormatting("family_err_command_format /family <param>", null), MessageType.WARNING, sender);
                    return false;
                }
            });

            // Цей блок коду виконається, коли операція завершиться, і не блокує головний потік.
            future.thenAccept(result -> {

                if (!result) {
                	if (!sender.getName().equalsIgnoreCase("CONSOLE"))
                		Logger.info(familyPlugin, sender.getName() + " failed to execute command: " + "/family " + String.join(" ", args));
                } else {
                	if (!sender.getName().equalsIgnoreCase("CONSOLE"))
                		Logger.info(familyPlugin, sender.getName() + " successfully executed command: " + "/family " + String.join(" ", args));
                }
            });

            // Завжди повертається true, щоб Bukkit не показував повідомлення про помилку.
            return true;
        }
        return false;
    }

	private boolean reload(CommandSender sender) {
		if (!(sender instanceof Player) && sender.getName().equalsIgnoreCase("CONSOLE")) {
			GlobalManager manager = GlobalManager.getManager(familyPlugin);
			manager.reload();

		    DatabaseManager.reload(familyPlugin);
	        MarriageManager.getInstance(familyPlugin).reload();
	        ParentManager.getInstance(familyPlugin).reload();

	        Logger.info(familyPlugin, Translator.translateKyeWorld(familyPlugin.getGlobalManager(), "family_plugin_reloaded" , null));
			return true;
		}
		return false;
	}

	private boolean infoMarryElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return familyPlugin.getMarriageManager().infoMarryElement();
		}
		return false;
	}
	
	private boolean infoParentElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return familyPlugin.getParentManager().infoParentElement();
		}
		return false;
	}

}
