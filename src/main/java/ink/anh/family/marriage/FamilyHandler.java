package ink.anh.family.marriage;

import ink.anh.family.fdetails.AccessControl;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsSave;
import ink.anh.family.fdetails.FamilyDetailsDelete;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.enums.Access;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FamilyHandler {

    public static FamilyDetails createFamilyOnMarriage(PlayerFamily spouse1, PlayerFamily spouse2) {

        // Ініціалізація FamilyDetails з подружжям
        FamilyDetails familyDetails = new FamilyDetails(spouse1.getRoot(), spouse2.getRoot());

        // Створення мап доступу для дітей та батьків
        Map<UUID, AccessControl> childrenAccessMap = new HashMap<>();
        Map<UUID, AccessControl> ancestorsAccessMap = new HashMap<>();

        // Додавання дітей та батьків подружжя до відповідних мап доступу
        addChildrenAndParentsToAccessMap(spouse1, childrenAccessMap, ancestorsAccessMap);
        addChildrenAndParentsToAccessMap(spouse2, childrenAccessMap, ancestorsAccessMap);

        // Встановлення мап доступу у FamilyDetails
        familyDetails.setChildrenAccessMap(childrenAccessMap);
        familyDetails.setAncestorsAccessMap(ancestorsAccessMap);

        // Збереження FamilyDetails
        FamilyDetailsSave.saveFamilyDetails(familyDetails, null);

        // Оновлення інформації про подружжя у PlayerFamily
        spouse1.setSpouse(spouse2.getRoot());
        spouse2.setSpouse(spouse1.getRoot());
        FamilyUtils.saveFamily(spouse1);
        FamilyUtils.saveFamily(spouse2);

        return familyDetails;
    }

    private static void addChildrenAndParentsToAccessMap(PlayerFamily spouse, Map<UUID, AccessControl> childrenAccessMap, Map<UUID, AccessControl> ancestorsAccessMap) {
        // Додавання дітей до мапи доступу дітей
        Set<UUID> children = spouse.getChildren();
        if (children != null) {
            for (UUID child : children) {
                childrenAccessMap.put(child, new AccessControl(Access.DEFAULT, Access.DEFAULT, Access.DEFAULT));
            }
        }

        // Додавання батьків до мапи доступу батьків
        UUID father = spouse.getFather();
        UUID mother = spouse.getMother();
        if (father != null) {
            ancestorsAccessMap.put(father, new AccessControl(Access.DEFAULT, Access.DEFAULT, Access.DEFAULT));
        }
        if (mother != null) {
            ancestorsAccessMap.put(mother, new AccessControl(Access.DEFAULT, Access.DEFAULT, Access.DEFAULT));
        }
    }

    public static void handleDivorce(PlayerFamily spouse1) {

        // Отримання familyId для видалення FamilyDetails
        UUID familyId = spouse1.getFamilyId();
        UUID spouse2Uuid = spouse1.getSpouse();

        if (spouse2Uuid == null) {
            return; // Якщо подружжя не встановлене, виходимо
        }

        PlayerFamily spouse2 = FamilyUtils.getFamily(spouse2Uuid);

        if (spouse2 == null) {
            return; // Якщо не вдалося знайти подружжя, виходимо
        }

        // Видалення подружжя у обох
        spouse1.setSpouse(null);
        spouse2.setSpouse(null);

        // Збереження змін у PlayerFamily
        FamilyUtils.saveFamily(spouse1);
        FamilyUtils.saveFamily(spouse2);

        if (familyId != null) {
            // Видалення FamilyDetails з кешу та бази даних
            FamilyDetailsDelete.deleteFamilyDetails(familyId);
        }
    }
}
