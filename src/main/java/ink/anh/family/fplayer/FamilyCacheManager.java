package ink.anh.family.fplayer;

import ink.anh.api.DataHandler;
import java.util.UUID;

public class FamilyCacheManager extends DataHandler {

	private static FamilyCacheManager instance;
    private static final String FAMILY_DATA_KEY = "fplayer";

    private FamilyCacheManager() {
		if (instance == null) {
			instance = this;
		}
	}

	public static FamilyCacheManager getInstance() {
		new FamilyCacheManager();
		return instance;
	}
	
    /**
     * Saves the family data for a specific player.
     * 
     * @param uuid   The UUID of the player.
     * @param playerFamily The family data to save.
     */
	public void addFamily(UUID uuid, PlayerFamily playerFamily) {
        addData(uuid, FAMILY_DATA_KEY, playerFamily);
    }

	public void addFamily(PlayerFamily playerFamily) {
        addData(playerFamily.getRoot(), FAMILY_DATA_KEY, playerFamily);
    }

    /**
     * Retrieves the family data for a specific player.
     * 
     * @param uuid The UUID of the player.
     * @return The PlayerFamily object, or null if no data is found.
     */
	public PlayerFamily getFamilyData(UUID uuid) {
        Object data = getData(uuid, FAMILY_DATA_KEY);
        if (data instanceof PlayerFamily) {
            return (PlayerFamily) data;
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
        return getData(uuid, FAMILY_DATA_KEY) instanceof PlayerFamily;
    }
}
