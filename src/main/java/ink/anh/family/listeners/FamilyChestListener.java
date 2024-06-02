package ink.anh.family.listeners;

import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.FamilyChest;
import ink.anh.family.fdetails.FamilyChestManager;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class FamilyChestListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof FamilyChest) {

            switch (event.getClick()) {
                case LEFT:
                    // Логіка для лівого кліку
                    break;
                case RIGHT:
                    // Логіка для правого кліку
                    break;
                case SHIFT_LEFT:
                    // Логіка для шіфт+лівий кліку
                    break;
                case SHIFT_RIGHT:
                    // Логіка для шіфт+правий кліку
                    break;
                // 
                default:
                    break;
            }
            // 
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof FamilyChest) {
            // 
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.getInventory().getHolder() instanceof FamilyChest) {
            // 
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof FamilyChest) {
            Player player = (Player) event.getPlayer();
            PlayerFamily playerFamily = FamilyUtils.getFamily(player);
            
            if (playerFamily == null) {
                return;
            }

            Map<UUID, FamilyDetails> detailsAll = FamilyDetailsGet.getAllFamilyDetails(playerFamily);
            
            if (detailsAll == null || detailsAll.isEmpty()) {
                return;
            }

            FamilyChestManager chestManager = FamilyChestManager.getInstance();
            
            for (UUID familyId : detailsAll.keySet()) {
                if (chestManager.hasChest(familyId) && player.getName().equals(chestManager.getViewer(familyId))) {
                    FamilyDetails familyDetails = detailsAll.get(familyId);
                    chestManager.removeChest(familyId);
                    if (familyDetails != null) {
                        FamilyDetailsSave.saveFamilyDetails(familyDetails, FamilyDetailsField.FAMILY_CHEST);
                    }
                    return;
                }
            }
        }
    }
}
