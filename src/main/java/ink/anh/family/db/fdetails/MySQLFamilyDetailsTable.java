package ink.anh.family.db.fdetails;

import java.util.UUID;

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
        return fetchFamilyDetails(familyId, selectSQL);
    }
}
