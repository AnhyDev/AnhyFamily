package ink.anh.family.db.fplayer;

import java.util.UUID;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.PlayerFamily;

public class SQLiteFamilyTable extends FamilyPlayerTable {

    public SQLiteFamilyTable(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    @Override
    protected String getCreateIndexSQL() {
        return "CREATE INDEX IF NOT EXISTS idx_display_name ON " + dbName + " (display_name);";
    }

    @Override
    public void insert(PlayerFamily playerFamily) {
        String insertSQL = "INSERT OR REPLACE INTO " + dbName + tableInsert + ";";
        insertPlayerFamily(playerFamily, insertSQL);
    }

    @Override
    public PlayerFamily getFamily(UUID playerUUID) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE player_uuid = ?;";
        return fetchPlayerFamily(selectSQL, playerUUID.toString());
    }

    @Override
    public PlayerFamily getFamilyByDisplayName(String displayName) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE display_name = ?;";
        return fetchPlayerFamily(selectSQL, displayName.toLowerCase());
    }
}
