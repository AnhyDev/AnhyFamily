package ink.anh.family.marriage;

import org.bukkit.entity.Player;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.GlobalManager;
import ink.anh.family.util.OtherComponentBuilder;
import ink.anh.family.util.StringColorUtils;

public class MarryComponentBuilder {

    private static GlobalManager manager = GlobalManager.getInstance();

    private static MessageBuilder prefix(MarryPrefixType prefixType, String playerName, String[] langs) {
    	if (prefixType == null) {
    		prefixType = MarryPrefixType.DEFAULT;
    	} else if (prefixType == MarryPrefixType.PRIVATE_MARRY_PREFIX && (playerName == null || playerName.isEmpty())) {
    		playerName = "family_marry_private_priest";
    	}
    	
    	String content = StringUtils.formatString(Translator.translateKyeWorld(manager, prefixType.getKey(), langs), new String[] {});
    	
    	return MessageComponents.builder()
                .content("[" + content + "] ")
                .hexColor(prefixType.getPrefixColor())
                .decoration("BOLD", true)
                .append(MessageComponents.builder()
                    .content(playerName)
                    .hexColor(prefixType.getNicknameColor())
                    .build())
                .append(MessageComponents.builder()
                    .content(": ")
                    .hexColor(prefixType.getPrefixColor())
                    .build());
    }

    public static MessageComponents announcementMessageComponent(Player recipient, String senderName, String messageKey, String messageColor, String[] placeholders, MarryPrefixType prefixType) {
        String[] langs = getLangs(recipient);
    	
        senderName = translateSenderName(prefixType, senderName, langs);

    	String messageBase = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, messageKey, langs), placeholders));

        return prefix(prefixType, senderName, langs)
                	.append(MessageComponents.builder()
                        .content(messageBase)
                        .hexColor(messageColor)
                        .build())
                    .build();
    }
    
    public static MessageComponents priestAcceptMessageComponent(MarryPrefixType prefixType, String senderName, Player recipient) {
        String[] langs = getLangs(recipient);

        senderName = translateSenderName(prefixType, senderName, langs);

        String messageBase = "family_groom_consent";
        String messageAccept = "family_groom_consent1";
        String messageRefuse = "family_groom_consent2";
        String hoverAccept = "family_groom_hover_consent1";
        String hoverRefuse = "family_groom_hover_consent2";

        String commandAccept = "/marry accept";
        String commandRefuse = "/marry refuse";

        MessageBuilder prefix = prefix(prefixType, senderName, langs)
                .append(MessageComponents.builder().content(" ").build());

        return OtherComponentBuilder.buildComponent(messageBase, commandAccept, commandRefuse, messageAccept, messageRefuse, hoverAccept, hoverRefuse,
                prefix, StringColorUtils.MESSAGE_COLOR, StringColorUtils.ACCESS_COLOR_TRUE, StringColorUtils.ACCESS_COLOR_FALSE, recipient);
    }
    
    private static String[] getLangs(Player recipient) {
    	return recipient != null ? LangUtils.getPlayerLanguage(recipient) : new String[]{manager.getDefaultLang()};
    }
    
    private static String translateSenderName(MarryPrefixType prefixType, String senderName, String[] langs) {
    	if (prefixType == MarryPrefixType.PRIVATE_MARRY_PREFIX) {
    		senderName = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, senderName, langs), new String[] {}));
    	}
    	return senderName;
    }
}
