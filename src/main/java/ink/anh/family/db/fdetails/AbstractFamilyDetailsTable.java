package ink.anh.family.db.fdetails;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ink.anh.api.database.AbstractTable;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.FamilyDetails;

public abstract class AbstractFamilyDetailsTable extends AbstractTable<FamilyDetails> {

	protected AnhyFamily familyPlugin;
    protected static final Map<String, String> allowedFields = new HashMap<>();

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

    protected static String tableCreate = " (" +
            "family_id VARCHAR(36) PRIMARY KEY," +
            "home_location TEXT," +
            "family_chest TEXT," +
            "children_access_home BOOLEAN," +
            "children_access_chest BOOLEAN," +
            "ancestors_access_home BOOLEAN," +
            "ancestors_access_chest BOOLEAN," +
            "specific_access_map TEXT," +
            "home_set_date TEXT" +
            ");";

    protected static String tableInsert = " (family_id, home_location, family_chest, children_access_home, children_access_chest, ancestors_access_home, ancestors_access_chest, specific_access_map, home_set_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public AbstractFamilyDetailsTable(AnhyFamily familyPlugin) {
        super(familyPlugin.getGlobalManager(), "FamilyDetails");
        this.familyPlugin = familyPlugin;
        initialize();
    }

    public abstract FamilyDetails getFamilyDetails(UUID familyId);

    public abstract void deleteFamilyDetails(UUID familyId);

    @Override
    public void update(FamilyDetails entity) {
        insert(entity);
    }

    @Override
    public void delete(FamilyDetails entity) {
        deleteFamilyDetails(entity.getFamilyId());
    }
}
