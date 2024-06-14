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
    private static final String PREFIX_COLOR = "#cedcf2";

    private static String getAccessColor(Access access) {
        return access == Access.TRUE ? ACCESS_COLOR_TRUE : access == Access.FALSE ? ACCESS_COLOR_FALSE : ACCESS_COLOR_DEFAULT;
    }

    private static String formatSetAccessMessage(Access access, String[] langs) {
        return StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_set_default", langs),
                new String[]{getFormattedAccessStatus(access)}));
        // family_hover_set_default: "Встановити доступ %s"
    }

    private static String colorize(String startColor, String element) {
        return startColor + element + MESSAGE_COLOR;
    }

    private static String getPrefix(String baseCommand) {
		return StringUtils.colorize(PREFIX_COLOR + "[" + baseCommand.toUpperCase() + "] " + MESSAGE_COLOR);
    }
    
    private static String getFormattedAccessStatus(Access access) {
        String status = "UNKNOWN";
        switch (access) {
            case TRUE:
                status = "ALLOW";
                break;
            case FALSE:
                status = "DENY";
                break;
            case DEFAULT:
                status = "DEFAULT";
                break;
            default:
                return status;
        }
        return StringUtils.colorize(colorize(getAccessColor(access), status.toUpperCase()));
    }

    public static MessageComponents buildDefaultAccessMessageComponent(Player player, String group, Access access, String baseCommand) {
        String command = "/" + baseCommand + " default ";
        String accessColor = getAccessColor(access);
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};

        String groupAccess = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, getPrefix(baseCommand) + " family_message_group_access", langs),
                new String[]{colorize(GROUP_COLOR, group), colorize(accessColor, getFormattedAccessStatus(access))}));
        // family_message_group_access: "У групи %s доступ %s, щоб змінити доступ оберіть варіант: "

        return MessageComponents.builder()
            .content(groupAccess)
            .append(MessageComponents.builder()
                .content(getFormattedAccessStatus(Access.TRUE))
                .hexColor(ACCESS_COLOR_TRUE)
                .hoverMessage(formatSetAccessMessage(Access.TRUE, langs))
                .clickActionRunCommand(command + group + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(getFormattedAccessStatus(Access.FALSE))
                .hexColor(ACCESS_COLOR_FALSE)
                .hoverMessage(formatSetAccessMessage(Access.FALSE, langs))
                .clickActionRunCommand(command + group + " deny")
                .build())
            .build();
    }

    public static MessageComponents buildCheckAccessMessageComponent(Player player, String nickname, Access access, String baseCommand) {
        String command = "/" + baseCommand + " access ";
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};

        String accessStatus = getFormattedAccessStatus(access);

        String checkAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, getPrefix(baseCommand) + "family_access_get", langs),
                new String[]{nickname, colorize(getAccessColor(access), accessStatus)}));
        // family_access_get: "Гравець %s має доступ: %s"
        String chsngeAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_access_change", langs),
                new String[]{}));
        // family_access_change: "Змінити доступ: "

        return MessageComponents.builder()
            .content(checkAccessMessage)
            .append(MessageComponents.builder()
                .content(chsngeAccessMessage)
                .hexColor(MESSAGE_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(getFormattedAccessStatus(Access.TRUE))
                .hexColor(ACCESS_COLOR_TRUE)
                .hoverMessage(formatSetAccessMessage(Access.TRUE, langs))
                .clickActionRunCommand(command + nickname + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(getFormattedAccessStatus(Access.FALSE))
                .hexColor(ACCESS_COLOR_FALSE)
                .hoverMessage(formatSetAccessMessage(Access.FALSE, langs))
                .clickActionRunCommand(command + nickname + " deny")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(getFormattedAccessStatus(Access.DEFAULT))
                .hexColor(ACCESS_COLOR_DEFAULT)
                .hoverMessage(formatSetAccessMessage(Access.DEFAULT, langs))
                .clickActionRunCommand(command + nickname + " default")
                .build())
            .build();
    }
}
