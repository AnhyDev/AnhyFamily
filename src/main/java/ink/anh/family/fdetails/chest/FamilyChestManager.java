package ink.anh.family.fdetails.chest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ink.anh.api.enums.Access;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
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
import ink.anh.family.util.FamilyUtils;

public class FamilyChestManager extends Sender {

    private static Map<UUID, ChestRequest> chestRequests = new HashMap<>();

    private AnhyFamily familyPlugin;
    private Player player;
    private String[] args;

    public FamilyChestManager(AnhyFamily familyPlugin, Player player, String[] args) {
        super(GlobalManager.getInstance());
        this.familyPlugin = familyPlugin;
        this.player = player;
        this.args = args;
    }

    // Метод для відкриття сімейної скрині
    public void openChest() {
        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(player), details -> {
            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            if (details.hasAccessChest(playerFamily)) {
                FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_chest", new String[] {}), MessageType.WARNING, player);
            }
        });
    }

    // Метод для встановлення дозволів доступу до сімейної скрині
    public void setChestAccess() {
        // Порожній метод
    }

    // Метод для прийняття запиту на відкриття скрині
    public void acceptChestRequest() {
        // Порожній метод
    }

    // Метод для відхилення запиту на відкриття скрині
    public void denyChestRequest() {
        // Порожній метод
    }

    // Метод для обробки запитів доступу до сімейної скрині
    public void handleChestRequest() {
        // Порожній метод
    }

    // Метод для відкриття скрині іншої сім'ї
    public void openChestForOtherFamily() {
        if (args.length < 2) {
            sendMessage(new MessageForFormatting("family_err_command_format", new String[] {"/fchest other <prefix>"}), MessageType.WARNING, player);
            return;
        }

        String symbol = args[1].toUpperCase();
        if (symbol.length() < 3 || symbol.length() > 6 || !symbol.matches("[A-Z]+")) {
            sendMessage(new MessageForFormatting("family_err_prefix_not_found", new String[] {}), MessageType.WARNING, player);
            return;
        }

        UUID familyId = FamilySymbolManager.getFamilyIdBySymbol(symbol);
        if (familyId == null) {
            sendMessage(new MessageForFormatting("family_err_symbol_not_found", new String[] {symbol}), MessageType.WARNING, player);
            return;
        }

        executeWithFamilyDetails(FamilyDetailsGet.getRootFamilyDetails(familyId), details -> {
            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            if (details.hasAccessChest(playerFamily)) {
                FamilyChestOpenManager.getInstance().openFamilyChest(player, details);
            } else {
                sendMessage(new MessageForFormatting("family_err_no_access_chest", new String[] {symbol}), MessageType.WARNING, player);
            }
        });
    }

    // Внутрішній клас для запитів доступу до скрині
    private static class ChestRequest {
        private final UUID requesterUUID;

        public ChestRequest(UUID requesterUUID) {
            this.requesterUUID = requesterUUID;
        }

        public UUID getRequesterUUID() {
            return requesterUUID;
        }
    }

    // Загальний метод для виконання дій з сімейними деталями
    private void executeWithFamilyDetails(FamilyDetails details, FamilyDetailsActionInterface action) {
        if (details != null) {
            action.execute(details);
        } else {
            sendMessage(new MessageForFormatting("family_err_details_not_found", new String[] {}), MessageType.WARNING, player);
        }
    }
}
