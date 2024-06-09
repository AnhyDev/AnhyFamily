package ink.anh.family.fdetails.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Messenger;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsActionInterface;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.info.FamilyTree;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.OtherUtils;

public class FamilyChatManager extends Sender {
	
    private AnhyFamily familiPlugin;

    private Player player;
    private String[] args;

    public FamilyChatManager(AnhyFamily familiPlugin, Player player, String[] args) {
        super(GlobalManager.getInstance());
        this.familiPlugin = familiPlugin;
        this.player = player;
        this.args = args;
    }

    public void sendMessageWithConditions() {

        String firstArg = args[0];
        String message = StringUtils.colorize(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));

        if (firstArg.startsWith("#")) {
            handleSymbolMessage(firstArg.substring(1).toUpperCase(), message);
        } else if (firstArg.startsWith("@")) {
            handleNicknameMessage(firstArg.substring(1), message);
        } else {
            handleDefaultMessage(message);
        }
    }

    private void handleSymbolMessage(String symbol, String message) {
        if (symbol.length() >= 3 && symbol.length() <= 6 && symbol.matches("[A-Z]+")) {
            UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(symbol);
            if (familyId != null) {
                sendMessageToFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(familyId), message);
                return;
            }
        }
        sendMessage(new MessageForFormatting("family_err_prefix_not_found", new String[] {symbol}), MessageType.WARNING, player);
    }

    private void handleNicknameMessage(String nickname, String message) {
        Player targetPlayer = Bukkit.getOnlinePlayers().stream()
            .filter(p -> p.getName().equalsIgnoreCase(nickname))
            .findFirst()
            .orElse(null);

        if (targetPlayer != null) {
            PlayerFamily targetFamily = FamilyUtils.getFamily(targetPlayer);
            if (targetFamily != null) {
                UUID familyId = targetFamily.getFamilyId();
                if (familyId != null) {
                    sendMessageToFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(familyId), message);
                }
            }
        } else {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[] {nickname}), MessageType.WARNING, player);
        }
    }

    private void handleDefaultMessage(String message) {
        sendMessageToFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), message);
    }

    private void sendMessageToFamilyDetails(FamilyDetails details, String message) {
        if (details != null) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerFamily onlinePlayerFamily = FamilyUtils.getFamily(onlinePlayer);
                if (onlinePlayerFamily != null && details.hasAccessChat(onlinePlayerFamily)) {
                    sendInteractiveMessageToPlayer(onlinePlayer, details, message);
                }
            }
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
        }
    }

    public void setChatAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchat access <NickName> <allow|deny|default>"}), MessageType.WARNING, player);
            return;
        }
        String nickname = args[1];
        String accessArg = args[2].toLowerCase();
        Access access;
        switch (accessArg) {
            case "allow":
                access = Access.TRUE;
                break;
            case "deny":
                access = Access.FALSE;
                break;
            case "default":
                access = Access.DEFAULT;
                break;
            default:
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[] {accessArg}), MessageType.WARNING, player);
                return;
        }

        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[] {nickname}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID targetUUID = targetFamily.getRoot();
            Map<UUID, AccessControl> childrenAccessMap = details.getChildrenAccessMap();
            Map<UUID, AccessControl> ancestorsAccessMap = details.getAncestorsAccessMap();
            AccessControl accessControl = null;

            if (childrenAccessMap.containsKey(targetUUID)) {
                accessControl = childrenAccessMap.get(targetUUID);
            } else if (ancestorsAccessMap.containsKey(targetUUID)) {
                accessControl = ancestorsAccessMap.get(targetUUID);
            }

            if (accessControl != null) {
                accessControl.setChatAccess(access);
                if (childrenAccessMap.containsKey(targetUUID)) {
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS_MAP);
                } else {
                    FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS_MAP);
                }
                sendMessage(new MessageForFormatting("family_chat_access_set", new String[] {nickname, accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_nickname_not_found_in_access_maps", new String[] {nickname}), MessageType.WARNING, player);
            }
        });
    }

    public void setDefaultChatAccess() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchat default <children|parents> <allow|deny>"}), MessageType.WARNING, player);
            return;
        }

        String targetGroup = args[1].toLowerCase();
        String accessArg = args[2].toLowerCase();
        Access access;
        switch (accessArg) {
            case "allow":
                access = Access.TRUE;
                break;
            case "deny":
                access = Access.FALSE;
                break;
            default:
                sendMessage(new MessageForFormatting("family_err_invalid_access", new String[] {accessArg}), MessageType.WARNING, player);
                return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            if ("children".equals(targetGroup)) {
                details.getChildrenAccess().setChatAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.CHILDREN_ACCESS);
                sendMessage(new MessageForFormatting("family_default_chat_access_set", new String[] {"children", accessArg}), MessageType.NORMAL, player);
            } else if ("parents".equals(targetGroup)) {
                details.getAncestorsAccess().setChatAccess(access);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.ANCESTORS_ACCESS);
                sendMessage(new MessageForFormatting("family_default_chat_access_set", new String[] {"parents", accessArg}), MessageType.NORMAL, player);
            } else {
                sendMessage(new MessageForFormatting("family_err_invalid_group", new String[] {targetGroup}), MessageType.WARNING, player);
            }
        });
    }

    private void executeWithFamilyDetails(FamilyDetails details, FamilyDetailsActionInterface action) {
        if (details != null) {
            action.execute(details);
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
        }
    }
    
    public MessageComponents buildInteractiveMessage(FamilyDetails details, String message, Player recepient) {
        String symbol = details.getFamilySymbol();
        String playerDisplayName = player.getDisplayName();
        String playerName = player.getName();
        String commandBase = "/fchat #" + symbol + " ";
        
        // Врахування кольорових кодів з рядка
        String symbolColor = "#0bdebb";
        String arrowColor = "#8a690f";
        String treeColor = "#228B22";
        String playerNameColor = "#FFFF00";
        String messageColor = "#f54900";
        
        String[] langs = player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};
        
        String familyTree = player.isOnline() ? new FamilyTree(FamilyUtils.getFamily(player)).buildFamilyTreeString() : "family_hover_player_offline";
        String hoverCopyMessageKey = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_copy_message", langs), new String[] {symbol});
        String hoverReplyChatKey = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_reply_chat", langs), new String[] {});
        String hoverFamilyTree = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, familyTree, langs), new String[] {});
        String hoverPlayerNameKey = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, "family_hover_player_reply", langs), new String[] {});

        return MessageComponents.builder()
            .content("[" + symbol + "]")
            .hexColor(symbolColor)
            .hoverMessage(hoverCopyMessageKey)
            .clickActionCopy(symbol)
            .append(MessageComponents.builder()
                .content(" ➤")
                .hexColor(arrowColor)
                .hoverMessage(hoverReplyChatKey)
                .clickActionRunCommand(commandBase)
                .build())
            .append(MessageComponents.builder()
                .content(" ♣")
                .hexColor(treeColor)
                .hoverMessage(hoverFamilyTree)
                .build())
            .append(MessageComponents.builder()
                .content(playerDisplayName)
                .hexColor(playerNameColor)
                .hoverMessage(hoverPlayerNameKey)
                .clickActionRunCommand(commandBase + "@" + playerName)
                .build())
            .append(MessageComponents.builder()
                .content(": " + StringUtils.colorize(message))
                .hexColor(messageColor)
                .build())
            .build();
    }

    private void sendInteractiveMessageToPlayer(Player recipient, FamilyDetails details, String message) {
    	SyncExecutor.runSync(() -> OtherUtils.notifyPlayerOnMention(recipient, args));
    	MessageComponents messageComponents = buildInteractiveMessage(details, message, recipient);
    	
    	Messenger.sendMessage(familiPlugin, recipient, messageComponents, message);
    }
}
