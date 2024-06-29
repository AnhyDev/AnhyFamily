package ink.anh.family.fdetails;

import org.bukkit.entity.Player;
import ink.anh.api.enums.Access;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.GlobalManager;
import ink.anh.family.util.OtherComponentBuilder;
import ink.anh.family.util.StringColorUtils;

public class MessageComponentBuilder {

    private static GlobalManager manager = GlobalManager.getInstance();

    private static String formatSetAccessMessage(Access access, String[] langs) {
        return StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_hover_set_default", langs),
                new String[]{getFormattedAccessStatus(access)}));
    }

    private static String prefixColor(String baseCommand) {
        switch (baseCommand.toLowerCase()) {
            case "fchat":
                return StringColorUtils.PREFIX_CHAT_COLOR;
            case "fchest":
                return StringColorUtils.PREFIX_CHEST_COLOR;
            case "fhome":
                return StringColorUtils.PREFIX_HOME_COLOR;
            default:
                return StringColorUtils.PLUGIN_COLOR;
        } 
    }
    
    private static MessageBuilder prefix(String baseCommand, String[] langs) {
    	return MessageComponents.builder()
                .content("[" + manager.getPluginName() + "] ")
                .hexColor(StringColorUtils.PLUGIN_COLOR)
                .decoration("BOLD", true)
                .append(MessageComponents.builder()
                    .content("[" + baseCommand.toUpperCase() + "] ")
                    .hexColor(prefixColor(baseCommand))
                    .build());
    }
    
    public static String getFormattedAccessStatus(Access access) {
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
        return status;
    }

    public static MessageComponents buildDefaultAccessMessageComponent(Player player, String group, Access access, String baseCommand) {
        String command = "/" + baseCommand + " default ";

        String[] langs = getLangs(player);

        String checkAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_message_group_access", langs),
                new String[]{group.toUpperCase()}));

        String changeAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_access_change", langs),
                new String[]{}));

        return prefix(baseCommand, langs)
                .append(MessageComponents.builder()
                    	.content(checkAccessMessage)
                        .hexColor(StringColorUtils.MESSAGE_COLOR)
                    	.build())
                    .append(MessageComponents.builder()
                        .content(getFormattedAccessStatus(access))
                        .hexColor(StringColorUtils.getAccessColor(access))
                        .build())
                    .append(MessageComponents.builder()
                        .content(changeAccessMessage)
                        .hexColor(StringColorUtils.MESSAGE_COLOR)
                        .build())
                .append(MessageComponents.builder()
                        .content(getFormattedAccessStatus(Access.TRUE))
                        .hexColor(StringColorUtils.ACCESS_COLOR_TRUE)
                        .hoverComponent(MessageComponents.builder().content(formatSetAccessMessage(Access.TRUE, langs)).hexColor(StringColorUtils.ACCESS_COLOR_TRUE).build())
                        .clickActionRunCommand(command + group + " allow")
                        .build())
                .append(MessageComponents.builder()
                        .content(" | ")
                        .hexColor(StringColorUtils.SEPARATOR_COLOR)
                        .build())
                .append(MessageComponents.builder()
                        .content(getFormattedAccessStatus(Access.FALSE))
                        .hexColor(StringColorUtils.ACCESS_COLOR_FALSE)
                        .hoverComponent(MessageComponents.builder().content(formatSetAccessMessage(Access.FALSE, langs)).hexColor(StringColorUtils.ACCESS_COLOR_FALSE).build())
                        .clickActionRunCommand(command + group + " deny")
                        .build())
                .build();
    }
    
    public static MessageComponents buildCheckAccessMessageComponent(Player player, String nickname, Access access, String baseCommand) {
        String command = "/" + baseCommand + " access ";

        String[] langs = getLangs(player);

        String checkAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_access_player_get", langs),
                new String[]{nickname}));

        String changeAccessMessage = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_access_change", langs),
                new String[]{}));

        return prefix(baseCommand, langs)
                .append(MessageComponents.builder()
                        .content(checkAccessMessage)
                        .hexColor(StringColorUtils.MESSAGE_COLOR)
                        .build())
                    .append(MessageComponents.builder()
                        .content(getFormattedAccessStatus(access))
                        .hexColor(StringColorUtils.getAccessColor(access))
                        .build())
                    .append(MessageComponents.builder()
                        .content(changeAccessMessage)
                        .hexColor(StringColorUtils.MESSAGE_COLOR)
                        .build())
                    .append(MessageComponents.builder()
                        .content(getFormattedAccessStatus(Access.TRUE))
                        .hexColor(StringColorUtils.ACCESS_COLOR_TRUE)
                        .hoverComponent(MessageComponents.builder().content(formatSetAccessMessage(Access.TRUE, langs)).hexColor(StringColorUtils.ACCESS_COLOR_TRUE).build())
                        .clickActionRunCommand(command + nickname + " allow")
                        .build())
                    .append(MessageComponents.builder()
                        .content(" | ")
                        .hexColor(StringColorUtils.SEPARATOR_COLOR)
                        .build())
                    .append(MessageComponents.builder()
                        .content(getFormattedAccessStatus(Access.FALSE))
                        .hexColor(StringColorUtils.ACCESS_COLOR_FALSE)
                        .hoverComponent(MessageComponents.builder().content(formatSetAccessMessage(Access.FALSE, langs)).hexColor(StringColorUtils.ACCESS_COLOR_FALSE).build())
                        .clickActionRunCommand(command + nickname + " deny")
                        .build())
                    .append(MessageComponents.builder()
                        .content(" | ")
                        .hexColor(StringColorUtils.SEPARATOR_COLOR)
                        .build())
                    .append(MessageComponents.builder()
                        .content(getFormattedAccessStatus(Access.DEFAULT))
                        .hexColor(StringColorUtils.ACCESS_COLOR_DEFAULT)
                        .hoverComponent(MessageComponents.builder().content(formatSetAccessMessage(Access.DEFAULT, langs)).hexColor(StringColorUtils.ACCESS_COLOR_DEFAULT).build())
                        .clickActionRunCommand(command + nickname + " default")
                        .build())
                    .build();
    }
    
    public static MessageComponents acceptMessageComponent(String messageBase, String baseCommand, String commandAccept, String commandRefuse, Player recipient) {
        String[] langs = getLangs(recipient);

        MessageBuilder prefix = prefix(baseCommand, langs);

        return OtherComponentBuilder.acceptMessageComponent(
                prefix,
                messageBase,
                baseCommand,
                commandAccept,
                commandRefuse,
                recipient
        );
    }

    private static String[] getLangs(Player recipient) {
    	return recipient != null ? LangUtils.getPlayerLanguage(recipient) : new String[]{manager.getDefaultLang()};
    }
}
