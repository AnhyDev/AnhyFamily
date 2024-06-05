package ink.anh.family.listeners;

import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.chest.FamilyChest;
import ink.anh.family.fdetails.chest.FamilyChestManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;

public class FamilyChestListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof FamilyChest) {
            updateFamilyChest(event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof FamilyChest) {
            updateFamilyChest(event.getInventory());
        }
    }

    @EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event){
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof FamilyChest) {
            updateFamilyChest(event.getPlayer().getOpenInventory().getTopInventory());
        }
	}

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() instanceof FamilyChest) {
            updateFamilyChest(event.getSource());
        }
        
        if (event.getDestination().getHolder() instanceof FamilyChest) {
            updateFamilyChest(event.getDestination());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof FamilyChest) {
            Player player = (Player) event.getPlayer();

        	SyncExecutor.runAsync(() -> {
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
                            familyDetails.setFamilyChest(event.getInventory().getContents());
                            FamilyDetailsSave.saveFamilyDetails(familyDetails, FamilyDetailsField.FAMILY_CHEST);
                        }
                        return;
                    }
                }
        	});
        }
    }

    private void updateFamilyChest(Inventory inventory) {
    	SyncExecutor.runAsync(() -> {
            FamilyChest holder = (FamilyChest) inventory.getHolder();
            if (holder == null) {
                return;
            }

            UUID familyId = holder.getFamilyId();
            if (familyId == null) {
                return;
            }

            FamilyDetails familyDetails = FamilyDetailsGet.getFamilyDetails(familyId);
            if (familyDetails == null) {
                return;
            }

            familyDetails.setFamilyChest(inventory.getContents());
    	});
    }
}