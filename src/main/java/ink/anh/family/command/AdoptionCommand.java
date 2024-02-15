package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.Sender;
import ink.anh.family.parents.Adopt;
import ink.anh.api.messages.MessageForFormatting;

public class AdoptionCommand extends Sender implements CommandExecutor {

	
	public AdoptionCommand(AnhyFamily familiPlugin) {
		super(familiPlugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length > 0) {
        	CompletableFuture.runAsync(() -> {
                switch (args[0].toLowerCase()) {
                case "forceadopt":
                    new Adopt(familiPlugin).forceAdopt(sender, args);
                    break;
                case "accept":
                    new Adopt(familiPlugin).accept(sender);
                    break;
                case "decline":
                    new Adopt(familiPlugin).declineAdoption(sender);
                    break;
                case "invite":
                    new Adopt(familiPlugin).adoption(sender, args);
                    break;
                case "cancel":
                    new Adopt(familiPlugin).cancelAdoption(sender);
                    break;
                default:
                	sendMessage(new MessageForFormatting("family_err_command_format /adoption [invite|accept|decline|cancel|forceadopt]", null), MessageType.WARNING, sender);
                }
            });

            return true;
        }
        return false;
    }
}
