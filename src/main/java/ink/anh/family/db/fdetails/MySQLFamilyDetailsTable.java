package ink.anh.family.db.fdetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import ink.anh.api.database.ErrorLogger;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.FamilyDetails;

public class MySQLFamilyDetailsTable extends FamilyDetailsTable {

    public MySQLFamilyDetailsTable(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    @Override
    public void insert(FamilyDetails familyDetails) {
        String insertSQL =
                "INSERT INTO " + dbName + tableInsert + " " +
                "ON DUPLICATE KEY UPDATE " + FamilyDetailsField.getUpdateFields();

        insertFamilyDetails(familyDetails, insertSQL);
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
