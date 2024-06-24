package ink.anh.family.fdetails.home;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ink.anh.family.fdetails.symbol.FamilySymbolManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyHomeTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("set");
            completions.add("accept");
            completions.add("refuse");
            completions.add("access");
            completions.add("check");
            completions.add("default");
            completions.add("#");
            completions.add("@");

            if (args[0].startsWith("#")) {
                String inputSymbol = args[0].substring(1).toUpperCase();
                completions.addAll(FamilySymbolManager.getAllFamilySymbols().stream()
                        .filter(symbol -> symbol.startsWith(inputSymbol))
                        .map(symbol -> "#" + symbol)
                        .collect(Collectors.toList()));
            }

            if (args[0].startsWith("@")) {
                String inputName = args[0].substring(1).toLowerCase();
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(p -> "@" + p.getName())
                        .filter(name -> name.toLowerCase().startsWith(inputName))
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("access") || args[0].equalsIgnoreCase("check")) {
                String inputName = args[1].toLowerCase();
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(inputName))
                        .collect(Collectors.toList()));
            } else if (args[0].equalsIgnoreCase("default")) {
                completions.add("children");
                completions.add("parents");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("access")) {
                completions.add("allow");
                completions.add("deny");
                completions.add("default");
            } else if (args[0].equalsIgnoreCase("default")) {
                completions.add("allow");
                completions.add("deny");
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
