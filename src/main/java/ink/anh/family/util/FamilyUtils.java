package ink.anh.family.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ink.anh.family.AnhyFamily;
import ink.anh.family.common.Family;
import ink.anh.family.common.FamilyDataHandler;
import ink.anh.family.db.AbstractFamilyTable;
import ink.anh.family.gender.Gender;
import ink.anh.family.info.FamilyTree;

public class FamilyUtils {
	
	public static void saveFamily(Family family) {
		AnhyFamily.getInstance().getDatabaseManager().getFamilyTable().insertFamily(family);
	}
	
	// Для UUID
	public static Family createNewFamily(UUID playerUUID) {
	    String displayName = "Unknown";
	    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
	    if (offlinePlayer != null) {
	        displayName = offlinePlayer.getName();
	    }

	    Family family = new Family(playerUUID, Gender.UNDECIDED, displayName, new String[2], new String[2], null, null, null, new HashSet<>());
	    saveFamily(family);
	    return family;
	}

	// Для Player
	public static Family createNewFamily(Player player) {
	    UUID playerUUID = player.getUniqueId();
	    String displayName = player.getName();

	    Family family = new Family(playerUUID, Gender.UNDECIDED, displayName, new String[2], new String[2], null, null, null, new HashSet<>());
	    saveFamily(family);
	    if (player.isOnline()) {
	    	new FamilyDataHandler().addFamilyData(playerUUID, family);
	    }
	    return family;
	}

	// Для OfflinePlayer
	public static Family createNewFamily(OfflinePlayer offlinePlayer) {
	    UUID playerUUID = offlinePlayer.getUniqueId();
	    String displayName = offlinePlayer.getName();

	    Family family = new Family(playerUUID, Gender.UNDECIDED, displayName, new String[2], new String[2], null, null, null, new HashSet<>());
	    saveFamily(family);
	    return family;
	}

	public static Family getFamily(Player onlinePlayer) {
		UUID playerUUID = onlinePlayer.getUniqueId();
	    Family family = new FamilyDataHandler().getFamilyData(playerUUID);
	    if (family == null) {
	        AbstractFamilyTable familyTable = AnhyFamily.getInstance().getDatabaseManager().getFamilyTable();
	        family = familyTable.getFamily(playerUUID, onlinePlayer.getDisplayName());
	        if (family == null) {
                family = createNewFamily(onlinePlayer);
	        }
	    }
	    return family;
	}

	public static Family getFamily(UUID playerUUID) {
	    Family family = new FamilyDataHandler().getFamilyData(playerUUID);
	    if (family == null) {
	        AbstractFamilyTable familyTable = AnhyFamily.getInstance().getDatabaseManager().getFamilyTable();
	        family = familyTable.getFamily(playerUUID);
	        if (family == null) {
	            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
	            if (offlinePlayer.hasPlayedBefore()) {
	                family = createNewFamily(playerUUID);
	            }
	        }
	    }
	    return family;
	}

	public static Family getFamily(String playerName) {
	    Player onlinePlayer = Bukkit.getPlayerExact(playerName);
	    UUID playerUUID;

	    if (onlinePlayer != null) {
		    return getFamily(onlinePlayer);
	    } else {
	        AbstractFamilyTable familyTable = AnhyFamily.getInstance().getDatabaseManager().getFamilyTable();
	        Family family = familyTable.getFamilyByDisplayName(playerName);
	        
	        if (family != null) {
	        	return family;
	        }
	        
	        @SuppressWarnings("deprecation")
	        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
	        if (offlinePlayer != null && (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline())) {
	            playerUUID = offlinePlayer.getUniqueId();
	        } else {
	            return null;
	        }
	    }

	    return getFamily(playerUUID);
	}

	public static Set<Family> clearRelatives(Family family) {
	    Set<Family> modifiedFamilies = new HashSet<>();

	    if (family == null) return modifiedFamilies;

	    modifiedFamilies.addAll(clearAllChildren(family));
	    modifiedFamilies.addAll(removeParents(family));
	    modifiedFamilies.addAll(removeSpouseAndRestoreLastName(family));

	    return modifiedFamilies;
	}

	public static Set<Family> clearAllChildren(Family family) {
	    Set<Family> modifiedFamilies = new HashSet<>();
	    if (family == null || family.getChildren() == null || family.getChildren().isEmpty()) return modifiedFamilies;

	    for (UUID childId : new HashSet<>(family.getChildren())) {
	        Family childFamily = getFamily(childId);
	        if (childFamily != null) {
	            removeChildFromParents(family, childFamily);
	            modifiedFamilies.add(childFamily);
	        }
	    }

	    family.setChildren(new HashSet<UUID>());
	    saveFamily(family);
	    modifiedFamilies.add(family);
	    return modifiedFamilies;
	}

	public static void removeChildFromParents(Family parentFamily, Family childFamily) {
	    if (parentFamily == null || childFamily == null) return;

	    // Перевірка та видалення зв'язку з батьком
	    if (childFamily.getFather() != null && childFamily.getFather().equals(parentFamily.getRoot())) {
	        childFamily.setFather(null);
	    }

	    // Перевірка та видалення зв'язку з матір'ю
	    if (childFamily.getMother() != null && childFamily.getMother().equals(parentFamily.getRoot())) {
	        childFamily.setMother(null);
	    }

	    saveFamily(childFamily); // Збереження оновленої сім'ї дитини
	}

	public static Set<Family> removeParents(Family family) {
		Set<Family> modifiedFamilies = new HashSet<>();
	    if (family == null) return modifiedFamilies;

	    UUID fatherId = family.getFather();
	    UUID motherId = family.getMother();
	    UUID childId = family.getRoot();
	    boolean isChanged = false;

	    if (fatherId != null) {
	        Family fatherFamily = getFamily(fatherId);
	        if (fatherFamily != null && fatherFamily.getChildren().remove(childId)) {
	            saveFamily(fatherFamily);
	            modifiedFamilies.add(fatherFamily);
	            isChanged = true;
	        }
	    }

	    if (motherId != null) {
	        Family motherFamily = getFamily(motherId);
	        if (motherFamily != null && motherFamily.getChildren().remove(childId)) {
	            saveFamily(motherFamily);
	            modifiedFamilies.add(motherFamily);
	            isChanged = true;
	        }
	    }

	    if (isChanged) {
	        family.setFather(null);
	        family.setMother(null);
	        saveFamily(family);
	        modifiedFamilies.add(family);
	    }
	    return modifiedFamilies;
	}

	public static Set<Family> removeSpouseAndRestoreLastName(Family family) {
		Set<Family> modifiedFamilies = new HashSet<>();
	    if (family == null) return modifiedFamilies;

	    Family spouseFamily = getFamily(family.getSpouse());

	    if (spouseFamily != null) {
	        spouseFamily.setSpouse(null);
	        if (spouseFamily.getOldLastName() != null && spouseFamily.getOldLastName().length > 0) {
	            spouseFamily.setLastName(spouseFamily.getOldLastName());
	            spouseFamily.setOldLastName(new String[2]);
	        }
	        saveFamily(spouseFamily);
	        modifiedFamilies.add(spouseFamily);
	    }

	    family.setSpouse(null);
	    if (family.getOldLastName() != null && family.getOldLastName().length > 0) {
	        family.setLastName(family.getOldLastName());
	        family.setOldLastName(new String[2]);
	    } else {
	        family.setLastName(new String[2]);
	    }
	    saveFamily(family);
	    modifiedFamilies.add(family);
	    return modifiedFamilies;
	}

	public static boolean areGendersCompatibleForTraditional(Family family1) {
    	Gender gender = family1.getGender();
        return gender != null && (gender == Gender.MALE || gender == Gender.FEMALE);
    }

	public static boolean areGendersCompatibleForTraditional(Family family1, Family family2) {
	    // Використання першого методу для перевірки кожної сім'ї окремо
	    if (!areGendersCompatibleForTraditional(family1) || !areGendersCompatibleForTraditional(family2)) {
	        return false;
	    }

	    // Додаткова перевірка на відмінність статей
	    return family1.getGender() != family2.getGender();
	}

	public static boolean hasRelatives(FamilyTree treePlayer1, UUID uuidPlayer2) {
        return treePlayer1.hasRelativesWithUUID(uuidPlayer2);
    }

	public static String getPriestTitle(Player player) {
        Gender gender = FamilyUtils.getFamily(player.getUniqueId()).getGender();
        String displayName = player.getDisplayName();
        return (gender == Gender.MALE ? "family_marry_priest_male" :
                gender == Gender.FEMALE ? "family_marry_priest_female" : "family_marry_priest_nonbinary")
                + " " + displayName;
    }

	public static String getBrideTitle(Player player) {
        Gender gender = FamilyUtils.getFamily(player.getUniqueId()).getGender();
        String displayName = player.getDisplayName();
        return (gender == Gender.MALE ? "family_marry_groom_male" :
                gender == Gender.FEMALE ? "family_marry_groom_female" : "family_marry_groom_nonbinary")
                + " " + displayName;
    }
}
