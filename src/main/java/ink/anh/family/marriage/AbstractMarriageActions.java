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

public abstract class AbstractMarriageActions extends Sender {

    protected AnhyFamily familyPlugin;
    protected GlobalManager manager;
    protected MarriageManager marriageManager;
    protected FamilyConfig familyConfig;
    protected String priestTitle = "";
    protected String bride1Title = "";

    public AbstractMarriageActions(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.manager = GlobalManager.getInstance();
        this.marriageManager = GlobalManager.getInstance().getMarriageManager();
        this.familyConfig = manager.getFamilyConfig();
    }

    protected void sendMAnnouncement(Player recipient, String senderName, String messageKey, String messageColor, String[] placeholders, MarryPrefixType prefixType) {
        sendMessageComponent(recipient, MarryComponentBuilder.announcementMessageComponent(recipient, senderName, messageKey, messageColor, placeholders, prefixType));
    }

    protected void sendPriestAcceptMessage(MarryPrefixType prefixType, String senderName, Player recipient) {
        sendMessageComponent(recipient, MarryComponentBuilder.priestAcceptMessageComponent(prefixType, senderName, recipient));
    }

    protected void sendMessageComponent(Player recipient, MessageComponents messageComponents) {
        Messenger.sendMessage(familyPlugin, recipient, messageComponents, "MessageComponents");
    }
    
    protected MarryPrefixType getMarryPrefixType(Gender gender, int memberNum) {
    	return MarryPrefixType.getMarryPrefixType(gender, memberNum);
    }

    /**
     * Виконує вказану дію для кожного гравця в масиві.
     *
     * @param recipients масив гравців, для яких буде виконано дію
     * @param action дія, яку необхідно виконати для кожного гравця
     *
     * <p>Приклад використання:</p>
     * <pre>
     * {@code
     * Player[] players = ...; // Отримуємо масив гравців
     * 
     * PlayerUtils.executeForPlayers(players, player -> {
     *     // Виконуємо потрібну дію для кожного гравця
     *     player.sendMessage("Привіт, " + player.getName() + "!");
     * });
     * }
     * </pre>
     */
    protected static void executeForPlayers(Player[] recipients, Consumer<Player> action) {
        for (Player player : recipients) {
            action.accept(player);
        }
    }
}
