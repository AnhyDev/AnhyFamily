package ink.anh.family.common;

import ink.anh.api.DataHandler;
import java.util.UUID;

public class FamilyDataHandler extends DataHandler {

    private static final String FAMILY_DATA_KEY = "family";

    /**
     * Saves the family data for a specific player.
     * 
     * @param uuid   The UUID of the player.
     * @param family The family data to save.
     */
    public void addFamilyData(UUID uuid, Family family) {
        addData(uuid, FAMILY_DATA_KEY, family);
    }

    /**
     * Retrieves the family data for a specific player.
     * 
     * @param uuid The UUID of the player.
     * @return The Family object, or null if no data is found.
     */
    public Family getFamilyData(UUID uuid) {
        Object data = getData(uuid, FAMILY_DATA_KEY);
        if (data instanceof Family) {
            return (Family) data;
        }
        removeFamilyData(uuid);
        return null;
    }

    /**
     * Removes the family data for a specific player.
     * 
     * @param uuid The UUID of the player.
     */
    public void removeFamilyData(UUID uuid) {
        removeData(uuid, FAMILY_DATA_KEY);
    }

    /**
     * Checks if the family data exists for a specific player.
     * 
     * @param uuid The UUID of the player.
     * @return true if family data exists, false otherwise.
     */
    public boolean hasFamilyData(UUID uuid) {
        return getData(uuid, FAMILY_DATA_KEY) instanceof Family;
    }
}
