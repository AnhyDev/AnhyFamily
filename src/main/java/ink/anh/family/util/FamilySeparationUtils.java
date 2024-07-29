package ink.anh.family.util;

import ink.anh.api.messages.Logger;
import ink.anh.family.AnhyFamily;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.events.FamilySeparationReason;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsGet;
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

	        case DIVORCE_RELATIVE:
	            UUID familyId = playerFamily.getFamilyId();
	            
	            FamilyDetails details = FamilyDetailsGet.getFamilyDetails(familyId);
	            
	            if (details == null) {
	                details = FamilyDetailsGet.getRootFamilyDetails(playerFamily.getRoot());
	            }

	            if (details != null) {
	                // Додавання родичів з мап childrenAccessMap
	                for (UUID childId : details.getChildrenAccessMap().keySet()) {
	                    PlayerFamily childFamily = FamilyUtils.getFamily(childId);
	                    if (childFamily != null) {
	                        relatives.add(childFamily);
	                    }
	                }

	                // Додавання родичів з мап ancestorsAccessMap
	                for (UUID ancestorId : details.getAncestorsAccessMap().keySet()) {
	                    PlayerFamily ancestorFamily = FamilyUtils.getFamily(ancestorId);
	                    if (ancestorFamily != null) {
	                        relatives.add(ancestorFamily);
	                    }
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

    public static void removeChildAndParent(PlayerFamily parentFamily, PlayerFamily childFamily) {
        FamilyPlayerField fieldToUpdate = removeOneParents(parentFamily, childFamily, false);

        if (fieldToUpdate != null) {
        	PlayerFamilyDBService.savePlayerFamily(childFamily, fieldToUpdate);
        }
        
        if (removeOneChildren(parentFamily, childFamily, false)) {
        	PlayerFamilyDBService.savePlayerFamily(parentFamily, FamilyPlayerField.CHILDREN);
        }
    }

    public static FamilyPlayerField removeOneParents(PlayerFamily parentFamily, PlayerFamily childFamily, boolean orSave) {
        FamilyPlayerField fieldToUpdate = null;
        
        if (parentFamily == null || childFamily == null) return fieldToUpdate;

        Logger.info(AnhyFamily.getInstance(), "Start remove parent");
        Logger.info(AnhyFamily.getInstance(), "parentFamily: " + parentFamily.getRoot());
        Logger.info(AnhyFamily.getInstance(), "parentFamily Father: " + parentFamily.getFather());
        Logger.info(AnhyFamily.getInstance(), "parentFamily Mother: " + parentFamily.getMother());
        Logger.info(AnhyFamily.getInstance(), "childFamily: " + childFamily.getRoot());
        Logger.info(AnhyFamily.getInstance(), "childFamily Father: " + childFamily.getFather());
        Logger.info(AnhyFamily.getInstance(), "childFamily Mother: " + childFamily.getMother());
        // Перевірка та видалення зв'язку з батьком
        if (childFamily.getFather() != null && childFamily.getFather().equals(parentFamily.getRoot())) {
            childFamily.setFather(null);
            fieldToUpdate = FamilyPlayerField.FATHER;
            Logger.info(AnhyFamily.getInstance(), "getFather: " + childFamily.getFather());
        }

        // Перевірка та видалення зв'язку з матір'ю
        if (childFamily.getMother() != null && childFamily.getMother().equals(parentFamily.getRoot())) {
            childFamily.setMother(null);
            fieldToUpdate = FamilyPlayerField.MOTHER;
            Logger.info(AnhyFamily.getInstance(), "getMother: " + childFamily.getMother());
        }
        
        if (orSave && fieldToUpdate != null) {
        	PlayerFamilyDBService.savePlayerFamily(childFamily, fieldToUpdate);
        }
		return fieldToUpdate;
    }

    public static boolean removeOneChildren(PlayerFamily parentFamily, PlayerFamily childFamily, boolean orSave) {
        if (parentFamily == null || childFamily == null) return false;
        FamilyPlayerField fieldToUpdate = null;
        
        if (parentFamily.getChildren().remove(childFamily.getRoot())) {
        	fieldToUpdate = FamilyPlayerField.CHILDREN;
        }
        
        if (orSave && fieldToUpdate != null) {
        	PlayerFamilyDBService.savePlayerFamily(parentFamily, FamilyPlayerField.CHILDREN);
        }
        
		return fieldToUpdate != null;
    }

    public static Set<PlayerFamily> removeSpouseAndRestoreLastName(Set<PlayerFamily> spouses) {
        Set<PlayerFamily> modifiedFamilies = new HashSet<>();

        for (PlayerFamily spouseFamily : spouses) {
        	if (spouseFamily != null ) {
                spouseFamily.setSpouse(null);
                if (spouseFamily.getOldLastName() != null && spouseFamily.getOldLastName().length > 0) {
                    spouseFamily.setLastName(spouseFamily.getOldLastName());
                    spouseFamily.setOldLastName(new String[2]);
                }
                PlayerFamilyDBService.savePlayerFamily(spouseFamily, null);
                modifiedFamilies.add(spouseFamily);
        	}
        }
        return modifiedFamilies;
    }

    public static boolean separateSpouses(PlayerFamily spouse1Family, PlayerFamily spouse2Family) {
    	boolean isSeparate = false;
    	
        Set<PlayerFamily> spouses = new HashSet<>();

        if (spouse1Family != null) {
            spouses.add(spouse1Family);
            isSeparate = true;
        }

        if (spouse2Family != null) {
            spouses.add(spouse2Family);
            isSeparate = true;
        }

        removeSpouseAndRestoreLastName(spouses);
        
        return isSeparate;
    }
}
