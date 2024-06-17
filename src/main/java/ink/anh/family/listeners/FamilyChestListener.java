package ink.anh.family.listeners;

import ink.anh.api.utils.SyncExecutor;
import ink.anh.family.AnhyFamily;
import ink.anh.family.db.fdetails.FamilyDetailsField;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.chest.FamilyChest;
import ink.anh.family.fdetails.chest.FamilyChestManager;
import ink.anh.family.fdetails.chest.FamilyChestOpenManager;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class FamilyChestListener implements Listener {

    private AnhyFamily familyPlugin;

    public FamilyChestListener(AnhyFamily familyPlugin) {
        this.familyPlugin = familyPlugin;
    }

    @EventHandler
    public void onPlayerRightClickBlock(PlayerInteractEvent event) {
        // Перевіряємо, чи це правий клік по блоку
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();
            
            // Перевірка чи є блок у мапі
            if (clickedBlock != null && FamilyChestManager.isFamilyChest(clickedBlock.getLocation())) {
                event.setCancelled(true);
                
                FamilyChestManager familyChestManager = new FamilyChestManager(familyPlugin, player, null, new String[]{});
                familyChestManager.attemptOpenFamilyChest(clickedBlock.getLocation());
            }
        }
    }

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
                FamilyChestOpenManager chestManager = FamilyChestOpenManager.getInstance();
                UUID familyId = chestManager.getKeyByViewerName(player.getName());

                if (familyId != null) {
                    chestManager.removeChest(familyId);

                    // Збереження сімейної скрині
                    FamilyDetails familyDetails = FamilyDetailsGet.getFamilyDetails(familyId);
                    if (familyDetails != null) {
                        familyDetails.getFamilyChest().setFamilyChest(event.getInventory().getContents());
                        FamilyDetailsSave.saveFamilyDetails(familyDetails, FamilyDetailsField.FAMILY_CHEST);
                        SyncExecutor.runSync(() -> player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f));
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

            familyDetails.getFamilyChest().setFamilyChest(inventory.getContents());
    	});
    }
}
