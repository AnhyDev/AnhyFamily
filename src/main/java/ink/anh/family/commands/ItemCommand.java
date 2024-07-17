package ink.anh.family.commands;

import ink.anh.family.AnhyFamily;
import ink.anh.family.payment.ItemCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemCommand implements CommandExecutor {

    private final AnhyFamily plugin;
    private final ItemCommandHandler commandHandler;

    public ItemCommand(AnhyFamily plugin) {
        this.plugin = plugin;
        this.commandHandler = new ItemCommandHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Цю команду можуть виконувати лише гравці.");
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("Permissions.FAMILY_ADMIN")) {
            player.sendMessage("У тебе немає прав для виконання цієї команди.");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage("Використання: /item <add|clear|get|remove> [key]");
            return false;
        }

        String action = args[0].toLowerCase();
        String key = args.length == 2 ? args[1] : null;

        switch (action) {
            case "add":
                if (key == null) {
                    player.sendMessage("Використання: /item add <key>");
                    return false;
                }
                return commandHandler.handleAdd(player, key);

            case "clear":
                return commandHandler.handleClear(player);

            case "get":
                if (key == null) {
                    player.sendMessage("Використання: /item get <key>");
                    return false;
                }
                return commandHandler.handleGet(player, key);

            case "remove":
                if (key == null) {
                    player.sendMessage("Використання: /item remove <key>");
                    return false;
                }
                return commandHandler.handleRemove(player, key);

            default:
                player.sendMessage("Невідома команда. Використання: /item <add|clear|get|remove> [key]");
                return false;
        }
    }
}
