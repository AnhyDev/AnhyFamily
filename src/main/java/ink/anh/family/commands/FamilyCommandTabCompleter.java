package ink.anh.family.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("firstname");
            completions.add("surname");
            completions.add("divorce");
            completions.add("separate");
            completions.add("info");
            completions.add("profile");
            completions.add("tree");
            completions.add("sugges");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "tree":
                case "info":
                case "profile":
                    completions.addAll(getPlayerNames(args[1]));
                    break;
                case "separate":
                    completions.add("spouse");
                    completions.add("child");
                    completions.add("parent");
                    completions.addAll(getPlayerNames(args[1]));
                    break;
                case "sugges":
                    completions.add("firstname");
                    completions.add("surname");
                    completions.add("accept");
                    completions.add("refuse");
                    break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("sugges") && 
                   (args[1].equalsIgnoreCase("firstname") || args[1].equalsIgnoreCase("surname"))) {
            completions.addAll(getPlayerNames(args[2]));
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
