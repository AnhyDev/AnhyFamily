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
    
    public static MessageComponents buildDefaultAccessMessage(Player player, String group, Access access) {
        String baseCommand = "/fchat default ";
        String accessColor = getAccessColor(access);
        String groupColor = "#0bdebb";
        String messageColor = "#2ab1fa";
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};
        
        String groupAccess = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_message_group_access", langs),
                new String[] {groupColor + group + messageColor, accessColor + access.name() + messageColor}));
        // family_message_group_access: "У групи %s доступ %s, щоб змінити доступ оберіть варіант: "
        
        return MessageComponents.builder()
            .content(groupAccess)
            .hexColor(messageColor)
            .append(MessageComponents.builder()
                .content("allow")
                .hexColor("#00FF00")
                .hoverMessage(stringSetAccess(Access.TRUE, langs))
                .clickActionRunCommand(baseCommand + group + " allow")
                .build())
            .append(MessageComponents.builder()
                .content(" | ")
                .hexColor("#cedcf2")
                .build())
            .append(MessageComponents.builder()
                .content("deny")
                .hexColor("#FF0000")
                .hoverMessage(stringSetAccess(Access.FALSE, langs))
                .clickActionRunCommand(baseCommand + group + " deny")
                .build())
            .build();
    }
}
