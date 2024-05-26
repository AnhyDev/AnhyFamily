package ink.anh.family.db.fdetails;

import java.util.UUID;

import ink.anh.api.database.AbstractTable;
import ink.anh.family.AnhyFamily;
import ink.anh.family.fdetails.FamilyDetails;

public abstract class AbstractFamilyDetailsTable extends AbstractTable<FamilyDetails> {

	protected AnhyFamily familyPlugin;

	protected static String tableCreate = FamilyDetailsField.getTableCreate();
    protected static String tableInsert = FamilyDetailsField.getTableInsert();

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
