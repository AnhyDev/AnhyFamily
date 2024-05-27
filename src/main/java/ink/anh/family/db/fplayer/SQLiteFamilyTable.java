package ink.anh.family.db.fplayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import ink.anh.api.database.ErrorLogger;
import ink.anh.api.database.TableField;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.gender.Gender;

public class SQLiteFamilyTable extends FamilyPlayerTable {

    public SQLiteFamilyTable(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    @Override
    public void initialize() {
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS " + dbName + tableCreate +
                        "CREATE INDEX IF NOT EXISTS idx_displayName ON " + dbName + " (displayName);";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(familyPlugin, e, "Failed to create family tableInsert or index");
        }
    }

    @Override
    public void insert(PlayerFamily playerFamily) {
        String insertSQL =
                "INSERT OR REPLACE INTO " + dbName + tableInsert + ";";

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setString(1, playerFamily.getRoot().toString());
                ps.setString(2, Gender.toStringSafe(playerFamily.getGender()));
                ps.setString(3, playerFamily.getLoverCaseName().toLowerCase());
                ps.setString(4, joinOrReturnNull(playerFamily.getLastName()));
                ps.setString(5, joinOrReturnNull(playerFamily.getOldLastName()));
                ps.setString(6, playerFamily.getFather() != null ? playerFamily.getFather().toString() : null);
                ps.setString(7, playerFamily.getMother() != null ? playerFamily.getMother().toString() : null);
                ps.setString(8, playerFamily.getSpouse() != null ? playerFamily.getSpouse().toString() : null);
                ps.setString(9, PlayerFamily.uuidSetToString(playerFamily.getChildren()));
                ps.setString(10, playerFamily.getFamilyId() != null ? playerFamily.getFamilyId().toString() : null);
                ps.setString(11, playerFamily.getParentFamilyId() != null ? playerFamily.getParentFamilyId().toString() : null);
                ps.setString(12, PlayerFamily.uuidSetToString(playerFamily.getChildFamilyIds()));
                ps.setString(13, playerFamily.getDynastyId() != null ? playerFamily.getDynastyId().toString() : null);
                ps.executeUpdate();
            }
        }, "Failed to insert or replace family data: " + playerFamily);
    }

    @Override
    public PlayerFamily getFamily(UUID playerUUID) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE player_uuid = ?;";
        return getPlayerFamily(selectSQL, playerUUID.toString());
    }

    @Override
    public PlayerFamily getFamilyByDisplayName(String displayName) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE displayName = ?;";
        return getPlayerFamily(selectSQL, displayName.toLowerCase());
    }

    private PlayerFamily getPlayerFamily(String sql, String param) {
        PlayerFamily playerFamily = null;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    playerFamily = new PlayerFamily(
                            UUID.fromString(rs.getString("player_uuid")),
                            Gender.fromString(rs.getString("gender")),
                            rs.getString("displayName"),
                            splitStringAndNullify(rs.getString("last_name"), ","),
                            splitStringAndNullify(rs.getString("old_last_name"), ","),
                            rs.getString("father") != null ? UUID.fromString(rs.getString("father")) : null,
                            rs.getString("mother") != null ? UUID.fromString(rs.getString("mother")) : null,
                            rs.getString("spouse") != null ? UUID.fromString(rs.getString("spouse")) : null,
                            PlayerFamily.stringToUuidSet(rs.getString("children")),
                            rs.getString("family_id") != null ? UUID.fromString(rs.getString("family_id")) : null,
                            rs.getString("parent_family_id") != null ? UUID.fromString(rs.getString("parent_family_id")) : null,
                            PlayerFamily.stringToUuidSet(rs.getString("child_family_ids")),
                            rs.getString("dynasty_id") != null ? UUID.fromString(rs.getString("dynasty_id")) : null
                    );
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(familyPlugin, e, "Failed to get family data");
        }
        return playerFamily;
    }

    @Override
    public void deleteFamily(UUID playerUUID) {
        String deleteSQL = "DELETE FROM " + dbName + " WHERE player_uuid = ?;";
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, playerUUID.toString());
                ps.executeUpdate();
            }
        }, "Failed to delete family data for UUID: " + playerUUID);
    }

    @Override
    public <K> void updateField(TableField<K> tableField) {
    	String fieldName = tableField.getFieldName();
        if (!FamilyPlayerField.contains(fieldName)) {
            throw new IllegalArgumentException("Invalid field name");
        }

        String updateSQL = "UPDATE " + dbName + " SET " + fieldName + " = ? WHERE player_uuid = ?;";
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                ps.setString(1, tableField.getFieldValue());
                ps.setString(2, tableField.getKey().toString());
                ps.executeUpdate();
            }
        }, "Failed to update family field: " + tableField.getFieldValue());
    }
}