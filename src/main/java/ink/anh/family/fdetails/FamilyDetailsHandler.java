package ink.anh.family.fdetails;

import ink.anh.family.fdetails.symbol.UUIDToUniqueString;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.enums.Access;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FamilyDetailsHandler {

    public static FamilyDetails createFamilyOnMarriage(PlayerFamily spouse1, PlayerFamily spouse2) {

        // Ініціалізація FamilyDetails з подружжям
        FamilyDetails familyDetails = new FamilyDetails(spouse1.getRoot(), spouse2.getRoot());
        UUID familyId = familyDetails.getFamilyId();

        // Створення мап доступу для дітей та батьків
        Map<UUID, AccessControl> childrenAccessMap = new HashMap<>();
        Map<UUID, AccessControl> ancestorsAccessMap = new HashMap<>();

        // Додавання дітей та батьків подружжя до відповідних мап доступу
        addChildrenAndParentsToAccessMap(spouse1, childrenAccessMap, ancestorsAccessMap);
        addChildrenAndParentsToAccessMap(spouse2, childrenAccessMap, ancestorsAccessMap);

        // Встановлення мап доступу у FamilyDetails
        familyDetails.setChildrenAccessMap(childrenAccessMap);
        familyDetails.setAncestorsAccessMap(ancestorsAccessMap);

        String symbol = UUIDToUniqueString.getUniqueStringFromUUID(familyId);
        familyDetails.setFamilySymbol(symbol);
        
        // Збереження FamilyDetails
        FamilyDetailsSave.saveFamilyDetails(familyDetails, null);

        // Оновлення інформації про familyId у PlayerFamily
        spouse1.setFamilyId(familyId);
        spouse2.setFamilyId(familyId);
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
        UUID familyId = spouse1.getFamilyId();
        
        if (familyId == null) {
        	return;
        }
        
        FamilyDetailsDelete.deleteRootFamilyDetails(spouse1);

        spouse1.setFamilyId(null);
        FamilyUtils.saveFamily(spouse1);
        
        UUID spouse2Uuid = spouse1.getSpouse();

        if (spouse2Uuid == null) {
            return;
        }

        PlayerFamily spouse2 = FamilyUtils.getFamily(spouse2Uuid);
        if (spouse2 != null) {
            spouse2.setFamilyId(null);
            FamilyUtils.saveFamily(spouse2);
        }
    }

    public static void removeCrossFamilyRelations(PlayerFamily spouseFamily, Set<PlayerFamily> relatives, boolean removeRelative, boolean initiatorSave) {
        boolean shouldSaveSpouseFamilyDetails = false;

        for (PlayerFamily relative : relatives) {
            boolean isRelativeOfSpouse1 = relative.getRoot().equals(spouseFamily.getFather()) || 
                                          relative.getRoot().equals(spouseFamily.getMother()) || 
                                          spouseFamily.getChildren().contains(relative.getRoot());

            boolean shouldRemove = (removeRelative && isRelativeOfSpouse1) || (!removeRelative && !isRelativeOfSpouse1);

            if (shouldRemove) {
                if (removeFamilyFromRelative(spouseFamily, relative, false)) {
                    shouldSaveSpouseFamilyDetails = true;
                }
                removeFamilyFromRelative(relative, spouseFamily, true);
            }
        }

        if (shouldSaveSpouseFamilyDetails && initiatorSave) {
            FamilyDetails spouseDetails = FamilyDetailsGet.getRootFamilyDetails(spouseFamily);
            if (spouseDetails != null) {
                FamilyDetailsSave.saveFamilyDetails(spouseDetails, null);
            }
        }
    }

    private static boolean removeFamilyFromRelative(PlayerFamily spouseFamily, PlayerFamily relative, boolean shouldSave) {
        UUID spouseRoot = spouseFamily.getRoot();

        if (relative.getFamilyId() != null) {
            FamilyDetails relativeDetails = FamilyDetailsGet.getRootFamilyDetails(relative);

            if (relativeDetails != null) {
                boolean childRemoved = relativeDetails.getChildrenAccessMap().remove(spouseRoot) != null;
                boolean ancestorRemoved = relativeDetails.getAncestorsAccessMap().remove(spouseRoot) != null;

                if (shouldSave) {
                    FamilyDetailsSave.saveFamilyDetails(relativeDetails, null);
                }

                return childRemoved || ancestorRemoved;
            }
        }
        return false;
    }
}
