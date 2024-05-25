package ink.anh.family.db.fdetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import ink.anh.api.database.TableField;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FDetailsSerializator;

public class SQLiteFamilyDetailsTable extends AbstractFamilyDetailsTable {

    public SQLiteFamilyDetailsTable(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    @Override
    protected void initialize() {
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS " + dbName + tableCreate;
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
                ps.executeUpdate();
            }
        }, "Failed to create family details tableInsert");
    }

    @Override
    public void insert(FamilyDetails familyDetails) {
        String insertSQL =
                "INSERT OR REPLACE INTO " + dbName + tableInsert + ";";

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
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
            }
        }, "Failed to insert family details: " + familyDetails);
    }

    @Override
    public FamilyDetails getFamilyDetails(UUID familyId) {
        String selectSQL = "SELECT * FROM " + dbName + " WHERE family_id = ?;";
        final FamilyDetails[] familyDetails = {null};

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(selectSQL)) {
                ps.setString(1, familyId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        familyDetails[0] = new FamilyDetails(
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
            }
        }, "Failed to get family details for family_id: " + familyId);

        return familyDetails[0];
    }

    @Override
    public void deleteFamilyDetails(UUID familyId) {
        String deleteSQL = "DELETE FROM " + dbName + " WHERE family_id = ?;";

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, familyId.toString());
                ps.executeUpdate();
            }
        }, "Failed to delete family details for family_id: " + familyId);
    }

    @Override
    public <K> void updateField(TableField<K> tableField) {
        if (!allowedFields.containsKey(tableField.getFieldName())) {
            throw new IllegalArgumentException("Invalid field name");
        }

        String updateSQL = "UPDATE " + dbName + " SET " + allowedFields.get(tableField.getFieldName()) + " = ? WHERE family_id = ?;";

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                ps.setString(1, tableField.getFieldValue());
                ps.setString(2, tableField.getKey().toString());
                ps.executeUpdate();
            }
        }, "Failed to update family details field: " + tableField.getFieldValue());
    }
}
