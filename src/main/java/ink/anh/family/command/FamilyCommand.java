package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fplayer.Surname;
import ink.anh.family.fplayer.info.FamilyInfoCommandHandler;
import ink.anh.family.fplayer.info.FamilyTreeCommandHandler;
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
	        if (args.length > 0) {
	            switch (args[0].toLowerCase()) {
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
	                case "pref":
	                case "prefix":
	                    new FamilySymbolManager(familyPlugin, sender, args).getPrefix();
	                    break;
	                case "setprefix":
	                case "setpref":
	                case "sp":
	                    new FamilySymbolManager(familyPlugin, sender, args).setSymbol();
	                    break;
	                case "acceptprefix":
	                case "acceptpref":
	                case "ap":
	                    new FamilySymbolManager(familyPlugin, sender, args).acceptSymbol();
	                    break;
	                default:
	                    sendMessage(new MessageForFormatting("family_err_command_format /family <param>", new String[] {}), MessageType.WARNING, sender);
	            }
	        }
	    });
	    return true;
	}
}
