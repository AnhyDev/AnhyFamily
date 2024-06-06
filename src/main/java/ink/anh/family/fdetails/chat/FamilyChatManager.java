package ink.anh.family.fdetails.chat;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.api.utils.StringUtils;
import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsActionInterface;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;

public class FamilyChatManager extends Sender {

    private Player player;
    private String[] args;

    public FamilyChatManager(Player player, String[] args) {
        super(GlobalManager.getInstance());
        this.player = player;
        this.args = args;
    }

    public void sendMessage() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchat <message>"}), MessageType.WARNING, player);
            return;
        }

        // Об'єднання аргументів у повідомлення та фарбування тексту
        String message = StringUtils.colorize(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));

        // Отримання сімейних даних та виконання дії
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            // Формування префіксу для повідомлення
            String prefix = StringUtils.colorize("#0bdebb\"[" + details.getFamilySymbol() + "] #228B22" + "♣ #FFFF00" + player.getDisplayName() + "#0bdebb\": #f54900");
            
            // Перевірка всіх онлайн гравців на доступ до сімейного чату
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerFamily onlinePlayerFamily = FamilyUtils.getFamily(onlinePlayer);
                
                if (onlinePlayerFamily != null && details.hasAccessChat(onlinePlayerFamily)) {
                    // Відправка повідомлення у головному потоці
                    SyncExecutor.runSync(() -> onlinePlayer.sendMessage(prefix + message));
                }
            }
        });
    }

    public void sendMessageToOtherFamily() {
        if (args.length < 3) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchat other <prefix> <message>"}), MessageType.WARNING, player);
            return;
        }

        String symbol = args[1].toUpperCase();
        if (symbol.length() < 3 || symbol.length() > 6 || !symbol.matches("[A-Z]+")) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchat other <prefix> <message>"}), MessageType.WARNING, player);
            return;
        }

        UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(symbol);
        if (familyId == null) {
            sendMessage(new MessageForFormatting("family_err_symbol_not_found", new String[] {symbol}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(familyId), details -> {
            String message = StringUtils.colorize(String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
            String prefix = StringUtils.colorize("#0bdebb\"[" + details.getFamilySymbol() + "] #228B22" + "♣ #FFFF00" + player.getDisplayName() + "#0bdebb\": #f54900");

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                PlayerFamily onlinePlayerFamily = FamilyUtils.getFamily(onlinePlayer);

                if (onlinePlayerFamily != null && details.hasAccessChat(onlinePlayerFamily)) {
                    SyncExecutor.runSync(() -> onlinePlayer.sendMessage(prefix + message));
                }
            }
        });
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

    public void executeWithFamilyDetails(FamilyDetails details, FamilyDetailsActionInterface action) {
        if (details != null) {
            action.execute(details);
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
        }
    }
}
