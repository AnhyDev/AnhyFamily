package ink.anh.family.listeners;

import ink.anh.family.fdetails.FamilyChest;
import ink.anh.family.fdetails.FamilyChestManager;
import ink.anh.family.util.FamilyUtils;

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
            UUID familyId = FamilyUtils.getFamily((Player) event.getPlayer()).getFamilyId(); 
            if (familyId != null) {
                FamilyChestManager.getInstance().removeChest(familyId);
            }
        }
    }
}
