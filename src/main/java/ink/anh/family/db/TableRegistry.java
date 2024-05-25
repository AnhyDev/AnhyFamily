package ink.anh.family.db;

import ink.anh.api.database.AbstractTableRegistrar;
import ink.anh.api.database.DatabaseManager;
import ink.anh.api.database.MySQLDatabaseManager;
import ink.anh.api.database.SQLiteDatabaseManager;

import ink.anh.family.db.fplayer.MySQLFamilyTable;
import ink.anh.family.db.fplayer.SQLiteFamilyTable;
import ink.anh.family.db.fdetails.SQLiteFamilyDetailsTable;
import ink.anh.family.db.fdetails.MySQLFamilyDetailsTable;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;

public class TableRegistry extends AbstractTableRegistrar {
    private AnhyFamily familyPlugin;

	public TableRegistry(AnhyFamily familyPlugin) {
		this.familyPlugin = familyPlugin;
	}

	@Override
    public void registerAllTables(DatabaseManager dbManager) {
        
        // Реєстрація таблиць для SQLite
        if (dbManager instanceof SQLiteDatabaseManager) {
            dbManager.registerTable(PlayerFamily.class, new SQLiteFamilyTable(familyPlugin));
            dbManager.registerTable(FamilyDetails.class, new SQLiteFamilyDetailsTable(familyPlugin));
        }
        
        // Реєстрація таблиць для MySQL
        if (dbManager instanceof MySQLDatabaseManager) {
            dbManager.registerTable(PlayerFamily.class, new MySQLFamilyTable(familyPlugin));
            dbManager.registerTable(FamilyDetails.class, new MySQLFamilyDetailsTable(familyPlugin));
        }
    }
}
