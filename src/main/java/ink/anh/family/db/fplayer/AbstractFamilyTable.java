package ink.anh.family.db.fplayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ink.anh.api.database.AbstractTable;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.PlayerFamily;

public abstract class AbstractFamilyTable extends AbstractTable<PlayerFamily> {

	protected AnhyFamily familyPlugin;
    protected static final Map<String, String> allowedFields = new HashMap<>();

    static {
        allowedFields.put("displayName", "displayName");
        allowedFields.put("last_name", "last_name");
        allowedFields.put("old_last_name", "old_last_name");
        allowedFields.put("father", "father");
        allowedFields.put("mother", "mother");
        allowedFields.put("spouse", "spouse");
        allowedFields.put("children", "children");
        allowedFields.put("family_id", "family_id");
        allowedFields.put("parent_family_id", "parent_family_id");
        allowedFields.put("child_family_ids", "child_family_ids");
        allowedFields.put("dynasty_id", "dynasty_id");
    }

    protected static String tableCreate = " (" +
            "player_uuid VARCHAR(36) PRIMARY KEY," +
            "gender VARCHAR(36)," +
            "displayName VARCHAR(255) NOT NULL UNIQUE," +
            "last_name TEXT," +
            "old_last_name TEXT," +
            "father VARCHAR(36)," +
            "mother VARCHAR(36)," +
            "spouse VARCHAR(36)," +
            "children TEXT," +
            "family_id VARCHAR(36)," +
            "parent_family_id VARCHAR(36)," +
            "child_family_ids TEXT," +
            "dynasty_id VARCHAR(36)" +
            ");";

    protected static String tableInsert = " (player_uuid, gender, displayName, last_name, old_last_name, father, mother, spouse, children, family_id, parent_family_id, child_family_ids, dynasty_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public AbstractFamilyTable(AnhyFamily familyPlugin) {
        super(familyPlugin.getGlobalManager(), "PlayerFamily");
        this.familyPlugin = familyPlugin;
        initialize();
    }

    public abstract void deleteFamily(UUID playerUUID);

    public abstract PlayerFamily getFamily(UUID playerUUID);

    public abstract PlayerFamily getFamilyByDisplayName(String displayName);

    public PlayerFamily getEntity(UUID uuid, String displayName) {
        PlayerFamily entity = null;

        if (uuid != null) {
            entity = getFamily(uuid);
        }

        if (entity == null && displayName != null) {
            entity = getFamilyByDisplayName(displayName);
        }

        return entity;
    }
    
    public PlayerFamily getFamily(UUID playerUUID, String displayName) {

        PlayerFamily playerFamily = null;
        
        if (playerUUID != null) {
        	playerFamily = getFamily(playerUUID);
        }
        
        if (playerFamily == null && displayName != null) {
        	playerFamily = getFamilyByDisplayName(displayName);
        }
        
        if (playerFamily != null) {
        	if (!playerFamily.getRoot().equals(playerUUID)) {
        		playerFamily.setRoot(playerUUID);
        	}
        }
        
        return playerFamily;
    }

    @Override
    public void update(PlayerFamily entity) {
        insert(entity);
    }

    @Override
    public void delete(PlayerFamily entity) {
        deleteFamily(entity.getRoot());
    }
}
