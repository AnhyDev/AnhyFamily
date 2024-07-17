package ink.anh.family.commands;

import java.util.concurrent.CompletableFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.gender.GenderCommandHandler;
import ink.anh.api.messages.Sender;

public class GenderCommand extends Sender implements CommandExecutor {
    private final GenderCommandHandler commandHandler;

    public GenderCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.commandHandler = new GenderCommandHandler(familyPlugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CompletableFuture.runAsync(() -> {
            try {
                if (args.length == 0) {
                    if (sender instanceof Player) {
                        commandHandler.handleGenderInfo(sender, null);
                    }
                    return; // Early return for async context
                }

                switch (args[0].toLowerCase()) {
                    case "set":
                        if (sender instanceof Player && args.length >= 2) {
                            commandHandler.handleSetGender(sender, args[1]);
                        }
                        break;
                    case "info":
                        if (args.length <= 1) {
                            if (sender instanceof Player) {
                                commandHandler.handleGenderInfo(sender, null);
                            }
                        } else if (args.length >= 2) {
                            commandHandler.handleGenderInfo(sender, args[1]);
                        }
                        break;
                    default:
                        sendMessage(new MessageForFormatting("family_err_command_format /gender [set|info|reset|forceset]", new String[]{}), MessageType.WARNING, sender);
                }
            } catch (Exception e) {
                e.printStackTrace(); // Вивід виключення в лог
            }
        });
        return true;
    }
}
