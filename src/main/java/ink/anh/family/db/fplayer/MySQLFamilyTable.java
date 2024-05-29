package ink.anh.family.db.fplayer;

import java.util.UUID;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.PlayerFamily;

public class MySQLFamilyTable extends FamilyPlayerTable {

    public MySQLFamilyTable(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    @Override
    protected String getCreateIndexSQL() {
        return "CREATE INDEX idx_displayName ON " + dbName + " (displayName);";
    }

    @Override
    public void insert(PlayerFamily playerFamily) {
        String insertSQL = "INSERT INTO " + dbName + tableInsert + " ON DUPLICATE KEY UPDATE " + FamilyPlayerField.getUpdateFields();
        insertPlayerFamily(playerFamily, insertSQL);
    }

    @Override
    public PlayerFamily getFamily(UUID playerUUID) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE player_uuid = ?;";
        return fetchPlayerFamily(selectSQL, playerUUID.toString());
    }

    @Override
    public PlayerFamily getFamilyByDisplayName(String displayName) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE displayName = ?;";
        return fetchPlayerFamily(selectSQL, displayName.toLowerCase());
    }
}
