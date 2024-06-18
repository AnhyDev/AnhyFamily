package ink.anh.family.fdetails;

import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fdetails.symbol.UUIDToUniqueString;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBServsce;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.enums.Access;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FamilyDetailsService {

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
        PlayerFamilyDBServsce.savePlayerFamily(spouse1, FamilyPlayerField.FAMILY_ID);
        PlayerFamilyDBServsce.savePlayerFamily(spouse2, FamilyPlayerField.FAMILY_ID);

        // Оновлення сімейних об'єктів родичів
        updateRelativesFamilyDetails(spouse1, spouse2);
        updateRelativesFamilyDetails(spouse2, spouse1);

        FamilyDetailsCacheManager.getInstance().addFamilyDetails(familyDetails);
        FamilySymbolManager.addSymbol(symbol, familyId);
        
        return familyDetails;
    }

    private static void addAccess(Map<UUID, AccessControl> accessMap, UUID member) {
        if (member != null) {
            accessMap.put(member, new AccessControl(Access.DEFAULT, Access.DEFAULT, Access.DEFAULT, Access.DEFAULT));
        }
    }

    private static void addChildrenAndParentsToAccessMap(PlayerFamily spouse, Map<UUID, AccessControl> childrenAccessMap, Map<UUID, AccessControl> ancestorsAccessMap) {
        // Додавання дітей до мапи доступу дітей
        Set<UUID> children = spouse.getChildren();
        if (children != null) {
            for (UUID child : children) {
                addAccess(childrenAccessMap, child);
            }
        }

        // Додавання батьків до мапи доступу батьків
        addAccess(ancestorsAccessMap, spouse.getFather());
        addAccess(ancestorsAccessMap, spouse.getMother());
    }

    private static void updateRelativesFamilyDetails(PlayerFamily spouse, PlayerFamily newSpouse) {
        // Оновлення сімейних об'єктів батьків
        addMemberToFamilyDetails(spouse.getFather(), newSpouse.getRoot(), true);
        addMemberToFamilyDetails(spouse.getMother(), newSpouse.getRoot(), true);

        // Оновлення сімейних об'єктів дітей
        Set<UUID> children = spouse.getChildren();
        if (children != null) {
            for (UUID child : children) {
                addMemberToFamilyDetails(child, newSpouse.getRoot(), false);
            }
        }
    }

    private static void addMemberToFamilyDetails(UUID targetUuid, UUID newMemberUuid, boolean isParent) {
        if (targetUuid != null && newMemberUuid != null) {
            FamilyDetails targetFamilyDetails = FamilyDetailsGet.getRootFamilyDetails(targetUuid);
            if (targetFamilyDetails != null) {
                Map<UUID, AccessControl> accessMap = isParent ? targetFamilyDetails.getChildrenAccessMap() : targetFamilyDetails.getAncestorsAccessMap();
                if (!accessMap.containsKey(newMemberUuid)) {
                    accessMap.put(newMemberUuid, new AccessControl(Access.DEFAULT, Access.DEFAULT, Access.DEFAULT, Access.DEFAULT));
                    FamilyDetailsSave.saveFamilyDetails(targetFamilyDetails, null);
                }
            }
        }
    }

    private static void addMemberToFamilyDetails(PlayerFamily targetPlayerFamily, UUID newMemberUuid, boolean isParent) {
        if (targetPlayerFamily != null && newMemberUuid != null) {
            FamilyDetails targetFamilyDetails = FamilyDetailsGet.getRootFamilyDetails(targetPlayerFamily);
            if (targetFamilyDetails != null) {
                Map<UUID, AccessControl> accessMap = isParent ? targetFamilyDetails.getChildrenAccessMap() : targetFamilyDetails.getAncestorsAccessMap();
                if (!accessMap.containsKey(newMemberUuid)) {
                    accessMap.put(newMemberUuid, new AccessControl(Access.DEFAULT, Access.DEFAULT, Access.DEFAULT, Access.DEFAULT));
                    FamilyDetailsSave.saveFamilyDetails(targetFamilyDetails, null);
                }
            }
        }
    }

    public static void handleAdoption(PlayerFamily[] adoptersFamily, PlayerFamily adoptedFamily) {
        if (adoptersFamily == null || adoptersFamily.length == 0 || adoptedFamily == null) {
            return; // Якщо немає батьків або дитини, виходимо
        }
        
        if (adoptersFamily.length == 1 || adoptersFamily.length > 1 && adoptersFamily[0].getFamilyId().equals(adoptersFamily[1].getFamilyId())) {
            addMemberToFamilyDetails(adoptersFamily[0], adoptedFamily.getRoot(), true);
        } else if (adoptersFamily.length > 1 && !adoptersFamily[0].getFamilyId().equals(adoptersFamily[1].getFamilyId())) {
            for (PlayerFamily adopter : adoptersFamily) {
                if (adopter != null && adopter.getFamilyId() != null) {
                    addMemberToFamilyDetails(adopter, adoptedFamily.getRoot(), true);
                }
            }
    	}


        // Оновлення сімейних об'єктів усиновлюваного
        updateAdoptedFamilyDetails(adoptedFamily, adoptersFamily);
    }

    private static void updateAdoptedFamilyDetails(PlayerFamily adoptedFamily, PlayerFamily[] adoptersFamily) {
        if (adoptedFamily == null || adoptersFamily == null || adoptersFamily.length == 0) {
            return; // Якщо немає усиновлюваного або усиновлювачів, виходимо
        }

        for (PlayerFamily adopter : adoptersFamily) {
            if (adopter != null) {
                addMemberToFamilyDetails(adoptedFamily.getRoot(), adopter.getRoot(), false);
            }
        }
    }

    public static void handleDivorce(PlayerFamily spouse1) {
        UUID familyId = spouse1.getFamilyId();

        if (familyId == null) {
            return;
        }

        FamilyDetailsDelete.deleteRootFamilyDetails(spouse1);

        spouse1.setFamilyId(null);
        PlayerFamilyDBServsce.savePlayerFamily(spouse1, FamilyPlayerField.FAMILY_ID);

        UUID spouse2Uuid = spouse1.getSpouse();

        if (spouse2Uuid == null) {
            return;
        }

        PlayerFamily spouse2 = FamilyUtils.getFamily(spouse2Uuid);
        if (spouse2 != null) {
            spouse2.setFamilyId(null);
            PlayerFamilyDBServsce.savePlayerFamily(spouse2, FamilyPlayerField.FAMILY_ID);
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
                shouldSaveSpouseFamilyDetails |= removeFamilyFromRelative(spouseFamily, relative, false);
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
                boolean modified = false;
                if (relativeDetails.getChildrenAccessMap().remove(spouseRoot) != null) {
                    modified = true;
                }
                if (relativeDetails.getAncestorsAccessMap().remove(spouseRoot) != null) {
                    modified = true;
                }

                if (shouldSave && modified) {
                    FamilyDetailsSave.saveFamilyDetails(relativeDetails, null);
                }

                return modified;
            }
        }
        return false;
    }
}
