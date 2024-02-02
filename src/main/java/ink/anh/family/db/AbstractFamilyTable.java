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
    public abstract Family getFamily(UUID playerUUID);
    public abstract void deleteFamily(UUID playerUUID);
    public abstract void updateFamilyField(UUID playerUUID, String fieldName, String fieldValue);
}
