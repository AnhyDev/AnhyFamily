package ink.anh.family.util;

import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.events.FamilySeparationReason;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FamilySeparationUtils {

    public static Set<PlayerFamily> getRelatives(PlayerFamily playerFamily, FamilySeparationReason reason) {
        Set<PlayerFamily> relatives = new HashSet<>();

        if (playerFamily == null) return relatives;

        switch (reason) {
            case DIVORCE:
                UUID spouseId = playerFamily.getSpouse();
                if (spouseId != null) {
                    PlayerFamily spouse = FamilyUtils.getFamily(spouseId);
                    if (spouse != null) {
                        relatives.add(spouse);
                    }
                }
                break;

            case DISOWN_CHILD:
                if (playerFamily.getChildren() != null) {
                    for (UUID childId : playerFamily.getChildren()) {
                        PlayerFamily childFamily = FamilyUtils.getFamily(childId);
                        if (childFamily != null) {
                            relatives.add(childFamily);
                        }
                    }
                }
                break;

            case DISOWN_PARENT:
                UUID fatherId = playerFamily.getFather();
                UUID motherId = playerFamily.getMother();

                if (fatherId != null) {
                    PlayerFamily fatherFamily = FamilyUtils.getFamily(fatherId);
                    if (fatherFamily != null) {
                        relatives.add(fatherFamily);
                    }
                }

                if (motherId != null) {
                    PlayerFamily motherFamily = FamilyUtils.getFamily(motherId);
                    if (motherFamily != null) {
                        relatives.add(motherFamily);
                    }
                }
                break;

            case FULL_SEPARATION:
                // Додавання всіх дітей
                if (playerFamily.getChildren() != null) {
                    for (UUID childId : playerFamily.getChildren()) {
                        PlayerFamily childFamily = FamilyUtils.getFamily(childId);
                        if (childFamily != null) {
                            relatives.add(childFamily);
                        }
                    }
                }

                // Додавання батьків
                UUID fId = playerFamily.getFather();
                UUID mId = playerFamily.getMother();

                if (fId != null) {
                    PlayerFamily fFamily = FamilyUtils.getFamily(fId);
                    if (fFamily != null) {
                        relatives.add(fFamily);
                    }
                }

                if (mId != null) {
                    PlayerFamily mFamily = FamilyUtils.getFamily(mId);
                    if (mFamily != null) {
                        relatives.add(mFamily);
                    }
                }

                // Додавання подружжя
                UUID spId = playerFamily.getSpouse();
                if (spId != null) {
                    PlayerFamily spouse = FamilyUtils.getFamily(spId);
                    if (spouse != null) {
                        relatives.add(spouse);
                    }
                }
                break;
        }

        return relatives;
    }

    public static Set<PlayerFamily> clearRelatives(PlayerFamily playerFamily, FamilySeparationReason reason) {
        Set<PlayerFamily> modifiedFamilies = new HashSet<>();

        if (playerFamily == null) return modifiedFamilies;

        Set<PlayerFamily> relatives = getRelatives(playerFamily, reason);
        switch (reason) {
            case DIVORCE:
                modifiedFamilies.addAll(removeSpouseAndRestoreLastName(playerFamily, relatives));
                break;

            case DISOWN_CHILD:
                modifiedFamilies.addAll(clearAllChildren(playerFamily, relatives));
                break;

            case DISOWN_PARENT:
                modifiedFamilies.addAll(removeParents(playerFamily, relatives));
                break;

            case FULL_SEPARATION:
                modifiedFamilies.addAll(clearAllChildren(playerFamily, relatives));
                modifiedFamilies.addAll(removeParents(playerFamily, relatives));
                modifiedFamilies.addAll(removeSpouseAndRestoreLastName(playerFamily, relatives));
                break;
        }

        return modifiedFamilies;
    }

    public static Set<PlayerFamily> clearAllChildren(PlayerFamily playerFamily, Set<PlayerFamily> children) {
        Set<PlayerFamily> modifiedFamilies = new HashSet<>();

        for (PlayerFamily childFamily : children) {
            removeChildFromParents(playerFamily, childFamily);
            modifiedFamilies.add(childFamily);
        }

        playerFamily.setChildren(new HashSet<UUID>());
        PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.CHILDREN);
        modifiedFamilies.add(playerFamily);
        return modifiedFamilies;
    }

    public static void removeChildFromParents(PlayerFamily parentFamily, PlayerFamily childFamily) {
        if (parentFamily == null || childFamily == null) return;

        // Перевірка та видалення зв'язку з батьком
        if (childFamily.getFather() != null && childFamily.getFather().equals(parentFamily.getRoot())) {
            childFamily.setFather(null);
        }

        // Перевірка та видалення зв'язку з матір'ю
        if (childFamily.getMother() != null && childFamily.getMother().equals(parentFamily.getRoot())) {
            childFamily.setMother(null);
        }

        PlayerFamilyDBService.savePlayerFamily(childFamily, null);
    }

    public static Set<PlayerFamily> removeParents(PlayerFamily playerFamily, Set<PlayerFamily> parents) {
        Set<PlayerFamily> modifiedFamilies = new HashSet<>();

        for (PlayerFamily parentFamily : parents) {
            parentFamily.getChildren().remove(playerFamily.getRoot());
            PlayerFamilyDBService.savePlayerFamily(parentFamily, FamilyPlayerField.CHILDREN);
            modifiedFamilies.add(parentFamily);
        }

        playerFamily.setFather(null);
        playerFamily.setMother(null);;
        PlayerFamilyDBService.savePlayerFamily(playerFamily, null);
        modifiedFamilies.add(playerFamily);
        return modifiedFamilies;
    }

    public static Set<PlayerFamily> removeSpouseAndRestoreLastName(PlayerFamily playerFamily, Set<PlayerFamily> spouses) {
        Set<PlayerFamily> modifiedFamilies = new HashSet<>();

        for (PlayerFamily spouseFamily : spouses) {
            spouseFamily.setSpouse(null);
            if (spouseFamily.getOldLastName() != null && spouseFamily.getOldLastName().length > 0) {
                spouseFamily.setLastName(spouseFamily.getOldLastName());
                spouseFamily.setOldLastName(new String[2]);
            }
            PlayerFamilyDBService.savePlayerFamily(spouseFamily, null);
            modifiedFamilies.add(spouseFamily);
        }

        playerFamily.setSpouse(null);
        if (playerFamily.getOldLastName() != null && playerFamily.getOldLastName().length > 0) {
            playerFamily.setLastName(playerFamily.getOldLastName());
            playerFamily.setOldLastName(new String[2]);
        } else {
            playerFamily.setLastName(new String[2]);
        }
        PlayerFamilyDBService.savePlayerFamily(playerFamily, null);
        modifiedFamilies.add(playerFamily);
        return modifiedFamilies;
    }
}
