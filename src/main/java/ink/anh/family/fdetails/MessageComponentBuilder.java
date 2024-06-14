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
    
    private static String hoverReplyChat(String[] langs) {
    	return StringUtils.formatString(Translator.translateKyeWorld(libraryManager, SEPARATOR_COLOR + "family_hover_reply_chat", langs), new String[] {});
    }

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
		return StringUtils.colorize(colorize(PREFIX_COLOR, "[" + baseCommand.toUpperCase() + "] "));
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

        String groupAccess = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, " family_message_group_access", langs),
                new String[]{colorize(GROUP_COLOR, group), colorize(accessColor, getFormattedAccessStatus(access))}));
        // family_message_group_access: "У групи %s доступ %s, щоб змінити доступ оберіть варіант: "
        
        String prefix = getPrefix(baseCommand);
        String accessTrue = getFormattedAccessStatus(Access.TRUE);
        String accessFalse = getFormattedAccessStatus(Access.FALSE);
        String hoverAccessTrue = formatSetAccessMessage(Access.TRUE, langs);
        String hoverAccessFalse = formatSetAccessMessage(Access.FALSE, langs);

        return MessageComponents.builder()
            .content(prefix)
            .hoverMessage(hoverReplyChat(langs))
            .insertTextChat("/" + baseCommand)
            .append(MessageComponents.builder()
            	.content(groupAccess)
                .hexColor(MESSAGE_COLOR)
            	.build())
            .append(MessageComponents.builder()
                .content(accessTrue)
                .hexColor(ACCESS_COLOR_TRUE)
                .hoverMessage(hoverAccessTrue)
                .clickActionRunCommand(command + group + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(accessFalse)
                .hexColor(ACCESS_COLOR_FALSE)
                .hoverMessage(hoverAccessFalse)
                .clickActionRunCommand(command + group + " deny")
                .build())
            .build();
    }

    public static MessageComponents buildCheckAccessMessageComponent(Player player, String nickname, Access access, String baseCommand) {
        String command = "/" + baseCommand + " access ";
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};

        String accessStatus = getFormattedAccessStatus(access);

        String checkAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_access_get", langs),
                new String[]{nickname, colorize(getAccessColor(access), accessStatus)}));
        // family_access_get: "Гравець %s має доступ: %s"
        String chsngeAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_access_change", langs),
                new String[]{}));
        // family_access_change: "Змінити доступ: "
        
        String prefix = getPrefix(baseCommand);
        String accessTrue = getFormattedAccessStatus(Access.TRUE);
        String accessFalse = getFormattedAccessStatus(Access.FALSE);
        String accessDefault = getFormattedAccessStatus(Access.DEFAULT);
        String hoverAccessTrue = formatSetAccessMessage(Access.TRUE, langs);
        String hoverAccessFalse = formatSetAccessMessage(Access.FALSE, langs);
        String hoverAccessDefault = formatSetAccessMessage(Access.DEFAULT, langs);

        return MessageComponents.builder()
            .content(prefix)
            .hoverMessage(hoverReplyChat(langs))
            .insertTextChat("/" + baseCommand)
            .append(MessageComponents.builder()
                .content(checkAccessMessage)
                .hexColor(MESSAGE_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(chsngeAccessMessage)
                .hexColor(MESSAGE_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(accessTrue)
                .hexColor(ACCESS_COLOR_TRUE)
                .hoverMessage(hoverAccessTrue)
                .clickActionRunCommand(command + nickname + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(accessFalse)
                .hexColor(ACCESS_COLOR_FALSE)
                .hoverMessage(hoverAccessFalse)
                .clickActionRunCommand(command + nickname + " deny")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor(SEPARATOR_COLOR)
                .build())
            .append(MessageComponents.builder()
                .content(accessDefault)
                .hexColor(ACCESS_COLOR_DEFAULT)
                .hoverMessage(hoverAccessDefault)
                .clickActionRunCommand(command + nickname + " default")
                .build())
            .build();
    }
}
