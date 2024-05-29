package ink.anh.family.db.fdetails;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import ink.anh.api.database.ErrorLogger;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.FamilyDetails;

public class SQLiteFamilyDetailsTable extends FamilyDetailsTable {

    public SQLiteFamilyDetailsTable(AnhyFamily familyPlugin) {
        super(familyPlugin);
    }

    @Override
    public void insert(FamilyDetails familyDetails) {
        String insertSQL =
                "INSERT OR REPLACE INTO " + dbName + tableInsert + ";";

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
