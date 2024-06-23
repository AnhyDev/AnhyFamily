package ink.anh.family.command;

import java.util.concurrent.CompletableFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.marriage.ActionsBridesPrivate;
import ink.anh.family.marriage.ActionsBridesPublic;
import ink.anh.family.marriage.ActionsPriest;
import ink.anh.api.messages.MessageForFormatting;

public class MarryCommand extends Sender implements CommandExecutor {

    private AnhyFamily familyPlugin;

    public MarryCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendMessage(new MessageForFormatting("family_err_command_format '/marry public player1 player2' or '/marry private player'", new String[]{}), MessageType.WARNING, sender);
            return true;
        }

        CompletableFuture.runAsync(() -> {
            try {
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
            } catch (Exception e) {
                e.printStackTrace(); // Вивід виключення в лог
            }
        });

        return true;
    }

    private void handlePublicMarriage(CommandSender sender, String[] args) {
        new ActionsPriest(familyPlugin).marry(sender, args);
    }

    private void handlePrivateMarriage(CommandSender sender, String[] args) {
        new ActionsBridesPrivate(familyPlugin).proposePrivateMarriage(sender, args);
    }

    private void handleAcceptProposal(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        Player player = (Player) sender;
        ActionsBridesPrivate privateActions = new ActionsBridesPrivate(familyPlugin);
        
        if (privateActions.acceptPrivateMarriage(player)) {
        	return;
        } else {
            ActionsBridesPublic publicActions = new ActionsBridesPublic(familyPlugin);
        	if (publicActions.handleMarriage((Player) player, true)) {
            	return;
        	}
        }
        sendMessage(new MessageForFormatting("family_err_no_proposal", new String[]{}), MessageType.WARNING, sender);
    }

    private void handleRefuseProposal(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
            return;
        }

        Player player = (Player) sender;
        ActionsBridesPrivate privateActions = new ActionsBridesPrivate(familyPlugin);
        
        if (privateActions.refusePrivateMarriage(player)) {
            return;
        } else {
            ActionsBridesPublic publicActions = new ActionsBridesPublic(familyPlugin);
            publicActions.handleMarriage(player, false);
        }
        sendMessage(new MessageForFormatting("family_err_no_proposal", new String[]{}), MessageType.WARNING, sender);
    }
}
