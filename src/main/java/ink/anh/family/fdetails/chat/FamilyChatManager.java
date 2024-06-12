package ink.anh.family.fdetails.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
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

        if (firstArg.startsWith("#")) {
        	handleChatMessage(firstArg.substring(1).toUpperCase(), 1);
        } else if (firstArg.startsWith("@")) {
        	handleChatMessage(firstArg.substring(1), 2);
        } else {
        	handleChatMessage(null, 0);
        }
    }

    private void handleChatMessage(String key, int typeKey) {
        FamilyDetails familyDetails = null;
        String lowerCaseKey = key.toLowerCase();

        String message = StringUtils.colorize(String.join(" ", (typeKey > 0 ? Arrays.copyOfRange(args, 1, args.length) : args)));
        if (message == null || message.isEmpty()) {
            sendMessage(new MessageForFormatting("family_err_command_format", 
            		new String[] {"/fchat access <args> | /fchat default <args> | /fchat <message> | /fchat #<RPEFIX> <message> | /fchat @<NickName> <message> | /fchat check <NickName>"}), MessageType.WARNING, player);
            return;
        }
        
        switch (typeKey) {
            case 0:
                // Відправка повідомлення сім'ї гравця
                familyDetails = FamilyDetailsGet.getRootFamilyDetails(player);
                break;
            case 1:
                Logger.info(AnhyFamily.getInstance(), "Відправка повідомлення сім'ї за символом сім'ї");
                // Відправка повідомлення сім'ї за символом сім'ї
                UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(key);
                Logger.info(AnhyFamily.getInstance(), "UUID familyId = " + familyId);
                if (familyId != null) {
                    familyDetails = FamilyDetailsGet.getRootFamilyDetails(familyId);
                    Logger.info(AnhyFamily.getInstance(), "FamilySymbol = " + familyDetails.getFamilySymbol());
                } else {
                    sendMessage(new MessageForFormatting("family_err_prefix_not_found", new String[]{key}), MessageType.WARNING, player);
                    return;
                }
                break;
            case 2:
                // Відправка повідомлення сім'ї за іменем або відображуваним іменем гравця без врахування регістру
                Player targetPlayer = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getName().toLowerCase().equals(lowerCaseKey) || p.getDisplayName().toLowerCase().equals(lowerCaseKey))
                        .findFirst()
                        .orElse(null);

                if (targetPlayer == null) {
                    sendMessage(new MessageForFormatting("family_hover_player_offline", new String[]{key}), MessageType.WARNING, player);
                    return;
                }
                familyDetails = FamilyDetailsGet.getRootFamilyDetails(targetPlayer);
                break;
            default:
                // Невідомий тип ключа
                sendMessage(new MessageForFormatting("family_err_invalid_typekey", new String[]{String.valueOf(typeKey)}), MessageType.WARNING, player);
                return;
        }

        // Відправка повідомлення, якщо знайдено сімейні деталі
        if (familyDetails != null) {
            sendMessageToFamilyDetails(familyDetails, message);
        }
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
        	if (targetFamily.getFamilyId() != null && targetFamily.getFamilyId().equals(details.getFamilyId())) {
                sendMessage(new MessageForFormatting("family_access_root", new String[] {nickname, details.getFamilySymbol()}), MessageType.NORMAL, player);
                return;
        	}
        	
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

    public void checkAccess() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchat check <NickName>"}), MessageType.WARNING, player);
            return;
        }
        String nickname = args[1];

        PlayerFamily targetFamily = FamilyUtils.getFamily(nickname);
        if (targetFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[] {nickname}), MessageType.WARNING, player);
            return;
        }


        PlayerFamily senderFamily = FamilyUtils.getFamily(player);
        if (senderFamily != null) {
        	
        	executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(senderFamily), details -> {
        		boolean accessControl = details.hasAccessChat(targetFamily);
                sendMessage(new MessageForFormatting("family_access_get", new String[] {nickname, String.valueOf(accessControl)}), MessageType.WARNING, player);
        	});
        }
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
        String playerName = player.getName();
        String commandBase = "/fchat #" + symbol + " ";
        
        // Врахування кольорових кодів з рядка
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
    	SyncExecutor.runSync(() -> OtherUtils.notifyPlayerOnMention(recipient, args));
    	MessageComponents messageComponents = buildInteractiveMessage(details, message, recipient);
    	
    	Messenger.sendMessage(familiPlugin, recipient, messageComponents, message);
    }
}
