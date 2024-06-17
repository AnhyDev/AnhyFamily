package ink.anh.family.fdetails.chest;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Chest {

    private ItemStack[] familyChest;
    private Location chestLocation;

    public Chest(ItemStack[] familyChest, Location chestLocation) {
        this.familyChest = familyChest != null ? familyChest : new ItemStack[54];
        this.chestLocation = chestLocation;
    }

    public Chest() {
        this.familyChest = new ItemStack[54];
        this.chestLocation = null;
    }

    public ItemStack[] getFamilyChest() {
        return familyChest;
    }

    public void setFamilyChest(ItemStack[] familyChest) {
        this.familyChest = familyChest;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public void setChestLocation(Location chestLocation) {
        this.chestLocation = chestLocation;
    }

    public void addItem(ItemStack item, int slot) {
        if (slot >= 0 && slot < familyChest.length) {
            familyChest[slot] = item;
        }
    }

    public void removeItem(int slot) {
        if (slot >= 0 && slot < familyChest.length) {
            familyChest[slot] = null;
        }
    }

    public boolean isChestFull() {
        for (ItemStack item : familyChest) {
            if (item == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isChestEmpty() {
        for (ItemStack item : familyChest) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Chest{" +
                "familyChest=" + Arrays.toString(familyChest) +
                ", chestLocation=" + chestLocation +
                '}';
    }
}
