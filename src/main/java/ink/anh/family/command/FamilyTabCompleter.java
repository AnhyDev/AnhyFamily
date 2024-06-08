package ink.anh.family.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("surname");
            completions.add("clear");
            completions.add("divorce");
            completions.add("separate");
            completions.add("info");
            completions.add("prefix");
            completions.add("setprefix");
            completions.add("acceptprefix");
            completions.add("reload");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "clear":
                case "info":
                case "prefix":
                    completions.addAll(getPlayerNames(args[1]));
                    break;
                case "separate":
                    completions.add("spouse");
                    completions.add("child");
                    completions.add("parent");
                    break;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("separate") && (args[1].equalsIgnoreCase("child") || args[1].equalsIgnoreCase("parent"))) {
                completions.addAll(getPlayerNames(args[2]));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getPlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
