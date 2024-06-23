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

    private static String[] prefixColor(String prefixType) {
        switch (prefixType) {
            case "priest_male":
                return new String[] {"#000080", "#ADD8E6"}; // Navy for prefix, Light Blue for nickname
            case "priest_female":
                return new String[] {"#800080", "#FF69B4"}; // Purple for prefix, Hot Pink for nickname
            case "priest_non_binary":
                return new String[] {"#808080", "#32CD32"}; // Gray for prefix, Lime Green for nickname
            case "bride_male":
                return new String[] {"#006400", "#00FF00"}; // Dark Green for prefix, Green for nickname
            case "bride_female":
                return new String[] {"#FFC0CB", "#FF1493"}; // Pink for prefix, Deep Pink for nickname
            case "bride_non_binary":
                return new String[] {"#40E0D0", "#20B2AA"}; // Turquoise for prefix, Light Sea Green for nickname
            default:
                return new String[] {"#FFA500", "#FFD700"}; // Orange for prefix, Gold for nickname
        }
    }

    private static String prefixKey(String prefixType) {
        switch (prefixType) {
            case "priest_male":
                return "family_marry_priest_male";
            case "priest_female":
                return "family_marry_priest_female";
            case "priest_non_binary":
                return "family_marry_priest_nonbinary";
            case "bride_male":
                return "family_marry_groom_male";
            case "bride_female":
                return "family_marry_groom_female";
            case "bride_non_binary":
                return "family_marry_groom_nonbinary";
            default:
                return "";
        } 
    }

    private static MessageBuilder prefix(String prefixType, String playerName, String[] langs) {    	
    	String content = StringUtils.formatString(Translator.translateKyeWorld(manager, prefixKey(prefixType), langs), new String[] {});
    	
    	return MessageComponents.builder()
                .content("[" + content + "] ")
                .hexColor(prefixColor(prefixType)[0])
                .decoration("BOLD", true)
                .append(MessageComponents.builder()
                    .content(playerName)
                    .hexColor(prefixColor(prefixType)[1])
                    .build())
                .append(MessageComponents.builder()
                    .content(": ")
                    .hexColor(prefixColor(prefixType)[0])
                    .build());
    }
    
    
    
    
    
    
    
    public static MessageComponents priestAcceptMessageComponent(Player player, String nickname, String prefixType) {
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{manager.getDefaultLang()};
        
    	String messageBase = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent", langs), new String[]{nickname}));
    	String messageAccept = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent1", langs), new String[]{nickname}));
    	String messageRefuse = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent2", langs), new String[]{nickname}));
    	
    	String hoverAccept = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent2", langs), new String[]{nickname}));
    	String hoverRefuse = StringUtils.colorize(StringUtils.formatString(Translator.translateKyeWorld(manager, "family_groom_consent2", langs), new String[]{nickname}));

        String commandAccept = "/marry accept";
        String commandRefuse = "/marry refuse";

        return prefix(prefixType, nickname, langs)
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
}
