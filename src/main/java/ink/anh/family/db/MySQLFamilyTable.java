package ink.anh.family.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import ink.anh.family.common.PlayerFamily;
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
    public void insertFamily(PlayerFamily playerFamily) {
    	try (Connection conn = dbManager.getConnection();
   		     PreparedStatement ps = conn.prepareStatement(
   		         "INSERT INTO " + tablePrefix + dbName + 
   		         " (player_uuid, gender, displayName, last_name, old_last_name, father, mother, spouse, children) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
   		         "ON DUPLICATE KEY UPDATE gender = VALUES(gender), displayName = VALUES(displayName), last_name = VALUES(last_name), old_last_name = VALUES(old_last_name), father = VALUES(father), mother = VALUES(mother), spouse = VALUES(spouse), children = VALUES(children);")) {

   		    ps.setString(1, playerFamily.getRoot().toString());
   		    ps.setString(2, Gender.toStringSafe(playerFamily.getGender()));
   		    ps.setString(3, playerFamily.getLoverCaseName().toLowerCase());
   		    ps.setString(4, joinOrReturnNull(playerFamily.getLastName()));
   		    ps.setString(5, joinOrReturnNull(playerFamily.getOldLastName()));
   		    ps.setString(6, playerFamily.getFather() != null ? playerFamily.getFather().toString() : null);
   		    ps.setString(7, playerFamily.getMother() != null ? playerFamily.getMother().toString() : null);
   		    ps.setString(8, playerFamily.getSpouse() != null ? playerFamily.getSpouse().toString() : null);
   		    ps.setString(9, PlayerFamily.uuidSetToString(playerFamily.getChildren()));

   		    ps.executeUpdate();
   		} catch (SQLException e) {
   		    ErrorLogger.log(dbManager.plugin, e, "Failed to insert or update family data");
   		}
    }

    @Override
    public PlayerFamily getFamily(UUID playerUUID) {
        PlayerFamily playerFamily = null;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tablePrefix + dbName + " WHERE player_uuid = ?;")) {
            
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
            	playerFamily = new PlayerFamily(
                        playerUUID,
                        Gender.fromString(rs.getString("gender")),
                        rs.getString("displayName"),
                        splitStringAndNullify(rs.getString("last_name"), ","),
                        splitStringAndNullify(rs.getString("old_last_name"), ","),
                        rs.getString("father") != null ? UUID.fromString(rs.getString("father")) : null,
                        rs.getString("mother") != null ? UUID.fromString(rs.getString("mother")) : null,
                        rs.getString("spouse") != null ? UUID.fromString(rs.getString("spouse")) : null,
                        PlayerFamily.stringToUuidSet(rs.getString("children"))
                    );
            }
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to get family data");
        }
        return playerFamily;
    }

    @Override
    public PlayerFamily getFamilyByDisplayName(String displayName) {
        PlayerFamily playerFamily = null;
        String sql = "SELECT * FROM " + tablePrefix + dbName + " WHERE displayName = ?;";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, displayName.toLowerCase());
            ResultSet rs = ps.executeQuery();

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
                    PlayerFamily.stringToUuidSet(rs.getString("children"))
                );
            }
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to get family data by displayName");
        }
        return playerFamily;
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
