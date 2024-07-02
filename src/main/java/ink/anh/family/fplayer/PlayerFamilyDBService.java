package ink.anh.family.fplayer;

import ink.anh.family.GlobalManager;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.db.fplayer.FamilyPlayerTable;
import ink.anh.api.database.TableField;
import java.util.UUID;

public class PlayerFamilyDBService {

    private static FamilyPlayerTable familyPlayerTable = (FamilyPlayerTable) GlobalManager.getInstance().getDatabaseManager().getTable(PlayerFamily.class);

	private static FamilyCacheManager dataHandler = FamilyCacheManager.getInstance();

	public static FamilyPlayerTable getFamilyPlayerTable() {
		return familyPlayerTable;
	}

    public static void savePlayerFamily(PlayerFamily playerFamily, FamilyPlayerField fieldToUpdate) {
        dataHandler.addFamily(playerFamily);

        if (fieldToUpdate == null) {
            // Додаємо нові дані
            familyPlayerTable.insert(playerFamily);
        } else {
            // Оновлюємо тільки вказане поле
            updateSingleField(playerFamily, fieldToUpdate);
        }
    }

    private static void updateSingleField(PlayerFamily playerFamily, FamilyPlayerField fieldToUpdate) {
        UUID rootId = playerFamily.getRoot();

        if (familyPlayerTable.getFamily(rootId) == null) {
            // Додаємо нові дані
            familyPlayerTable.insert(playerFamily);
            return;
        }

        String fieldValue;
        switch (fieldToUpdate) {
            case GENDER:
                fieldValue = playerFamily.getGender().toString();
                break;
            case DISPLAY_NAME:
                fieldValue = playerFamily.getLoverCaseName();
                break;
            case FIRST_NAME:
                fieldValue = playerFamily.getFirstName();
                break;
            case LAST_NAME:
                fieldValue = String.join(",", playerFamily.getLastName());
                break;
            case OLD_LAST_NAME:
                fieldValue = String.join(",", playerFamily.getOldLastName());
                break;
            case FATHER:
                fieldValue = playerFamily.getFather() != null ? playerFamily.getFather().toString() : null;
                break;
            case MOTHER:
                fieldValue = playerFamily.getMother() != null ? playerFamily.getMother().toString() : null;
                break;
            case SPOUSE:
                fieldValue = playerFamily.getSpouse() != null ? playerFamily.getSpouse().toString() : null;
                break;
            case CHILDREN:
                fieldValue = PlayerFamilySerializer.serializeUuidSet(playerFamily.getChildren());
                break;
            case FAMILY_ID:
                fieldValue = playerFamily.getFamilyId() != null ? playerFamily.getFamilyId().toString() : null;
                break;
            case PERMISSIONS_MAP:
                fieldValue = PlayerFamilySerializer.serializePermissionsMap(playerFamily.getPermissionsMap());
                break;
            default:
                // Якщо поле не існує в `enum`, оновлюємо весь об'єкт
                familyPlayerTable.update(playerFamily);
                return;
        }

        familyPlayerTable.updateField(new TableField<>(rootId, fieldToUpdate.getFieldName(), fieldValue));
    }
}
