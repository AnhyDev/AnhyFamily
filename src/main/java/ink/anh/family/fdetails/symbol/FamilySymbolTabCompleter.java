package ink.anh.family.fdetails.symbol;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class FamilySymbolTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList("set", "accept", "refuse", "@");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        if (args.length == 1) {
            return COMMANDS.stream()
                           .filter(command -> command.startsWith(args[0].toLowerCase()))
                           .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            // Повернути введений текст, якщо він відповідає умовам
            String input = args[1].toUpperCase();
            if (input.matches("[A-Z]{3,5}")) {
                return Arrays.asList(input);
            }
            return Arrays.asList();
        } else if (args.length == 1 && args[0].startsWith("@")) {
            String prefix = args[0].substring(1).toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                         .map(Player::getName)
                         .filter(name -> name.toLowerCase().startsWith(prefix))
                         .map(name -> "@" + name)
                         .collect(Collectors.toList());
        }

        return null;
    }
}
