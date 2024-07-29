package ink.anh.family.fdetails.symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageChat;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.family.AnhyFamily;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.AbstractDetailsManager;
import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.FDetailsComponentBuilder;
import ink.anh.family.fplayer.FamilyUtils;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.TypeTargetComponent;

public class FamilySymbolManager extends AbstractDetailsManager {

    private static Map<UUID, SymbolRequest> symbolRequests = new ConcurrentHashMap<>();
    private static Map<String, UUID> symbolMap = new ConcurrentHashMap<>();

    public FamilySymbolManager(AnhyFamily familyPlugin, Player player, Command cmd, String[] args) {
        super(familyPlugin, player, cmd, args);
    }

    @Override
    protected String getDefaultCommand() {
        return "fprefix";
    }

    protected void handleSymbolRequest(FamilyDetails details, String newSymbol) {
        if (details.getFamilySymbol() != null && details.getFamilySymbol().length() >= 6) {
            sendMessage(new MessageForFormatting("family_err_symbol_already_set", new String[] {}), MessageType.WARNING, player);
            return;
        }
        
        PlayerFamily playerFamily = FamilyUtils.getFamily(player);
        UUID spouseUUID = playerFamily.getSpouse();
        if (spouseUUID == null) {
            sendMessage(new MessageForFormatting("family_err_no_spouse", new String[]{}), MessageType.WARNING, player);
            return;
        }

        symbolRequests.put(details.getFamilyId(), new SymbolRequest(newSymbol, player.getUniqueId()));
        
        Player spouse = Bukkit.getPlayer(spouseUUID);
        if (spouse != null && spouse.isOnline()) {
            MessageComponents messageComponents = FDetailsComponentBuilder.acceptMessageComponent("family_symbol_request_sent", command, "accept", "refuse", spouse);
            sendMessageComponent(player, messageComponents);
        }

        familyPlugin.getServer().getScheduler().runTaskLater(familyPlugin, () -> {
            if (symbolRequests.containsKey(details.getFamilyId())) {
                symbolRequests.remove(details.getFamilyId());
                sendMessage(new MessageForFormatting("family_err_request_symbol_not_confirmed", new String[] {}), MessageType.WARNING, player);
            }
        }, 1200L);
    }

    protected void handleSymbolAccept(FamilyDetails details) {
        UUID familyId = details.getFamilyId();
        SymbolRequest request = symbolRequests.get(familyId);
        if (request != null && !request.getRequesterUUID().equals(player.getUniqueId())) {
            String oldSymbol = details.getFamilySymbol();
            String newSymbol = request.getSymbol();

            if (oldSymbol != null) {
                symbolMap.remove(oldSymbol);
            }

            details.setFamilySymbol(newSymbol);
            FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.FAMILY_SYMBOL);

            symbolMap.put(newSymbol, familyId);

            symbolRequests.remove(familyId);
            Player[] players = new Player[] {player, familyPlugin.getServer().getPlayer(request.getRequesterUUID())};
            sendMessage(new MessageForFormatting("family_symbol_set", new String[] {}), MessageType.NORMAL, players);
        } else {
            sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[] {}), MessageType.WARNING, player);
        }
    }

    public static void addSymbol(String symbol, UUID familyId) {
        if (symbol != null && familyId != null) {
            symbolMap.put(symbol.toUpperCase(), familyId);
        }
    }

    public void setSymbol() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[]{getCommandUsage()}), MessageType.WARNING, player);
            return;
        }

        String newSymbol = args[1].toUpperCase();
        if (newSymbol.length() < 3 || newSymbol.length() > 5 || !newSymbol.matches("[A-Z]+")) {
            sendMessage(new MessageForFormatting("family_err_invalid_symbol", new String[] {}), MessageType.WARNING, player);
            return;
        }

        if (symbolMap.containsKey(newSymbol)) {
            sendMessage(new MessageForFormatting("family_err_symbol_taken", new String[] {newSymbol}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> handleSymbolRequest(details, newSymbol));
    }

    public void acceptSymbol() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), this::handleSymbolAccept);
    }

    public void rejectSymbolRequest() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            UUID familyId = details.getFamilyId();
            SymbolRequest request = symbolRequests.get(familyId);
            if (request != null && !request.getRequesterUUID().equals(player.getUniqueId())) {
                symbolRequests.remove(familyId);
                Player[] players = new Player[]{player, familyPlugin.getServer().getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_request_rejected", new String[]{}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[]{}), MessageType.WARNING, player);
            }
        });
    }

    public void getPrefix() {
        String targetPlayerName = player.getName();
        Player targetPlayer = player;

        if (args.length > 1) {
            String firstArg = args[1];
            if (firstArg.startsWith("@")) {
                targetPlayerName = firstArg.substring(1);
                targetPlayer = Bukkit.getPlayerExact(targetPlayerName);
            } else {
                sendMessage(new MessageForFormatting("family_err_command_format", new String[]{getCommandUsage()}), MessageType.WARNING, player);
                return;
            }
        }

        PlayerFamily playerFamily = targetPlayer != null ? FamilyUtils.getFamily(targetPlayer) : FamilyUtils.getFamily(targetPlayerName);

        if (playerFamily == null) {
            sendMessage(new MessageForFormatting("family_err_nickname_not_found", new String[] {targetPlayerName}), MessageType.WARNING, player);
            return;
        }

        final String playerName = targetPlayerName;
        final Player finalPlayer = targetPlayer;
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(playerFamily), details -> {
            String familySymbol = details.getFamilySymbol();
            if (familySymbol != null) {
                MessageForFormatting textForFormatting = new MessageForFormatting("family_prefix_info", new String[] {playerName, familySymbol});
                MessageForFormatting hoverText = new MessageForFormatting("family_prefix_info_hover", new String[] {familySymbol, playerName});

                MessageChat.sendMessageWithCopy(libraryManager, finalPlayer, textForFormatting, hoverText, familySymbol, MessageType.ESPECIALLY, true);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_family_prefix", new String[] {playerName}), MessageType.WARNING, player);
            }
        });
    }

    private String getCommandUsage() {
        return "\n| /fprefix \n| /fprefix @<NickName> \n| /fprefix set <PREFIX> \n| /fprefix accept \n| /fprefix refuse";
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

    public static void removeSymbol(String symbol) {
        if (symbol != null) {
            symbolMap.remove(symbol.toUpperCase());
        }
    }

    @Override
    protected String getInvalidAccessMessage() {
    	// Не використовується
        return "family_err_no_access_symbol";
    }

    @Override
    protected String getComponentAccessSetMessageKey(TypeTargetComponent component) {
    	// Не використовується
        return "family_symbol_access_set";
    }

    @Override
    protected String getDefaultAccessSetMessageKey(TypeTargetComponent component) {
    	// Не використовується
        return "family_default_symbol_access_set";
    }

    @Override
    protected String getDefaultAccessCheckMessageKey(TypeTargetComponent component) {
    	// Не використовується
        return "family_default_symbol_access_check";
    }

    @Override
    protected TypeTargetComponent getTypeTargetComponent() {
        return TypeTargetComponent.SYMBOL;
    }

    @Override
    protected void setComponentAccess(AccessControl accessControl, Access access, TypeTargetComponent component) {
        // реалізація не потрібна для символів
    }

    @Override
    protected void performAction(FamilyDetails details) {
        // реалізація не потрібна для символів
    }
}
