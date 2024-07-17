package ink.anh.family.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import ink.anh.family.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdoptionTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Додавання можливих варіантів для першого аргументу
            completions.add("invite");
            completions.add("accept");
            completions.add("decline");
            completions.add("cancel");

            // Додавання команди forceadopt тільки для консолі або адміністратора
            if (sender.hasPermission(Permissions.FAMILY_ADMIN)) {
                completions.add("forceadopt");
            }
        } else if (args.length == 2) {
            // Додавання імен гравців для команд, які потребують другого аргументу
            if (args[0].equalsIgnoreCase("invite")) {
                String inputName = args[1].toLowerCase();
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(inputName))
                        .collect(Collectors.toList()));
            }
        }

        // Фільтрація результатів для часткового співпадіння
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
