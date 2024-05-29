package ink.anh.family.db.fdetails;

import java.util.UUID;

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
        return fetchFamilyDetails(familyId, selectSQL);
    }
}
