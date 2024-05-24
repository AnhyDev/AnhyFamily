package ink.anh.family.db.fdetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ink.anh.family.db.ErrorLogger;
import ink.anh.family.db.SQLiteDatabaseManager;
import ink.anh.family.db.TableField;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FDetailsSerializator;

public class SQLiteFamilyDetailsTable extends AbstractFamilyDetailsTable {

    private final SQLiteDatabaseManager dbManager;
    private static final Map<String, String> allowedFields = new HashMap<>();

    static {
        allowedFields.put("home_location", "home_location");
        allowedFields.put("family_chest", "family_chest");
        allowedFields.put("children_access_home", "children_access_home");
        allowedFields.put("children_access_chest", "children_access_chest");
        allowedFields.put("ancestors_access_home", "ancestors_access_home");
        allowedFields.put("ancestors_access_chest", "ancestors_access_chest");
        allowedFields.put("specific_access_map", "specific_access_map");
        allowedFields.put("home_set_date", "home_set_date");
    }

    public SQLiteFamilyDetailsTable(SQLiteDatabaseManager dbManager) {
        super(dbManager.plugin);
        this.dbManager = dbManager;
    }

    @Override
    protected void initialize() {
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS " + dbName + " (" +
                        "family_id TEXT PRIMARY KEY," +
                        "home_location TEXT," +
                        "family_chest TEXT," +
                        "children_access_home BOOLEAN," +
                        "children_access_chest BOOLEAN," +
                        "ancestors_access_home BOOLEAN," +
                        "ancestors_access_chest BOOLEAN," +
                        "specific_access_map TEXT," +
                        "home_set_date TEXT" +
                        ");";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to create family details table");
        }
    }

    @Override
    public void insert(FamilyDetails familyDetails) {
        String insertSQL =
                "INSERT OR REPLACE INTO " + dbName + " (family_id, home_location, family_chest, children_access_home, children_access_chest, ancestors_access_home, ancestors_access_chest, specific_access_map, home_set_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSQL)) {

            ps.setString(1, familyDetails.getFamilyId().toString());
            ps.setString(2, FDetailsSerializator.serializeLocation(familyDetails.getHomeLocation()));
            ps.setString(3, FDetailsSerializator.serializeFamilyChest(familyDetails.getFamilyChest()));
            ps.setBoolean(4, familyDetails.isChildrenAccessHome());
            ps.setBoolean(5, familyDetails.isChildrenAccessChest());
            ps.setBoolean(6, familyDetails.isAncestorsAccessHome());
            ps.setBoolean(7, familyDetails.isAncestorsAccessChest());
            ps.setString(8, FDetailsSerializator.serializeSpecificAccessMap(familyDetails.getSpecificAccessMap()));
            ps.setString(9, familyDetails.getHomeSetDate() != null ? familyDetails.getHomeSetDate().toString() : null);

            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to insert family details");
        }
    }

    @Override
    public FamilyDetails getFamilyDetails(UUID familyId) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE family_id = ?;";
        FamilyDetails familyDetails = null;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSQL)) {

            ps.setString(1, familyId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    familyDetails = new FamilyDetails(
                            UUID.fromString(rs.getString("family_id")),
                            FDetailsSerializator.deserializeLocation(rs.getString("home_location")),
                            FDetailsSerializator.deserializeFamilyChest(rs.getString("family_chest")),
                            rs.getBoolean("children_access_home"),
                            rs.getBoolean("children_access_chest"),
                            rs.getBoolean("ancestors_access_home"),
                            rs.getBoolean("ancestors_access_chest"),
                            rs.getTimestamp("home_set_date").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to get family details");
        }
        return familyDetails;
    }

    @Override
    public void deleteFamilyDetails(UUID familyId) {
        String deleteSQL = "DELETE FROM " + dbName + " WHERE family_id = ?;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
            ps.setString(1, familyId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to delete family details");
        }
    }

    @Override
    public <K> void updateField(TableField<K> tableField) {
        if (!allowedFields.containsKey(tableField.getFieldName())) {
            throw new IllegalArgumentException("Invalid field name");
        }

        String updateSQL = "UPDATE " + dbName + " SET " + allowedFields.get(tableField.getFieldName()) + " = ? WHERE family_id = ?;";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSQL)) {

            ps.setString(1, tableField.getFieldValue());
            ps.setString(2, tableField.getKey().toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to update family details field: " + tableField.getFieldValue());
        }
    }
}
