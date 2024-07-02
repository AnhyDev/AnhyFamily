package ink.anh.family.fdetails.hugs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FamilyHugsTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList(
            "access", "default", "check", "defaultcheck", "allow", "deny", "allowall", "denyall"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (String cmd : COMMANDS) {
                if (cmd.startsWith(args[0].toLowerCase())) {
                    suggestions.add(cmd);
                }
            }
            return suggestions;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("access") || args[0].equalsIgnoreCase("allow") || args[0].equalsIgnoreCase("deny")) {
                // Return player names or other relevant data
                return null; // Placeholder for player names
            } else if (args[0].equalsIgnoreCase("default") || args[0].equalsIgnoreCase("defaultcheck")) {
                return Arrays.asList("children", "parents");
            } else if (args[0].equalsIgnoreCase("allowall") || args[0].equalsIgnoreCase("denyall")) {
                return Arrays.asList("true", "false");
            }
        }
        return Collections.emptyList();
    }
}
