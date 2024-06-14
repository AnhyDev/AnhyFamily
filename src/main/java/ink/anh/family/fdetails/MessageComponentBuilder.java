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
    
    private static String getAccessColor(Access access) {
        return access == Access.TRUE ? "#00FF00" : access == Access.FALSE ? "#FF0000" : "#FFFF00";
    }
    
    private static String stringSetAccess(Access access, String[] langs) {
        return StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_set_default", langs),
                new String[] {access.name()}));
        // family_hover_set_default: "Встановити доступ %s"
    }
    
    public static MessageComponents buildDefaultAccessMessage(Player player, String group, Access access, String baseCommand) {
        String command = "/" + baseCommand + " default ";
        String accessColor = getAccessColor(access);
        String groupColor = "#0bdebb";
        String messageColor = "#0bdebb";
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};
        
        String groupAccess = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, baseCommand.toUpperCase() + " family_message_group_access", langs),
                new String[] {groupColor + group + messageColor, accessColor + access.name() + messageColor}));
        // family_message_group_access: "У групи %s доступ %s, щоб змінити доступ оберіть варіант: "
        
        return MessageComponents.builder()
            .content(groupAccess)
            .hexColor(messageColor)
            .append(MessageComponents.builder()
                .content("allow")
                .hexColor("#00FF00")
                .hoverMessage(stringSetAccess(Access.TRUE, langs))
                .clickActionRunCommand(command + group + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor("#cedcf2")
                .build())
            .append(MessageComponents.builder()
                .content("deny")
                .hexColor("#FF0000")
                .hoverMessage(stringSetAccess(Access.FALSE, langs))
                .clickActionRunCommand(command + group + " deny")
                .build())
            .build();
    }
    
    public static MessageComponents buildCheckAccessMessage(Player player, String nickname, Access access, String baseCommand) {
        String command = "/" + baseCommand + " access ";
        String messageColor = "#0bdebb";
        
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
                new String[] {nickname, accessStatus}));
        // family_access_get: "Гравець %s має доступ: %s"
        
        return MessageComponents.builder()
            .content(checkAccessMessage)
            .hexColor(messageColor)
            .append(MessageComponents.builder()
                .content("allow")
                .hexColor("#00FF00")
                .hoverMessage(stringSetAccess(Access.TRUE, langs))
                .clickActionRunCommand(command + nickname + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor("#cedcf2")
                .build())
            .append(MessageComponents.builder()
                .content("deny")
                .hexColor("#FF0000")
                .hoverMessage(stringSetAccess(Access.FALSE, langs))
                .clickActionRunCommand(command + nickname + " deny")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor("#cedcf2")
                .build())
            .append(MessageComponents.builder()
                .content("default")
                .hexColor("#FFFF00")
                .hoverMessage(stringSetAccess(Access.DEFAULT, langs))
                .clickActionRunCommand(command + nickname + " default")
                .build())
            .build();
    }
}
