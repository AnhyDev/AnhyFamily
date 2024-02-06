package ink.anh.family.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import ink.anh.family.common.Family;
import ink.anh.family.gender.Gender;

public class SQLiteFamilyTable extends AbstractFamilyTable {


    public SQLiteFamilyTable(SQLiteDatabaseManager dbManager) {
    	super(dbManager);
    }

    @Override
    public void initialize() {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
            		 "CREATE TABLE IF NOT EXISTS " + dbName + " (" +
            				 "player_uuid TEXT PRIMARY KEY," +
            				 "gender TEXT," + // Додана кома тут
            				 "displayName TEXT," +
            				 "last_name TEXT," +
            				 "old_last_name TEXT," +
            				 "father TEXT," +
            				 "mother TEXT," +
            				 "spouse TEXT," +
            				 "children TEXT" +
            				 ");")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to create family table");
        }
    }

    @Override
    public void insertFamily(Family family) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR REPLACE INTO " + dbName + " (player_uuid, gender, displayName, last_name, old_last_name, father, mother, spouse, children) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            
            ps.setString(1, family.getRoot().toString());
            ps.setString(2, Gender.toStringSafe(family.getGender()));
            ps.setString(3, family.getDisplayName());
            ps.setString(4, String.join(",", family.getLastName()));
            ps.setString(5, String.join(",", family.getOldLastName()));
            ps.setString(6, family.getFather() != null ? family.getFather().toString() : null);
            ps.setString(7, family.getMother() != null ? family.getMother().toString() : null);
            ps.setString(8, family.getSpouse() != null ? family.getSpouse().toString() : null);
            ps.setString(9, Family.uuidSetToString(family.getChildren()));

            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to insert family data");
        }
    }

    @Override
    public Family getFamily(UUID playerUUID) {
        Family family = null;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + dbName + " WHERE player_uuid = ?;")) {
            
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                family = new Family(
                    playerUUID,
                    Gender.fromString(rs.getString("gender")),
                    rs.getString("displayName"),
                    rs.getString("last_name") != null ? rs.getString("last_name").split(",") : new String[2],
                    rs.getString("old_last_name") != null ? rs.getString("old_last_name").split(",") : new String[2],
                    rs.getString("father") != null ? UUID.fromString(rs.getString("father")) : null,
                    rs.getString("mother") != null ? UUID.fromString(rs.getString("mother")) : null,
                    rs.getString("spouse") != null ? UUID.fromString(rs.getString("spouse")) : null,
                    Family.stringToUuidSet(rs.getString("children"))
                );
            }
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to get family data");
        }
        return family;
    }

    @Override
    public void deleteFamily(UUID playerUUID) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM " + dbName + " WHERE player_uuid = ?;")) {
            
            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to delete family data");
        }
    }

    @Override
    public void updateFamilyField(UUID playerUUID, String fieldName, String fieldValue) {
        String sql = "UPDATE " + dbName + " SET " + fieldName + " = ? WHERE player_uuid = ?;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fieldValue);
            ps.setString(2, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to update family field: " + fieldName);
        }
    }
}
