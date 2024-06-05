package ink.anh.family.fdetails.symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    private static Map<UUID, SymbolRequest> symbolRequests = new HashMap<>();

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
    	if (sender instanceof Player ) {
    		this.player = (Player) sender;
    	} else {
            sendMessage(new MessageForFormatting("family_err_command_only_player", new String[]{}), MessageType.WARNING, sender);
    	}
    }
    
    public void setSymbol() {
    	if (player == null) {
    		return;
    	}
    	
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/family symbolset <symbol>"}), MessageType.WARNING, player);
            return;
        }

        String newSymbol = args[1].toUpperCase();
        if (newSymbol.length() < 3 || newSymbol.length() > 5 || !newSymbol.matches("[A-Z]+")) {
            sendMessage(new MessageForFormatting("family_err_invalid_symbol", new String[] {}), MessageType.WARNING, player);
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
                        sendMessage(new MessageForFormatting("family_err_request_not_confirmed", new String[] {}), MessageType.WARNING, player);
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
                details.setFamilySymbol(request.getSymbol());
                FamilyDetailsSave.saveFamilyDetails(details, FamilyDetailsField.FAMILY_SYMBOL);
                symbolRequests.remove(familyId);
                Player[] players = new Player[] {player, familyPlugin.getServer().getPlayer(request.getRequesterUUID())};
                sendMessage(new MessageForFormatting("family_symbol_set", new String[] {}), MessageType.NORMAL, players);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_pending_request", new String[] {}), MessageType.WARNING, player);
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
}
