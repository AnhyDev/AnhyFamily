package ink.anh.family.fdetails.chat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.AbstractDetailsManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.info.FamilyTree;
import ink.anh.family.util.TypeTargetComponent;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.StringColorUtils;

public class FamilyChatManager extends AbstractDetailsManager {

    public FamilyChatManager(AnhyFamily familyPlugin, Player player, Command cmd, String[] args) {
        super(familyPlugin, player, cmd, args);
    }

    @Override
    protected String getDefaultCommand() {
        return "fchat";
    }

    @Override
    protected String getInvalidAccessMessage() {
        return "family_err_no_access_chat";
    }

    @Override
    protected String getComponentAccessSetMessageKey(TypeTargetComponent component) {
        return "family_chat_access_set";
    }

    @Override
    protected String getDefaultAccessSetMessageKey(TypeTargetComponent component) {
        return "family_default_chat_access_set";
    }

    @Override
    protected String getDefaultAccessCheckMessageKey(TypeTargetComponent component) {
        return "family_default_chat_access_check";
    }

    @Override
    protected boolean canPerformAction(FamilyDetails details, Object additionalParameter) {
        return true;
    }

    @Override
    protected TypeTargetComponent getTypeTargetComponent() {
        return TypeTargetComponent.CHAT;
    }

    @Override
    protected void setComponentAccess(AccessControl accessControl, Access access, TypeTargetComponent component) {
        accessControl.setChatAccess(access);
    }

    @Override
    protected void performAction(FamilyDetails details) {
        sendMessageToFamilyDetails(details, StringUtils.colorize(String.join(" ", args)));
    }

    public void sendMessageWithConditions() {
        handleActionWithConditions();
    }

    private void sendMessageToFamilyDetails(FamilyDetails details, String message) {
        if (details != null) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerFamily onlinePlayerFamily = FamilyUtils.getFamily(onlinePlayer);
                if (onlinePlayerFamily != null && details.hasAccess(onlinePlayerFamily, TypeTargetComponent.CHAT)) {
                    sendInteractiveMessageToPlayer(onlinePlayer, details, message);
                }
            }
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
        }
    }

    public void setChatAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchat access <NickName> <allow|deny|default>"}), MessageType.WARNING, player);
            return;
        }
        setAccess(args[1], args[2], TypeTargetComponent.CHAT);
    }

    public void setDefaultChatAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchat default <children|parents> <allow|deny>"}), MessageType.WARNING, player);
            return;
        }
        setDefaultAccess(args[1], args[2], TypeTargetComponent.CHAT);
    }

    public void checkAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchat check <NickName>"}), MessageType.WARNING, player);
            return;
        }
        checkAccess(args[1], TypeTargetComponent.CHAT);
    }

    public void checkDefaultAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{"/fchat defaultcheck <children|parents>"}), MessageType.WARNING, player);
            return;
        }
        checkDefaultAccess(args[1], TypeTargetComponent.CHAT);
    }

    private MessageComponents buildInteractiveMessage(FamilyDetails details, String message, Player recipient) {
        String symbol = details.getFamilySymbol();
        String playerName = player.getName();
        String commandBase = "/fchat #" + symbol + " ";
        
        String symbolColor = "#0bdebb";
        String arrowColor = "#8a690f";
        String treeColor = "#228B22";
        String playerNameColor = "#fac32a";
        String messageColor = "#2ab1fa";
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};
        
        String familyTree = player.isOnline() ? new FamilyTree(FamilyUtils.getFamily(player)).buildFamilyTreeString() : "family_hover_player_offline";
        String hoverCopyMessageKey = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_copy_message", langs), new String[] {symbol});
        String hoverReplyChatKey = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_reply_chat", langs), new String[] {});
        String hoverFamilyTree = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, familyTree, langs), new String[] {});
        String hoverPlayerNameKey = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_player_reply", langs), new String[] {playerName});

        return MessageComponents.builder()
            .content("[" + symbol + "]")
            .hexColor(symbolColor)
            .hoverMessage(hoverCopyMessageKey)
            .clickActionCopy(symbol)
            .append(MessageComponents.builder()
                .content(" ♣ ")
                .hexColor(treeColor)
                .hoverMessage(hoverFamilyTree)
                .build())
            .append(MessageComponents.builder()
                .content(playerName)
                .hexColor(playerNameColor)
                .hoverMessage(hoverPlayerNameKey)
                .insertTextChat(commandBase + "@" + playerName)
                .build())
            .append(MessageComponents.builder()
                .content(" ➡ ")
                .hexColor(arrowColor)
                .hoverMessage(hoverReplyChatKey)
                .insertTextChat(commandBase)
                .build())
            .append(MessageComponents.builder()
                .content(message)
                .hexColor(messageColor)
                .build())
            .build();
    }

    private void sendInteractiveMessageToPlayer(Player recipient, FamilyDetails details, String message) {
        SyncExecutor.runSync(() -> {
        	notifyPlayerOnMention(recipient, args);
        });
        MessageComponents messageComponents = buildInteractiveMessage(details, message, recipient);
        Messenger.sendMessage(familyPlugin, recipient, messageComponents, message);
    }

    private void notifyPlayerOnMention(Player target, String[] args) {
		boolean notify = false;
        for (String arg : args) {
            if (arg.charAt(0) == '@') {
                String playerName = arg.substring(1);
                if (target != null && target.isOnline() &&
                	(target.getName().equalsIgnoreCase(playerName) || 
                    (target.getDisplayName() != null && target.getDisplayName().equalsIgnoreCase(playerName)))) {
                	//target.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.1f, 0.8f);
                	target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                    sendActionBarMessage(target, new MessageForFormatting("family_notify_family_chat", new String[]{player.getName()}), StringColorUtils.PREFIX_CHAT_COLOR);

                    Location particleLocation = target.getLocation().add(0, 1.6, 0);
                    target.spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 8, 0.4, 0.4, 0.4, 0.08);
                    notify = true;
                    break;
                }
            }
        }
        if (!notify) {
        	target.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.8f);
        }
    }
}
