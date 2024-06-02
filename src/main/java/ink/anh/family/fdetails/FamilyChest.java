package ink.anh.family.fdetails;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class FamilyChest implements InventoryHolder {
	
    private Inventory inventory;

    public FamilyChest(String repoName) {
        // Створення інвентаря на 54 слотів з назвою "Repository"
        inventory = Bukkit.createInventory(this, 54, repoName);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void addItems(ItemStack[] content) {
    	inventory.setContents(content);
    }
}
