package ink.anh.family.db.fdetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import ink.anh.api.database.TableField;
import ink.anh.api.enums.Access;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsSerializer;

public class SQLiteFamilyDetailsTable extends FamilyDetailsTable {

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
                ps.setString(2, FamilyDetailsSerializer.serializeLocation(familyDetails.getHomeLocation()));
                ps.setString(3, FamilyDetailsSerializer.serializeFamilyChest(familyDetails.getFamilyChest()));
                ps.setString(4, familyDetails.getChildrenAccess().name());
                ps.setString(5, familyDetails.getAncestorsAccess().name());
                ps.setString(6, FamilyDetailsSerializer.serializeAccessControlMap(familyDetails.getChildrenAccessMap()));
                ps.setString(7, FamilyDetailsSerializer.serializeAccessControlMap(familyDetails.getAncestorsAccessMap()));
                ps.setString(8, familyDetails.getHomeSetDate() != null ? familyDetails.getHomeSetDate().toString() : null);
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
                                FamilyDetailsSerializer.deserializeLocation(rs.getString("home_location")),
                                FamilyDetailsSerializer.deserializeFamilyChest(rs.getString("family_chest")),
                                Access.valueOf(rs.getString("children_access")),
                                Access.valueOf(rs.getString("ancestors_access")),
                                FamilyDetailsSerializer.deserializeAccessControlMap(rs.getString("children_access_map")),
                                FamilyDetailsSerializer.deserializeAccessControlMap(rs.getString("ancestors_access_map")),
                                rs.getTimestamp("home_set_date") != null ? rs.getTimestamp("home_set_date").toLocalDateTime() : null
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
        String fieldName = tableField.getFieldName();
        if (!FamilyDetailsField.contains(fieldName)) {
            throw new IllegalArgumentException("Invalid field name");
        }

        String updateSQL = "UPDATE " + dbName + " SET " + fieldName + " = ? WHERE family_id = ?;";

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                ps.setString(1, tableField.getFieldValue());
                ps.setString(2, tableField.getKey().toString());
                ps.executeUpdate();
            }
        }, "Failed to update family details field: " + tableField.getFieldValue());
    }
}
