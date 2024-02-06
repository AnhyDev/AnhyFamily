package ink.anh.family.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ink.anh.family.common.Family;
import ink.anh.family.gender.Gender;

public class MySQLFamilyTable extends AbstractFamilyTable {

    private String tablePrefix;

    public MySQLFamilyTable(MySQLDatabaseManager dbManager) {
    	super(dbManager);
    	this.dbName = dbManager.getDatabase();
        this.tablePrefix = dbManager.getTablePrefix();
    }

    protected void initialize() {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
            		 "CREATE TABLE IF NOT EXISTS " + tablePrefix + dbName + " (" +
            				 "player_uuid VARCHAR(36) PRIMARY KEY," +
            				 "gender VARCHAR(255)," +
            				 "displayName VARCHAR(255)," +
            				 "last_name TEXT," +
            				 "old_last_name TEXT," +
            				 "father VARCHAR(36)," +
            				 "mother VARCHAR(36)," +
            				 "spouse VARCHAR(36)," +
            				 "children TEXT" +
            				 ");")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to create family table");
        }
    }

    public void insertFamily(Family family) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "REPLACE INTO " + tablePrefix + dbName + " (player_uuid, gender, displayName, last_name, old_last_name, father, mother, spouse, children) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);")) {
            
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

    public Family getFamily(UUID playerUUID) {
        Family family = null;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tablePrefix + dbName + " WHERE player_uuid = ?;")) {
            
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
    
    public void deleteFamily(UUID playerUUID) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tablePrefix + dbName + " WHERE player_uuid = ?;")) {
            
            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to delete family data");
        }
    }

    public void updateFamilyField(UUID playerUUID, String fieldName, String fieldValue) {
        List<String> allowedFields = Arrays.asList("displayName", "last_name", "old_last_name", "father", "mother", "spouse", "children");
        if (!allowedFields.contains(fieldName)) {
            throw new IllegalArgumentException("Invalid field name");
        }

        String sql = "UPDATE " + tablePrefix + dbName + " SET " + fieldName + " = ? WHERE player_uuid = ?;";

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
