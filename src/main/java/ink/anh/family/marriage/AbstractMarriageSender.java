package ink.anh.family.marriage;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.FamilyConfig;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.gender.Gender;

public abstract class AbstractMarriageSender extends Sender {

    protected AnhyFamily familyPlugin;
    protected GlobalManager manager;
    protected MarriageManager marriageManager;
    protected FamilyConfig familyConfig;
    protected MarryPrefixType priestPrefixType = MarryPrefixType.DEFAULT;
    protected MarryPrefixType bridePrefixType = MarryPrefixType.DEFAULT;
    protected MarryPrefixType bridxPrefixType = MarryPrefixType.DEFAULT;

    public AbstractMarriageSender(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.manager = GlobalManager.getInstance();
        this.marriageManager = GlobalManager.getInstance().getMarriageManager();
        this.familyConfig = manager.getFamilyConfig();
    }

    protected void sendMAnnouncement(MarryPrefixType prefixType, String senderName, String messageKey, String messageColor, String[] placeholders, Player[] recipients) {
        executeForPlayers(recipients, recipient -> {
            sendMessageComponent(recipient, MarryComponentBuilder.announcementMessageComponent(recipient, senderName, messageKey, messageColor, placeholders, prefixType));
        });
    }

    protected void sendPriestAcceptMessage(MarryPrefixType prefixType, String senderName, Player[] recipients) {
        executeForPlayers(recipients, recipient -> {
            sendMessageComponent(recipient, MarryComponentBuilder.priestAcceptMessageComponent(prefixType, senderName, recipient));
        });
    }

    protected void sendMessageComponent(Player recipient, MessageComponents messageComponents) {
        Messenger.sendMessage(familyPlugin, recipient, messageComponents, "MessageComponents");
    }
    
    protected MarryPrefixType getMarryPrefixType(Gender gender, int memberNum) {
    	return MarryPrefixType.getMarryPrefixType(gender, memberNum);
    }

    protected static void executeForPlayers(Player[] recipients, Consumer<Player> action) {
        for (Player player : recipients) {
            action.accept(player);
        }
    }
}
