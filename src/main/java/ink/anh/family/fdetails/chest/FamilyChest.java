package ink.anh.family.fdetails.chest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class FamilyChest implements InventoryHolder {
	
    private Inventory inventory;
    private UUID familyId;

    public FamilyChest(String prefix, UUID familyId) {
        // Створення інвентаря на 54 слотів з назвою "Repository"
        this.inventory = Bukkit.createInventory(this, 54, prefix);
        this.familyId = familyId;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void addItems(ItemStack[] content) {
        inventory.setContents(content);
    }
}
