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

    @Override
    protected void initialize() {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + tablePrefix + dbName + " (" +
                            "player_uuid VARCHAR(36) PRIMARY KEY," +
                            "gender VARCHAR(255)," +
                            "displayName VARCHAR(255) NOT NULL UNIQUE," + // Зроблено поле не нульовим та унікальним
                            "last_name TEXT," +
                            "old_last_name TEXT," +
                            "father VARCHAR(36)," +
                            "mother VARCHAR(36)," +
                            "spouse VARCHAR(36)," +
                            "children TEXT" +
                            ");" +
                            "CREATE INDEX IF NOT EXISTS idx_displayName ON " + tablePrefix + dbName + " (displayName);")) { // Індекс для displayName
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to create family table");
        }
    }

    @Override
    public void insertFamily(Family family) {
    	try (Connection conn = dbManager.getConnection();
   		     PreparedStatement ps = conn.prepareStatement(
   		         "INSERT INTO " + tablePrefix + dbName + 
   		         " (player_uuid, gender, displayName, last_name, old_last_name, father, mother, spouse, children) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
   		         "ON DUPLICATE KEY UPDATE gender = VALUES(gender), displayName = VALUES(displayName), last_name = VALUES(last_name), old_last_name = VALUES(old_last_name), father = VALUES(father), mother = VALUES(mother), spouse = VALUES(spouse), children = VALUES(children);")) {

   		    ps.setString(1, family.getRoot().toString());
   		    ps.setString(2, Gender.toStringSafe(family.getGender()));
   		    ps.setString(3, family.getDisplayName().toLowerCase());
   		    ps.setString(4, String.join(",", family.getLastName()));
   		    ps.setString(5, String.join(",", family.getOldLastName()));
   		    ps.setString(6, family.getFather() != null ? family.getFather().toString() : null);
   		    ps.setString(7, family.getMother() != null ? family.getMother().toString() : null);
   		    ps.setString(8, family.getSpouse() != null ? family.getSpouse().toString() : null);
   		    ps.setString(9, Family.uuidSetToString(family.getChildren()));

   		    ps.executeUpdate();
   		} catch (SQLException e) {
   		    ErrorLogger.log(dbManager.plugin, e, "Failed to insert or update family data");
   		}
    }

    @Override
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

    @Override
    public Family getFamilyByDisplayName(String displayName) {
        Family family = null;
        String sql = "SELECT * FROM " + tablePrefix + dbName + " WHERE displayName = ?;";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, displayName.toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                family = new Family(
                    UUID.fromString(rs.getString("player_uuid")),
                    Gender.fromString(rs.getString("gender")),
                    rs.getString("displayName"),
                    rs.getString("last_name") != null ? rs.getString("last_name").split(",") : new String[0],
                    rs.getString("old_last_name") != null ? rs.getString("old_last_name").split(",") : new String[0],
                    rs.getString("father") != null ? UUID.fromString(rs.getString("father")) : null,
                    rs.getString("mother") != null ? UUID.fromString(rs.getString("mother")) : null,
                    rs.getString("spouse") != null ? UUID.fromString(rs.getString("spouse")) : null,
                    Family.stringToUuidSet(rs.getString("children"))
                );
            }
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to get family data by displayName");
        }
        return family;
    }

    @Override
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
