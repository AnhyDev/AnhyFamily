package ink.anh.family.fdetails.chest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ink.anh.api.lingo.Translator;
import ink.anh.api.utils.LangUtils;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;

public class FamilyChestManager {

    private static final FamilyChestManager instance = new FamilyChestManager();
    private final Map<UUID, String> chestMap;

    private FamilyChestManager() {
        chestMap = new ConcurrentHashMap<>();
    }

    public static FamilyChestManager getInstance() {
        return instance;
    }

 // Метод для відкриття FamilyChest інвентаря
    public void openFamilyChest(Player player) {
        FamilyDetails details = FamilyDetailsGet.getRootFamilyDetails(player);
        
        if (details == null) {
        	return;
        }
        
        UUID familyId = details.getFamilyId();
        
        if (hasChest(familyId)) {
            String viewerName = getViewer(familyId);
            player.sendMessage("Інвентар вже відкритий гравцем: " + viewerName);
            return;
        }

        String guiName = Translator.translateKyeWorld(GlobalManager.getInstance(), "repo_group_holder", LangUtils.getPlayerLanguage(player));
        FamilyChest holder = new FamilyChest(guiName, familyId);
        ItemStack[] familyChest = details.getFamilyChest();
        
        addChest(familyId, player.getName());

        Bukkit.getScheduler().runTask(GlobalManager.getInstance().getPlugin(), () -> {
            holder.addItems(familyChest);
            player.openInventory(holder.getInventory());
        });
    }

    // Метод для додавання елемента до мапи
    private void addChest(UUID uuid, String value) {
        chestMap.put(uuid, value);
    }

    // Метод для видалення елемента з мапи
    public void removeChest(UUID uuid) {
        chestMap.remove(uuid);
    }

    // Метод для перевірки наявності елемента в мапі
    public boolean hasChest(UUID uuid) {
        return chestMap.containsKey(uuid);
    }

    // Метод для отримання елемента з мапи
    public String getChest(UUID uuid) {
        return chestMap.get(uuid);
    }

    // Метод для отримання імені гравця, який дивиться у скриню
    public String getViewer(UUID uuid) {
        return chestMap.get(uuid);
    }
}
