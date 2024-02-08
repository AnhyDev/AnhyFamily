package ink.anh.family.db;

import java.util.UUID;

import ink.anh.family.common.Family;

public abstract class AbstractFamilyTable {
    
    protected DatabaseManager dbManager;
    protected String dbName;

    public AbstractFamilyTable(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.dbName = dbManager.dbName;
        initialize();
    }

    protected abstract void initialize();

    public abstract void insertFamily(Family family);
    
    public Family getFamily(UUID playerUUID, String displayName) {

        Family family = null;
        
        if (playerUUID != null) {
        	family = getFamily(playerUUID);
        }
        
        if (family == null && displayName != null) {
        	family = getFamilyByDisplayName(displayName);
        }
        
        if (family != null) {
        	if (!family.getRoot().equals(playerUUID)) {
        		family.setRoot(playerUUID);
        	}
        }
        
        return family;
    }
    
    public abstract Family getFamily(UUID playerUUID);
    public abstract Family getFamilyByDisplayName(String displayName);
    public abstract void deleteFamily(UUID playerUUID);
    public abstract void updateFamilyField(UUID playerUUID, String fieldName, String fieldValue);
}
