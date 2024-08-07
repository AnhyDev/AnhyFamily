package ink.anh.family.fdetails;

import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.fdetails.chest.FamilyChestManager;
import ink.anh.family.fdetails.symbol.FamilySymbolManager;
import ink.anh.family.fdetails.symbol.UUIDToUniqueString;
import ink.anh.family.fplayer.FamilyUtils;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;
import ink.anh.api.enums.Access;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

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
        PlayerFamilyDBService.savePlayerFamily(spouse1, FamilyPlayerField.FAMILY_ID);
        PlayerFamilyDBService.savePlayerFamily(spouse2, FamilyPlayerField.FAMILY_ID);

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

        FamilyDetails details = FamilyDetailsGet.getFamilyDetails(familyId);
        Location chestLoc = details.getFamilyChest() != null ? details.getFamilyChest().getChestLocation() : null;
        String symbol = details.getFamilySymbol();

        FamilyDetailsDelete.deleteRootFamilyDetails(spouse1);
        spouse1.setFamilyId(null);
        PlayerFamilyDBService.savePlayerFamily(spouse1, FamilyPlayerField.FAMILY_ID);

        UUID spouse2Uuid = spouse1.getSpouse();

        if (spouse2Uuid == null) {
            return;
        }

        PlayerFamily spouse2 = FamilyUtils.getFamily(spouse2Uuid);
        if (spouse2 != null) {
            FamilyDetailsDelete.deleteRootFamilyDetails(spouse2);
            spouse2.setFamilyId(null);
            PlayerFamilyDBService.savePlayerFamily(spouse2, FamilyPlayerField.FAMILY_ID);
        }
    	if (chestLoc != null) FamilyChestManager.removeLocationFromUUIDMap(chestLoc);
    	if (symbol != null) FamilySymbolManager.removeSymbol(symbol);
    }

    public static void removeCrossFamilyRelations(PlayerFamily target, Set<PlayerFamily> relatives, boolean removeRelative, boolean initiatorSave) {
        boolean shouldSaveSpouseFamilyDetails = false;

        for (PlayerFamily relative : relatives) {
            boolean isRelativeOfSpouse = relative.getRoot().equals(target.getFather()) || 
                                          relative.getRoot().equals(target.getMother()) || 
                                          target.getChildren().contains(relative.getRoot());

            boolean shouldRemove = (removeRelative && isRelativeOfSpouse) || (!removeRelative && !isRelativeOfSpouse);

            if (shouldRemove) {
                shouldSaveSpouseFamilyDetails |= removeFamilyFromRelative(target, relative, false);
                removeFamilyFromRelative(relative, target, true);
            }
        }

        if (shouldSaveSpouseFamilyDetails && initiatorSave) {
            FamilyDetails targetDetails = FamilyDetailsGet.getRootFamilyDetails(target);
            if (targetDetails != null) {
                FamilyDetailsSave.saveFamilyDetails(targetDetails, null);
            }
        }
    }

    private static boolean removeFamilyFromRelative(PlayerFamily target, PlayerFamily relative, boolean shouldSave) {
        UUID spouseRoot = target.getRoot();

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
