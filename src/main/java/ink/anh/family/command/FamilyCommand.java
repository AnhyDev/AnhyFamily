package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ink.anh.api.messages.Logger;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Sender;
import ink.anh.family.command.sub.Clear;
import ink.anh.family.command.sub.Separation;
import ink.anh.family.command.sub.Surname;
import ink.anh.family.info.FamilyInfoCommandHandler;
import ink.anh.family.info.FamilyTreeCommandHandler;
import ink.anh.family.marry.Divorce;
import ink.anh.family.marry.ActionsPriest;
import ink.anh.api.messages.MessageForFormatting;

public class FamilyCommand extends Sender implements CommandExecutor {

	
	public FamilyCommand(AnhyFamily familiPlugin) {
		super(familiPlugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        
        if (args.length > 0) {
            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                switch (args[0].toLowerCase()) {
                case "surname":
                    return new Surname(familiPlugin).setSurname(sender, args);
                case "marry":
                    return new ActionsPriest(familiPlugin).marry(sender, args);
                case "clear":
                    return new Clear(familiPlugin).exeClearFamily(sender, args);
                case "divorce":
                    return new Divorce(familiPlugin).separate(sender);
                case "separate":
                    return new Separation(familiPlugin).separate(sender, args);
                case "info":
                    return new FamilyInfoCommandHandler(familiPlugin).handleCommand(sender, args, true);
                case "infos":
                    return new FamilyInfoCommandHandler(familiPlugin).handleCommand(sender, args, false);
                case "tree":
                	return new FamilyTreeCommandHandler(familiPlugin).handleTreeCommand(sender, args, true);
                case "trees":
                	return new FamilyTreeCommandHandler(familiPlugin).handleTreeCommand(sender, args, false);
                case "parentelement":
                	return infoParentElement(sender);
                case "marryelement":
                	return infoMarryElement(sender);
                default:
                	sendMessage(new MessageForFormatting("family_err_command_format /family <param>", null), MessageType.WARNING, sender);
                    return false;
                }
            });

            // Цей блок коду виконається, коли операція завершиться, і не блокує головний потік.
            future.thenAccept(result -> {

                if (!result) {
                	if (!sender.getName().equalsIgnoreCase("CONSOLE"))
                		Logger.info(familiPlugin, sender.getName() + " failed to execute command: " + "/family " + String.join(" ", args));
                } else {
                	if (!sender.getName().equalsIgnoreCase("CONSOLE"))
                		Logger.info(familiPlugin, sender.getName() + " successfully executed command: " + "/family " + String.join(" ", args));
                }
            });

            // Завжди повертається true, щоб Bukkit не показував повідомлення про помилку.
            return true;
        }
        return false;
    }

	private boolean infoMarryElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return familiPlugin.getMarriageManager().infoMarryElement();
		}
		return false;
	}
	
	private boolean infoParentElement(CommandSender sender) {
		if (sender.getName().equalsIgnoreCase("CONSOLE")) {
			return familiPlugin.getParentManager().infoParentElement();
		}
		return false;
	}

}
