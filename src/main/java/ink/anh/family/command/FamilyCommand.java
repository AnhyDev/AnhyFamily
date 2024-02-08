package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.command.sub.Clear;
import ink.anh.family.command.sub.Separation;
import ink.anh.family.command.sub.Surname;
import ink.anh.family.marry.Divorce;
import ink.anh.family.marry.ActionsPriest;
import ink.anh.family.parents.Adopt;
import ink.anh.api.messages.MessageChat;
import ink.anh.api.messages.MessageForFormatting;

public class FamilyCommand implements CommandExecutor {

	AnhyFamily plugin;
	
	public FamilyCommand(AnhyFamily plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        
        if (args.length > 0) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                switch (args[0].toLowerCase()) {
                case "surname":
                    return new Surname(plugin).setSurname(sender, args);
                case "marry":
                    return new ActionsPriest(plugin).marry(sender, args);
                case "clear":
                    return new Clear(plugin).exeClearFamily(sender, args);
                /*case "info":
                	return new CommandInfo(sender).execFamily(args, true);
                /*case "infos":
                	return new CommandInfo(sender).execFamily(args, false);*/
                case "divorce":
                    return new Divorce(plugin).separate(sender, args);
                case "forceadopt":
                    return new Adopt(plugin).forceAdopt(sender, args);
                case "accept":
                    return new Adopt(plugin).exeAddParents(sender);
                case "separate":
                    return new Separation(plugin).separate(sender, args);
                case "adopt":
                    return new Adopt(plugin).execAdopt(sender, args);
                /*case "tree":
                	return new CommandInfo(sender).execTree(args, true);
                case "trees":
                	return new CommandInfo(sender).execTree(args, false);*/
                case "infoparadd":
                	return infoParentElement(sender);
                case "infomaradd":
                	return infoMarryElement(sender);
                default:
                	sendMessage(sender, new MessageForFormatting("family_err_command_format /family <param>", null), MessageType.WARNING);
                    return false;
                }
            });

            // Цей блок коду виконається, коли операція завершиться, і не блокує головний потік.
            future.thenAccept(result -> {

                if (!result) {
                	if (!sender.getName().equalsIgnoreCase("CONSOLE"))
                		Logger.info(plugin, sender.getName() + " failed to execute command: " + "/family " + String.join(" ", args));
                } else {
                	if (!sender.getName().equalsIgnoreCase("CONSOLE"))
                		Logger.info(plugin, sender.getName() + " successfully executed command: " + "/family " + String.join(" ", args));
                }
            });

            // Завжди повертається true, щоб Bukkit не показував повідомлення про помилку.
            return true;
        }
        return false;
    }
	
	private boolean infoMarryElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return plugin.getMarriageManager().infoMarryElement();
		}
		return false;
	}
	
	private boolean infoParentElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return plugin.getParentManager().infoParentElement();
		}
		return false;
	}

	private void sendMessage(CommandSender sender, MessageForFormatting textForFormatting, MessageType type) {
    	MessageChat.sendMessage(plugin.getGlobalManager(), sender, textForFormatting, type, true);
    }

}
