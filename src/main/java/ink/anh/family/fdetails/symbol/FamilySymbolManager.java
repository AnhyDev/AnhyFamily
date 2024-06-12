package ink.anh.family.fdetails.symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ink.anh.api.messages.MessageChat;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsActionInterface;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;

public class FamilySymbolManager extends Sender {

    private static Map<UUID, SymbolRequest> symbolRequests = new ConcurrentHashMap<>();
    private static Map<String, UUID> symbolMap = new ConcurrentHashMap<>();

    private AnhyFamily familyPlugin;
    private Player player = null;
    private String[] args;

    public FamilySymbolManager(AnhyFamily familyPlugin, CommandSender sender, String[] args) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.setPlayer(sender);
        this.args = args;
    }

    private void setPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            this.player = (Player) sender;
        } else {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
        }
    }
    
    public static void addSymbol(String symbol, UUID familyId) {
        if (symbol != null && familyId != null) {
            symbolMap.put(symbol.toUpperCase(), familyId);
        }
    }

    public void setSymbol() {
        if (player == null) {
            return;
        }

        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/family setpref <prefix>"}), MessageType.WARNING, player);
            return;
        }

        String newSymbol = args[1].toUpperCase();
        if (newSymbol.length() < 3 || newSymbol.length() > 5 || !newSymbol.matches("[A-Z]+")) {
            sendMessage(new MessageForFormatting("family_err_invalid_symbol", new String[] {}), MessageType.WARNING, player);
            return;
        }

        // Перевірка, чи символ вже зайнятий
        if (symbolMap.containsKey(newSymbol)) {
            sendMessage(new MessageForFormatting("family_err_symbol_taken", new String[] {newSymbol}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            if (details.getFamilySymbol() == null || details.getFamilySymbol().length() == 6) {
                PlayerFamily playerFamily = FamilyUtils.getFamily(player);
                UUID spouseUUID = playerFamily.getSpouse();
                if (spouseUUID == null) {
                    sendMessage(new MessageForFormatting("family_err_no_spouse", new String[] {}), MessageType.WARNING, player);
                    return;
                }
                symbolRequests.put(details.getFamilyId(), new SymbolRequest(newSymbol, player.getUniqueId()));
                sendMessage(new MessageForFormatting("family_symbol_request_sent", new String[] {}), MessageType.NORMAL, player);

                // Запуск таймера на 60 секунд
                familyPlugin.getServer().getScheduler().runTaskLater(familyPlugin, () -> {
                    if (symbolRequests.containsKey(details.getFamilyId())) {
                        symbolRequests.remove(details.getFamilyId());
                        sendMessage(new MessageForFormatting("family_err_request_symbol_not_confirmed", new String[] {}), MessageType.WARNING, player);
                    }
                }, 1200L); // 1200 тік дорівнює 60 секунд
            } else {
                sendMessage(new MessageForFormatting("family_err_symbol_already_set", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    public void acceptSymbol() {
        if (player == null) {
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID familyId = details.getFamilyId();
            SymbolRequest request = symbolRequests.get(familyId);
            if (request != null && !request.getRequesterUUID().equals(player.getUniqueId())) {
                String oldSymbol = details.getFamilySymbol();
                String newSymbol = request.getSymbol();

                // Видаляємо старий символ з мапи, якщо він існує
                if (oldSymbol != null) {
                    symbolMap.remove(oldSymbol);
                }

                // Оновлюємо символ у FamilyDetails
                details.setFamilySymbol(newSymbol);
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.FAMILY_SYMBOL);

                // Додаємо новий символ у мапу
                symbolMap.put(newSymbol, familyId);

                symbolRequests.remove(familyId);
                Player[] players = new Player[] {player, familyPlugin.getServer().getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_symbol_set", new String[] {}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    public void getPrefix() {
        if (player == null) {
            return;
        }

        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/family prefix <player>"}), MessageType.WARNING, player);
            return;
        }

        String targetPlayerName = args[1];
        Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
        
        PlayerFamily playerFamily = targetPlayer!= null ? FamilyUtils.getFamily(targetPlayer) : FamilyUtils.getFamily(targetPlayerName);
        
        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[] {targetPlayerName}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(playerFamily), details -> {
            String familySymbol = details.getFamilySymbol();
            if (familySymbol != null) {
            	MessageForFormatting textForFormatting = new MessageForFormatting("family_prefix_info", new String[] {targetPlayerName, familySymbol});
            	MessageForFormatting hoverText = new MessageForFormatting("family_prefix_info_hover", new String[] {familySymbol, targetPlayerName});
            	
            	MessageChat.sendMessageWithCopy(libraryManager, targetPlayer, textForFormatting, hoverText, familySymbol, MessageType.ESPECIALLY, true);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_family_prefix", new String[] {targetPlayerName}), MessageType.WARNING, player);
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

    private static class SymbolRequest {
        private final String symbol;
        private final UUID requesterUUID;

        public SymbolRequest(String symbol, UUID requesterUUID) {
            this.symbol = symbol;
            this.requesterUUID = requesterUUID;
        }

        public String getSymbol() {
            return symbol;
        }

        public UUID getRequesterUUID() {
            return requesterUUID;
        }
    }

    public static UUID getFamilyIdBySymbol(String symbol) {
        return symbolMap.get(symbol.toUpperCase());
    }

    public static void setFamilyIdBySymbolMap(Map<String, UUID> newMap) {
    	symbolMap.clear();
    	symbolMap.putAll(newMap);
    }

    public static List<String> getAllFamilySymbols() {
        return new ArrayList<>(symbolMap.keySet());
    }
}
