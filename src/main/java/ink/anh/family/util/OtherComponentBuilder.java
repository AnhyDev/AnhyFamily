package ink.anh.family.util;

import org.bukkit.entity.Player;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.GlobalManager;

/**
 * Utility class for building various message components for the plugin.
 */
public class OtherComponentBuilder {

    private static GlobalManager manager = GlobalManager.getInstance();

    /**
     * Creates a MessageBuilder with the plugin prefix.
     *
     * @return a MessageBuilder with the plugin prefix and style.
     */
    private static MessageBuilder prefixPlugin() {
        return MessageComponents.builder()
                .content("[" + manager.getPluginName() + "] ")
                .hexColor(StringColorUtils.PLUGIN_COLOR)
                .decoration("BOLD", true);
    }

    /**
     * Creates a MessageBuilder with a sub-prefix.
     *
     * @param subPrefix the sub-prefix content.
     * @param hexColor  the color of the sub-prefix in hex.
     * @return a MessageBuilder with the sub-prefix and color.
     */
    private static MessageBuilder subPrefix(String subPrefix, String hexColor) {
        return MessageComponents.builder()
                .content("[" + subPrefix.toUpperCase() + "] ")
                .hexColor(hexColor);
    }

    /**
     * Builds a complex message component with multiple parts and actions.
     *
     * @param messageBase   the base message content.
     * @param commandV1     the command for the first action.
     * @param commandV2     the command for the second action.
     * @param messageV1     the message for the first action.
     * @param messageV2     the message for the second action.
     * @param hoverV1       the hover message for the first action.
     * @param hoverV2       the hover message for the second action.
     * @param prefix        the prefix to be used for the message.
     * @param messageColor  the color for the base message.
     * @param v1Color       the color for the first action message.
     * @param v2Color       the color for the second action message.
     * @param recipient     the player recipient for language translation.
     * @return the built MessageComponents object.
     */
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

    /**
     * Creates a message component for accept/decline actions with prefixed commands.
     *
     * @param messageBase    the base message content.
     * @param baseCommand    the base command to be prefixed.
     * @param commandAccept  the accept command.
     * @param commandRefuse  the refuse command.
     * @param messageAccept  the accept message.
     * @param messageRefuse  the refuse message.
     * @param hoverAccept    the hover message for accept.
     * @param hoverRefuse    the hover message for refuse.
     * @param recipient      the player recipient for language translation.
     * @return the built MessageComponents object.
     */
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

    /**
     * Creates a message component for accept/decline actions with a custom prefix.
     *
     * @param prefix         the custom prefix to be used.
     * @param messageBase    the base message content.
     * @param baseCommand    the base command to be prefixed.
     * @param commandAccept  the accept command.
     * @param commandRefuse  the refuse command.
     * @param recipient      the player recipient for language translation.
     * @return the built MessageComponents object.
     */
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

	/**
     * Creates an informational message component.
     *
     * @param messageBase    the base message content.
     * @param commandV1      the command for the first action.
     * @param commandV2      the command for the second action.
     * @param messageV1      the message for the first action.
     * @param messageV2      the message for the second action.
     * @param hoverV1        the hover message for the first action.
     * @param hoverV2        the hover message for the second action.
     * @param recipient      the player recipient for language translation.
     * @return the built MessageComponents object.
     */
    public static MessageComponents infoDoubleComponent(String messageBase, String commandV1, String commandV2,
                                                         String messageV1, String messageV2, String hoverV1, String hoverV2, Player recipient) {
        MessageBuilder prefix = prefixPlugin()
                .append(MessageComponents.builder().content(" ").build());

        return buildComponent(messageBase, commandV1, commandV2, messageV1, messageV2, hoverV1, hoverV2,
                prefix, StringColorUtils.MESSAGE_COLOR, StringColorUtils.ACCESS_COLOR_TRUE, StringColorUtils.ACCESS_COLOR_DEFAULT, recipient);
    }

    /**
     * Retrieves the preferred languages for a player.
     *
     * @param recipient the player whose languages are to be retrieved.
     * @return an array of language codes.
     */
    private static String[] getLangs(Player recipient) {
        return recipient != null ? LangUtils.getPlayerLanguage(recipient) : new String[]{manager.getDefaultLang()};
    }
}
