package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.marry.ActionsPriest;
import ink.anh.family.marry.ActionsBridesPrivate;
import ink.anh.api.messages.MessageForFormatting;

public class MarryCommand extends Sender implements CommandExecutor {

    private AnhyFamily familyPlugin;

    public MarryCommand(AnhyFamily familyPlugin) {
        super(familyPlugin.getGlobalManager());
        this.familyPlugin = familyPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendMessage(new MessageForFormatting("family_err_command_format '/marry public player1 player2' or '/marry private player'", new String[]{}), MessageType.WARNING, sender);
            return true;
        }

        CompletableFuture.runAsync(() -> {
            String type = args[0].toLowerCase();
            switch (type) {
                case "public":
                    if (args.length < 3) {
                        sendMessage(new MessageForFormatting("family_err_command_format '/marry public player1 player2'", new String[]{}), MessageType.WARNING, sender);
                    } else {
                        handlePublicMarriage(sender, args);
                    }
                    break;
                case "private":
                    if (args.length < 2) {
                        sendMessage(new MessageForFormatting("family_err_command_format '/marry private player'", new String[]{}), MessageType.WARNING, sender);
                    } else {
                        handlePrivateMarriage(sender, args);
                    }
                    break;
                case "accept":
                    handleAcceptProposal(sender);
                    break;
                case "refuse":
                    handleRefuseProposal(sender);
                    break;
                default:
                    sendMessage(new MessageForFormatting("family_err_command_format '/marry public player1 player2' or '/marry private player'", new String[]{}), MessageType.WARNING, sender);
            }
        });

        return true;
    }

    private void handlePublicMarriage(CommandSender sender, String[] args) {
        new ActionsPriest(familyPlugin).marry(sender, args);
        sendMessage(new MessageForFormatting("family_success_public_marriage", new String[]{args[1], args[2]}), MessageType.NORMAL, sender);
    }

    private void handlePrivateMarriage(CommandSender sender, String[] args) {
        new ActionsBridesPrivate(familyPlugin).proposePrivateMarriage(sender, args);
    }

    private void handleAcceptProposal(CommandSender sender) {
        new ActionsBridesPrivate(familyPlugin).acceptPrivateMarriage(sender);
    }

    private void handleRefuseProposal(CommandSender sender) {
        new ActionsBridesPrivate(familyPlugin).refusePrivateMarriage(sender);
    }
}
