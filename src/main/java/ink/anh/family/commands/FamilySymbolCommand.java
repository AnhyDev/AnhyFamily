package ink.anh.family.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.symbol.FamilySymbolSubCommand;

public class FamilySymbolCommand extends Sender implements CommandExecutor {
	
    private AnhyFamily familiPlugin;

    public FamilySymbolCommand(AnhyFamily familiPlugin) {
        super(GlobalManager.getInstance());
        this.familiPlugin = familiPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player player = null;

        if (sender instanceof Player) {

            player = (Player) sender;

            if (player != null) {
                return new FamilySymbolSubCommand(familiPlugin).onCommand(player, cmd, args);
            }
        }

        sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
        return true;
    }
}
