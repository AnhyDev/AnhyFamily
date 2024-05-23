package ink.anh.family.db;

import ink.anh.family.AnhyFamily;
import ink.anh.family.common.PlayerFamily;
import ink.anh.family.db.fplayer.SQLiteFamilyTable;
import ink.anh.family.db.fplayer.MySQLFamilyTable;

public class TableRegistry {

    public static void registerAllTables(DatabaseManager dbManager) {
        AnhyFamily plugin = dbManager.plugin;
        
        // Реєстрація таблиць для SQLite
        if (dbManager instanceof SQLiteDatabaseManager) {
            dbManager.registerTable(PlayerFamily.class, new SQLiteFamilyTable(plugin));
            // Реєструй інші таблиці тут
        }
        
        // Реєстрація таблиць для MySQL
        if (dbManager instanceof MySQLDatabaseManager) {
            dbManager.registerTable(PlayerFamily.class, new MySQLFamilyTable((MySQLDatabaseManager) dbManager));
            // Реєструй інші таблиці тут
        }
    }
}
