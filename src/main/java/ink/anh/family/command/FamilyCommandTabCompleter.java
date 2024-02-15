package ink.anh.family.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;

public class FamilyCommandTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("marry");
            completions.add("info");
            completions.add("infos");
            completions.add("divorce");
            completions.add("surname");
            completions.add("clear");
            completions.add("tree");
            completions.add("trees");
        }

        // More advanced completions can be added for each subcommand with additional if statements

        return completions;
    }
}

