package ink.anh.family.fdetails.chest;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class Chest {

    private ItemStack[] familyChest;
    private Location chestLocation;
    private int openDistance;

    public Chest(ItemStack[] familyChest, Location chestLocation, int openDistance) {
        this.familyChest = familyChest != null ? familyChest : new ItemStack[54];
        this.chestLocation = chestLocation;
        this.openDistance = openDistance;
    }

    public Chest() {
        this.familyChest = new ItemStack[54];
        this.chestLocation = null;
        this.openDistance = 0;
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

    public int getOpenDistance() {
        return openDistance;
    }

    public void setOpenDistance(int openDistance) {
        this.openDistance = openDistance;
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
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(familyChest), chestLocation, openDistance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Chest other = (Chest) obj;
        return openDistance == other.openDistance &&
                Arrays.equals(familyChest, other.familyChest) &&
                Objects.equals(chestLocation, other.chestLocation);
    }

    @Override
    public String toString() {
        return "Chest{" +
                "familyChest=" + Arrays.toString(familyChest) +
                ", chestLocation=" + chestLocation +
                ", openDistance=" + openDistance +
                '}';
    }
}
