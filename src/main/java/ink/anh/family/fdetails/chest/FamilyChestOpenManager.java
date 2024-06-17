package ink.anh.family.fdetails.chest;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import ink.anh.api.lingo.Translator;
import ink.anh.api.utils.LangUtils;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.FamilyDetails;

public class FamilyChestOpenManager {

    private static final FamilyChestOpenManager instance = new FamilyChestOpenManager();
    private final BiMap<UUID, String> chestMap;

    private FamilyChestOpenManager() {
    	chestMap = HashBiMap.create();
    }

    public static FamilyChestOpenManager getInstance() {
        return instance;
    }

 // Метод для відкриття FamilyChest інвентаря
    public void openFamilyChest(Player player, FamilyDetails details) {
        if (details == null) {
            return;
        }

        UUID familyId = details.getFamilyId();

        synchronized (chestMap) {
            if (chestMap.containsKey(familyId)) {
                String viewerName = chestMap.get(familyId);
                player.sendMessage("Інвентар вже відкритий гравцем: " + viewerName);
                return;
            }

            String guiName = Translator.translateKyeWorld(GlobalManager.getInstance(), "repo_group_holder", LangUtils.getPlayerLanguage(player));
            FamilyChest holder = new FamilyChest(guiName, familyId);

            if (details.getFamilyChest() == null) {
                player.sendMessage("Скриня не встановлена");
                return;
            }
            ItemStack[] familyChest = details.getFamilyChest().getFamilyChest();

            chestMap.put(familyId, player.getName());

            Bukkit.getScheduler().runTask(GlobalManager.getInstance().getPlugin(), () -> {
                holder.addItems(familyChest);
                player.openInventory(holder.getInventory());
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            });
        }
    }

    // Метод для додавання елемента до мапи
    public void addChest(UUID uuid, String value) {
        synchronized (chestMap) {
            chestMap.put(uuid, value);
        }
    }

    // Метод для видалення елемента з мапи
    public void removeChest(UUID uuid) {
        synchronized (chestMap) {
            chestMap.remove(uuid);
        }
    }

    // Метод для перевірки наявності елемента в мапі
    public boolean hasChest(UUID uuid) {
        synchronized (chestMap) {
            return chestMap.containsKey(uuid);
        }
    }

    // Метод для отримання елемента з мапи
    public String getChest(UUID uuid) {
        synchronized (chestMap) {
            return chestMap.get(uuid);
        }
    }

    // Метод для отримання імені гравця, який дивиться у скриню
    public String getViewer(UUID uuid) {
        synchronized (chestMap) {
            return chestMap.get(uuid);
        }
    }

    // Метод для отримання ключа по нікнейму гравця
    public UUID getKeyByViewerName(String viewerName) {
        synchronized (chestMap) {
            return chestMap.inverse().get(viewerName);
        }
    }
}
