package ink.anh.family.commands;

import java.util.concurrent.CompletableFuture;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.parents.Adopt;
import ink.anh.api.messages.MessageForFormatting;

public class AdoptionCommand extends Sender implements CommandExecutor {

	private AnhyFamily familyPlugin;
	
	public AdoptionCommand(AnhyFamily familyPlugin) {
		super(GlobalManager.getInstance());
		this.familyPlugin = familyPlugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length > 0) {
        	CompletableFuture.runAsync(() -> {
                try {
                    switch (args[0].toLowerCase()) {
                    case "accept":
                        new Adopt(familyPlugin).accept(sender);
                        break;
                    case "decline":
                        new Adopt(familyPlugin).declineAdoption(sender);
                        break;
                    case "invite":
                        new Adopt(familyPlugin).adoption(sender, args);
                        break;
                    case "cancel":
                        new Adopt(familyPlugin).cancelAdoption(sender);
                        break;
                    default:
                    	sendMessage(new MessageForFormatting("family_err_command_format /adoption [invite|accept|decline|cancel]", new String[] {}), MessageType.WARNING, sender);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return true;
        }
        return false;
    }
}
