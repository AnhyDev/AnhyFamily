package ink.anh.family.fdetails.hugs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FamilyHugsTabCompleter implements TabCompleter {

    private static final List<String> COMMANDS = Arrays.asList(
            "access", "default", "check", "defaultcheck", "allow", "deny", "allowall", "denyall", "remove", "list", "globalstatus"
    );

    private static final List<String> ALLOW_DENY_DEFAULT = Arrays.asList("allow", "deny", "default");
    private static final List<String> CHILDREN_PARENTS = Arrays.asList("children", "parents");
    private static final List<String> TRUE_FALSE = Arrays.asList("true", "false");
    private static final List<String> GLOBAL_STATUS_OPTIONS = Arrays.asList("allowall", "denyall");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return COMMANDS.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "access":
                case "allow":
                case "deny":
                case "remove":
                case "list":
                case "check":
                    return getPlayerNames(sender);
                case "default":
                case "defaultcheck":
                    return CHILDREN_PARENTS;
                case "allowall":
                case "denyall":
                    return TRUE_FALSE;
                case "globalstatus":
                    return GLOBAL_STATUS_OPTIONS;
                default:
                    return Collections.emptyList();
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "access":
                    return ALLOW_DENY_DEFAULT;
                case "default":
                    return ALLOW_DENY_DEFAULT.subList(0, 2); // Only "allow" and "deny" for default
                default:
                    return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private List<String> getPlayerNames(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
