package ink.anh.family.db.fdetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import ink.anh.api.database.AbstractTable;
import ink.anh.api.database.ErrorLogger;
import ink.anh.api.database.TableField;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsSerializer;

public abstract class FamilyDetailsTable extends AbstractTable<FamilyDetails> {

    protected AnhyFamily familyPlugin;

    protected static String tableCreate = FamilyDetailsField.getTableCreate();
    protected static String tableInsert = FamilyDetailsField.getTableInsert();

    public FamilyDetailsTable(AnhyFamily familyPlugin) {
        super(GlobalManager.getInstance(), "FamilyDetails");
        this.familyPlugin = familyPlugin;
        initialize();
    }

    protected void initialize() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " + dbName + tableCreate;
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(createTableSQL)) {
                ps.executeUpdate();
            }
        }, "Failed to create family details table");
    }

    public abstract FamilyDetails getFamilyDetails(UUID familyId);

    @Override
    public void update(FamilyDetails entity) {
        insert(entity);
    }

    @Override
    public void delete(FamilyDetails entity) {
        deleteFamilyDetails(entity.getFamilyId());
    }

    public void deleteFamilyDetails(UUID familyId) {
        String deleteSQL = "DELETE FROM " + dbName + " WHERE family_id = ?;";

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setString(1, familyId.toString());
                ps.executeUpdate();
            }
        }, "Failed to delete family details for family_id: " + familyId);
    }

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

    protected void insertFamilyDetails(FamilyDetails familyDetails, String insertSQL) {
        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setString(1, familyDetails.getFamilyId().toString());
                ps.setString(2, familyDetails.getFamilySymbol());
                ps.setString(3, FamilyDetailsSerializer.serializeLocation(familyDetails.getHomeLocation()));
                ps.setString(4, FamilyDetailsSerializer.serializeChest(familyDetails.getFamilyChest()));
                ps.setString(5, FamilyDetailsSerializer.serializeAccessControl(familyDetails.getChildrenAccess()));
                ps.setString(6, FamilyDetailsSerializer.serializeAccessControl(familyDetails.getAncestorsAccess()));
                ps.setString(7, FamilyDetailsSerializer.serializeAccessControlMap(familyDetails.getChildrenAccessMap()));
                ps.setString(8, FamilyDetailsSerializer.serializeAccessControlMap(familyDetails.getAncestorsAccessMap()));
                ps.setString(9, familyDetails.getHomeSetDate() != null ? familyDetails.getHomeSetDate().toString() : null);
                ps.executeUpdate();
            }
        }, "Failed to insert family details: " + familyDetails);
    }

    protected FamilyDetails getFamilyDetailsFromResultSet(ResultSet rs) throws Exception {
        return new FamilyDetails(
                UUID.fromString(rs.getString("family_id")),
                rs.getString("family_symbol"),
                FamilyDetailsSerializer.deserializeLocation(rs.getString("home_location")),
                FamilyDetailsSerializer.deserializeChest(rs.getString("family_chest")),
                FamilyDetailsSerializer.deserializeAccessControl(rs.getString("children_access")),
                FamilyDetailsSerializer.deserializeAccessControl(rs.getString("ancestors_access")),
                FamilyDetailsSerializer.deserializeAccessControlMap(rs.getString("children_access_map")),
                FamilyDetailsSerializer.deserializeAccessControlMap(rs.getString("ancestors_access_map")),
                rs.getTimestamp("home_set_date") != null ? rs.getTimestamp("home_set_date").toLocalDateTime() : null
        );
    }

    protected FamilyDetails fetchFamilyDetails(UUID familyId, String selectSQL) {
        final FamilyDetails[] familyDetails = {null};

        executeTransaction(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(selectSQL)) {
                ps.setString(1, familyId.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        familyDetails[0] = getFamilyDetailsFromResultSet(rs);
                    }
                } catch (Exception e) {
                    ErrorLogger.log(familyPlugin, e, "Failed to establish database connection");
                }
            }
        }, "Failed to get family details for family_id: " + familyId);

        return familyDetails[0];
    }
}
