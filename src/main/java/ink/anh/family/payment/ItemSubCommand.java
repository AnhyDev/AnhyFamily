package ink.anh.family.payment;

import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.Permissions;
import ink.anh.family.fplayer.FamilyService;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemSubCommand extends Sender {

    private final ItemCommandHandler commandHandler;

    public ItemSubCommand(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.commandHandler = new ItemCommandHandler(familyPlugin);
    }

    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = baseCheck(sender, args);
        
        if (player == null) {
            return false;
        }

        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/afam item <add|clear|get|remove> [key]"}), MessageType.WARNING, sender);
            return false;
        }

        String action = args[1].toLowerCase();
        String key = args.length == 3 ? args[2] : null;
        String validKeys = Arrays.stream(FamilyService.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        switch (action) {
            case "add":
                if (key == null) {
                    sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/afam item add <key>"}), MessageType.WARNING, sender);
                    return false;
                }
                if (!isValidKey(key)) {
                    sendMessage(new MessageForFormatting("family_err_invalid_key", new String[] {key, validKeys}), MessageType.WARNING, sender);
                    return false;
                }
                return commandHandler.handleAdd(player, key);

            case "clear":
                return commandHandler.handleClear(player);

            case "get":
                if (key == null) {
                    sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/afam item get <key>"}), MessageType.WARNING, sender);
                    return false;
                }
                if (!isValidKey(key)) {
                    sendMessage(new MessageForFormatting("family_err_invalid_key", new String[] {key, validKeys}), MessageType.WARNING, sender);
                    return false;
                }
                return commandHandler.handleGet(player, key);

            case "remove":
                if (key == null) {
                    sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/afam item remove <key>"}), MessageType.WARNING, sender);
                    return false;
                }
                if (!isValidKey(key)) {
                    sendMessage(new MessageForFormatting("family_err_invalid_key", new String[] {key, validKeys}), MessageType.WARNING, sender);
                    return false;
                }
                return commandHandler.handleRemove(player, key);

            default:
                sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/afam item <add|clear|get|remove> [key]"}), MessageType.WARNING, sender);
                return false;
        }
    }

    private boolean isValidKey(String key) {
        try {
            FamilyService.valueOf(key.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Player baseCheck(CommandSender sender, String[] args) {
        Player player = null;

        if (!(sender instanceof Player)) {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[] {}), MessageType.WARNING, sender);
            return player;
        }

        player = (Player) sender;

        if (!player.hasPermission(Permissions.FAMILY_ADMIN)) {
            sendMessage(new MessageForFormatting("family_err_not_have_permission", new String[] {}), MessageType.WARNING, sender);
            return player;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("item")) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/afam item <add|clear|get|remove> [key]"}), MessageType.WARNING, sender);
            return player;
        }
        return player;
    }
}
