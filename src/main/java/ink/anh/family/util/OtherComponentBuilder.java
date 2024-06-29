package ink.anh.family.util;

import org.bukkit.entity.Player;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.GlobalManager;

public class OtherComponentBuilder {

    private static GlobalManager manager = GlobalManager.getInstance();

    private static MessageBuilder prefixPlugin() {
        return MessageComponents.builder()
                .content("[" + manager.getPluginName() + "] ")
                .hexColor(StringColorUtils.PLUGIN_COLOR)
                .decoration("BOLD", true);
    }

    private static MessageBuilder subPrefix(String subPrefix, String hexColor) {
        return MessageComponents.builder()
                .content("[" + subPrefix.toUpperCase() + "] ")
                .hexColor(hexColor);
    }

    public static MessageComponents buildComponent(String messageBase, String commandV1, String commandV2,
                                                    String messageV1, String messageV2, String hoverV1, String hoverV2,
                                                    MessageBuilder prefix, String messageColor, String v1Color, String v2Color, Player recipient) {
        String[] langs = getLangs(recipient);
        
        messageBase = StringUtils.colorize(Translator.translateKyeWorld(manager, messageBase, langs));
        messageV1 = StringUtils.colorize(Translator.translateKyeWorld(manager, messageV1, langs));
        messageV2 = StringUtils.colorize(Translator.translateKyeWorld(manager, messageV2, langs));
        hoverV1 = StringUtils.colorize(Translator.translateKyeWorld(manager, hoverV1, langs));
        hoverV2 = StringUtils.colorize(Translator.translateKyeWorld(manager, hoverV2, langs));

        return prefix.append(MessageComponents.builder()
                        .content(messageBase)
                        .hexColor(messageColor)
                        .build())
                .append(MessageComponents.builder()
                        .content(messageV1)
                        .hexColor(v1Color)
                        .hoverComponent(MessageComponents.builder().content(hoverV1).hexColor(v1Color).build())
                        .clickActionRunCommand(commandV1)
                        .build())
                .append(MessageComponents.builder()
                        .content(" | ")
                        .hexColor(StringColorUtils.SEPARATOR_COLOR)
                        .build())
                .append(MessageComponents.builder()
                        .content(messageV2)
                        .hexColor(v2Color)
                        .hoverComponent(MessageComponents.builder().content(hoverV2).hexColor(v2Color).build())
                        .clickActionRunCommand(commandV2)
                        .build())
                .build();
    }

    public static MessageComponents acceptMessageComponent(String messageBase, String baseCommand, String commandAccept, String commandRefuse,
                                                           String messageAccept, String messageRefuse, String hoverAccept, String hoverRefuse, Player recipient) {
        commandAccept = "/" + baseCommand + " " + commandAccept;
        commandRefuse = "/" + baseCommand + " " + commandRefuse;

        MessageBuilder prefix = prefixPlugin()
                .append(MessageComponents.builder().content(" ").build())
                .append(subPrefix(baseCommand, "#0bdebb").build());

        return buildComponent(messageBase, commandAccept, commandRefuse, messageAccept, messageRefuse, hoverAccept, hoverRefuse,
                prefix, StringColorUtils.MESSAGE_COLOR, StringColorUtils.ACCESS_COLOR_TRUE, StringColorUtils.ACCESS_COLOR_FALSE, recipient);
    }

    public static MessageComponents infoMessageComponent(String messageBase, String commandV1, String commandV2,
                                                         String messageV1, String messageV2, String hoverV1, String hoverV2, Player recipient) {
        MessageBuilder prefix = prefixPlugin()
                .append(MessageComponents.builder().content(" ").build());

        return buildComponent(messageBase, commandV1, commandV2, messageV1, messageV2, hoverV1, hoverV2,
                prefix, StringColorUtils.MESSAGE_COLOR, StringColorUtils.ACCESS_COLOR_TRUE, StringColorUtils.ACCESS_COLOR_DEFAULT, recipient);
    }

    public static MessageComponents acceptMessageComponent(MessageBuilder prefix, String messageBase, String baseCommand, String commandAccept, String commandRefuse, Player recipient) {

        return buildComponent(
                messageBase, 
                "/" + baseCommand + " " + commandAccept, 
                "/" + baseCommand + " " + commandRefuse, 
                "family_request_confirm", 
                "family_request_reject", 
                "family_request_confirm_hover", 
                "family_request_reject_hover", 
                prefix, 
                StringColorUtils.MESSAGE_COLOR, 
                StringColorUtils.ACCESS_COLOR_TRUE, 
                StringColorUtils.ACCESS_COLOR_FALSE, 
                recipient
        );
    }

    private static String[] getLangs(Player recipient) {
        return recipient != null ? LangUtils.getPlayerLanguage(recipient) : new String[]{manager.getDefaultLang()};
    }
}
