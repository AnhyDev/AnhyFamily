package ink.anh.family.marriage;

import org.bukkit.entity.Player;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.GlobalManager;
import ink.anh.family.util.StringColorUtils;

public class MarryComponentBuilder {

    private static GlobalManager manager = GlobalManager.getInstance();

    private static MessageBuilder prefix(MarryPrefixType prefixType, String playerName, String[] langs) {
    	if (prefixType == null) {
    		prefixType = MarryPrefixType.DEFAULT;
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
        
    	String messageBase = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent", langs), new String[]{recipient.getName()}));
    	String messageAccept = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent1", langs), new String[]{}));
    	String messageRefuse = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent2", langs), new String[]{}));
    	
    	String hoverAccept = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent2", langs), new String[]{}));
    	String hoverRefuse = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent2", langs), new String[]{}));

        String commandAccept = "/marry accept";
        String commandRefuse = "/marry refuse";

        return prefix(prefixType, senderName, langs)
                	.append(MessageComponents.builder()
                        .content(messageBase)
                        .hexColor(StringColorUtils.MESSAGE_COLOR)
                        .build())
                    .append(MessageComponents.builder()
                        .content(messageAccept)
                        .hexColor(StringColorUtils.ACCESS_COLOR_TRUE)
                        .hoverComponent(MessageComponents.builder().content(hoverAccept).hexColor(StringColorUtils.ACCESS_COLOR_TRUE).build())
                        .clickActionRunCommand(commandAccept)
                        .build())
                    .append(MessageComponents.builder()
                        .content(" | ")
                        .hexColor(StringColorUtils.SEPARATOR_COLOR)
                        .build())
                    .append(MessageComponents.builder()
                        .content(messageRefuse)
                        .hexColor(StringColorUtils.ACCESS_COLOR_FALSE)
                        .hoverComponent(MessageComponents.builder().content(hoverRefuse).hexColor(StringColorUtils.ACCESS_COLOR_TRUE).build())
                        .clickActionRunCommand(commandRefuse)
                        .build())
                    .build();
    }
    
    private static String[] getLangs(Player recipient) {
    	return recipient != null ? LangUtils.getPlayerLanguage(recipient) : new String[]{manager.getDefaultLang()};
    }
}
