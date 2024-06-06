package ink.anh.family.fdetails;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import ink.anh.api.messages.Logger;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsTable;
import ink.anh.family.fdetails.chest.FamilyChestManager;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import org.bukkit.Location;

public class FamilyDataLoader {

    public static void loadData() {
    	FamilyDetailsTable familyDetailsTable = (FamilyDetailsTable) GlobalManager.getInstance().getDatabaseManager().getTable(FamilyDetails.class);

        // Load family details from database
        Map<UUID, FamilyDetails> familyDetailsMap = familyDetailsTable.getAllFamilyDetails();

        // Initialize FamilyChestManager locationToUUIDMap
        Map<Integer, UUID> locationToUUIDMap = familyDetailsMap.values().stream()
                .filter(details -> details.getFamilyChest().getChestLocation() != null)
                .collect(Collectors.toMap(
                        details -> getLocationHash(details.getFamilyChest().getChestLocation()),
                        FamilyDetails::getFamilyId
                ));
        FamilyChestManager.setLocationToUUIDMap(locationToUUIDMap);

        // Initialize FamilySymbolManager symbolMap
        Map<String, UUID> symbolMap = familyDetailsMap.values().stream()
                .filter(details -> details.getFamilySymbol() != null)
                .collect(Collectors.toMap(
                        FamilyDetails::getFamilySymbol,
                        FamilyDetails::getFamilyId
                ));
        FamilySymbolManager.setFamilyIdBySymbolMap(symbolMap);
        
        Logger.info(AnhyFamily.getInstance(), "The static maps of the FamilyChestManager and FamilySymbolManager classes have been loaded");
    }

    public static int getLocationHash(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int worldHash = location.getWorld().hashCode();
        return Objects.hash(worldHash, x, y, z);
    }
}
