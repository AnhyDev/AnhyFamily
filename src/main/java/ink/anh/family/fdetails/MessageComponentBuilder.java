package ink.anh.family.fdetails;

import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.GlobalManager;

public class MessageComponentBuilder {

    private static GlobalManager libraryManager = GlobalManager.getInstance();
    private static final String MESSAGE_COLOR = "#0bdebb";
    private static final String GROUP_COLOR = "#0bdebb";
    private static final String ACCESS_COLOR_TRUE = "#00FF00";
    private static final String ACCESS_COLOR_FALSE = "#FF0000";
    private static final String ACCESS_COLOR_DEFAULT = "#FFFF00";
    private static final String SEPARATOR_COLOR = "#cedcf2";

    private static String getAccessColor(Access access) {
        return access == Access.TRUE ? ACCESS_COLOR_TRUE : access == Access.FALSE ? ACCESS_COLOR_FALSE : ACCESS_COLOR_DEFAULT;
    }

    private static String stringSetAccess(Access access, String[] langs) {
        return StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_set_default", langs),
                new String[]{access.name()}));
        // family_hover_set_default: "Встановити доступ %s"
    }

    private static String colorize(String startColor, String element) {
        return startColor + element + MESSAGE_COLOR;
    }

    public static MessageComponents buildDefaultAccessMessage(Player player, String group, Access access, String baseCommand) {
        String command = "/" + baseCommand + " default ";
        String accessColor = getAccessColor(access);
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};

        String groupAccess = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, baseCommand.toUpperCase() + " family_message_group_access", langs),
                new String[]{colorize(GROUP_COLOR, group), colorize(accessColor, access.name())}));
        // family_message_group_access: "У групи %s доступ %s, щоб змінити доступ оберіть варіант: "

        return MessageComponents.builder()
            .content(groupAccess)
            .hexColor(MESSAGE_COLOR)
            .append(MessageComponents.builder()
                .content("ALLOW")
                .hexColor(ACCESS_COLOR_TRUE)
                .hoverMessage(stringSetAccess(Access.TRUE, langs))
                .clickActionRunCommand(command + group + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content("DENY")
                .hexColor(ACCESS_COLOR_FALSE)
                .hoverMessage(stringSetAccess(Access.FALSE, langs))
                .clickActionRunCommand(command + group + " deny")
                .build())
            .build();
    }

    public static MessageComponents buildCheckAccessMessage(Player player, String nickname, Access access, String baseCommand) {
        String command = "/" + baseCommand + " access ";

        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};

        String accessStatus;
        switch (access) {
            case TRUE:
                accessStatus = "allow";
                break;
            case FALSE:
                accessStatus = "deny";
                break;
            case DEFAULT:
                accessStatus = "default";
                break;
            default:
                accessStatus = "unknown";
        }

        String checkAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_access_get", langs),
                new String[]{nickname, colorize(getAccessColor(access), accessStatus.toUpperCase())}));
        // family_access_get: "Гравець %s має доступ: %s"

        String changeAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_access_change", langs), new String[]{}));
        // family_access_change: "Змінити доступ: "

        return MessageComponents.builder()
            .content(checkAccessMessage)
            .hexColor(MESSAGE_COLOR)
            .append(MessageComponents.builder()
                .content(changeAccessMessage)
                .hexColor(MESSAGE_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content("ALLOW")
                .hexColor(ACCESS_COLOR_TRUE)
                .hoverMessage(stringSetAccess(Access.TRUE, langs))
                .clickActionRunCommand(command + nickname + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content("DENY")
                .hexColor(ACCESS_COLOR_FALSE)
                .hoverMessage(stringSetAccess(Access.FALSE, langs))
                .clickActionRunCommand(command + nickname + " deny")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content("DEFAULT")
                .hexColor(ACCESS_COLOR_DEFAULT)
                .hoverMessage(stringSetAccess(Access.DEFAULT, langs))
                .clickActionRunCommand(command + nickname + " default")
                .build())
            .build();
    }
}
