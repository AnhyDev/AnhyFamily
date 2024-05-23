package ink.anh.family.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ink.anh.api.messages.Logger;
import ink.anh.family.AnhyFamily;
import ink.anh.family.common.PlayerFamily;
import ink.anh.family.db.fplayer.AbstractFamilyTable;
import ink.anh.family.common.FamilyDataHandler;
import ink.anh.family.gender.Gender;
import ink.anh.family.info.FamilyTree;

public class FamilyUtils {
	
	private static AbstractFamilyTable familyTable = (AbstractFamilyTable) AnhyFamily.getInstance().getDatabaseManager().getTable(PlayerFamily.class);
	
	public static void saveFamily(PlayerFamily playerFamily) {
		familyTable.insert(playerFamily);
	}
	
	// Для UUID
	public static PlayerFamily createNewFamily(UUID playerUUID) {
	    String displayName = "Unknown";
	    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
	    if (offlinePlayer != null) {
	        displayName = offlinePlayer.getName();
	    }

	    PlayerFamily playerFamily = new PlayerFamily(playerUUID, Gender.UNDECIDED, displayName, new String[2], new String[2], null, null, null, new HashSet<>());
	    saveFamily(playerFamily);
	    return playerFamily;
	}

	// Для Player
	public static PlayerFamily createNewFamily(Player player) {
	    UUID playerUUID = player.getUniqueId();
	    String displayName = player.getName();

	    PlayerFamily playerFamily = new PlayerFamily(playerUUID, Gender.UNDECIDED, displayName, new String[2], new String[2], null, null, null, new HashSet<>());
	    saveFamily(playerFamily);
	    if (player.isOnline()) {
	    	new FamilyDataHandler().addFamilyData(playerUUID, playerFamily);
	    }
	    return playerFamily;
	}

	// Для OfflinePlayer
	public static PlayerFamily createNewFamily(OfflinePlayer offlinePlayer) {
	    UUID playerUUID = offlinePlayer.getUniqueId();
	    String displayName = offlinePlayer.getName();

	    PlayerFamily playerFamily = new PlayerFamily(playerUUID, Gender.UNDECIDED, displayName, new String[2], new String[2], null, null, null, new HashSet<>());
	    saveFamily(playerFamily);
	    return playerFamily;
	}

	public static PlayerFamily getFamily(Player onlinePlayer) {
		UUID playerUUID = onlinePlayer.getUniqueId();
	    PlayerFamily playerFamily = new FamilyDataHandler().getFamilyData(playerUUID);
	    if (playerFamily == null) {
	        playerFamily = familyTable.getFamily(playerUUID, onlinePlayer.getDisplayName());
	        if (playerFamily == null) {
                playerFamily = createNewFamily(onlinePlayer);
	        }
	    }
	    return playerFamily;
	}

	public static PlayerFamily getFamily(UUID playerUUID) {
	    Logger.info(AnhyFamily.getInstance(), "Виклик методу getFamily з параметром UUID: " + playerUUID);

	    PlayerFamily playerFamily = null;
	    try {
	        Logger.info(AnhyFamily.getInstance(), "Отримання даних FamilyDataHandler для UUID: " + playerUUID);
	        playerFamily = new FamilyDataHandler().getFamilyData(playerUUID);
	        if (playerFamily == null) {
	            Logger.info(AnhyFamily.getInstance(), "FamilyDataHandler не повернув дані, перевірка familyTable для UUID: " + playerUUID);
	            playerFamily = familyTable.getFamily(playerUUID);
	            if (playerFamily == null) {
	                Logger.info(AnhyFamily.getInstance(), "familyTable не повернув дані, перевірка OfflinePlayer для UUID: " + playerUUID);
	                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
	                if (offlinePlayer.hasPlayedBefore()) {
	                    Logger.info(AnhyFamily.getInstance(), "OfflinePlayer має історію гри, створення нової сім'ї для UUID: " + playerUUID);
	                    playerFamily = createNewFamily(playerUUID);
	                } else {
	                    Logger.warn(AnhyFamily.getInstance(), "OfflinePlayer не має історії гри для UUID: " + playerUUID);
	                }
	            } else {
	                Logger.info(AnhyFamily.getInstance(), "Отримані дані з familyTable для UUID: " + playerUUID);
	            }
	        } else {
	            Logger.info(AnhyFamily.getInstance(), "Отримані дані з FamilyDataHandler для UUID: " + playerUUID);
	        }
	    } catch (Exception e) {
	        Logger.error(AnhyFamily.getInstance(), "Виникла помилка під час отримання сім'ї для UUID: " + playerUUID);
	        Logger.error(AnhyFamily.getInstance(), "Повідомлення про помилку: " + e.getMessage());
	        e.printStackTrace();
	    }

	    if (playerFamily == null) {
	        Logger.warn(AnhyFamily.getInstance(), "Не вдалося знайти або створити сім'ю для UUID: " + playerUUID);
	    } else {
	        Logger.info(AnhyFamily.getInstance(), "Успішно знайдена або створена сім'я для UUID: " + playerUUID);
	    }

	    return playerFamily;
	}

	public static PlayerFamily getFamily(String playerName) {
	    Player onlinePlayer = Bukkit.getPlayerExact(playerName);
	    UUID playerUUID;

	    if (onlinePlayer != null) {
		    return getFamily(onlinePlayer);
	    } else {
	        PlayerFamily playerFamily = familyTable.getFamilyByDisplayName(playerName);
	        
	        if (playerFamily != null) {
	        	return playerFamily;
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

	public static Set<PlayerFamily> clearRelatives(PlayerFamily playerFamily) {
	    Set<PlayerFamily> modifiedFamilies = new HashSet<>();

	    if (playerFamily == null) return modifiedFamilies;

	    modifiedFamilies.addAll(clearAllChildren(playerFamily));
	    modifiedFamilies.addAll(removeParents(playerFamily));
	    modifiedFamilies.addAll(removeSpouseAndRestoreLastName(playerFamily));

	    return modifiedFamilies;
	}

	public static Set<PlayerFamily> clearAllChildren(PlayerFamily playerFamily) {
	    Set<PlayerFamily> modifiedFamilies = new HashSet<>();
	    if (playerFamily == null || playerFamily.getChildren() == null || playerFamily.getChildren().isEmpty()) return modifiedFamilies;

	    for (UUID childId : new HashSet<>(playerFamily.getChildren())) {
	        PlayerFamily childFamily = getFamily(childId);
	        if (childFamily != null) {
	            removeChildFromParents(playerFamily, childFamily);
	            modifiedFamilies.add(childFamily);
	        }
	    }

	    playerFamily.setChildren(new HashSet<UUID>());
	    saveFamily(playerFamily);
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

	    saveFamily(childFamily); // Збереження оновленої сім'ї дитини
	}

	public static Set<PlayerFamily> removeParents(PlayerFamily playerFamily) {
		Set<PlayerFamily> modifiedFamilies = new HashSet<>();
	    if (playerFamily == null) return modifiedFamilies;

	    UUID fatherId = playerFamily.getFather();
	    UUID motherId = playerFamily.getMother();
	    UUID childId = playerFamily.getRoot();
	    boolean isChanged = false;

	    if (fatherId != null) {
	        PlayerFamily fatherFamily = getFamily(fatherId);
	        if (fatherFamily != null && fatherFamily.getChildren().remove(childId)) {
	            saveFamily(fatherFamily);
	            modifiedFamilies.add(fatherFamily);
	            isChanged = true;
	        }
	    }

	    if (motherId != null) {
	        PlayerFamily motherFamily = getFamily(motherId);
	        if (motherFamily != null && motherFamily.getChildren().remove(childId)) {
	            saveFamily(motherFamily);
	            modifiedFamilies.add(motherFamily);
	            isChanged = true;
	        }
	    }

	    if (isChanged) {
	        playerFamily.setFather(null);
	        playerFamily.setMother(null);
	        saveFamily(playerFamily);
	        modifiedFamilies.add(playerFamily);
	    }
	    return modifiedFamilies;
	}

	public static Set<PlayerFamily> removeSpouseAndRestoreLastName(PlayerFamily playerFamily) {
		Set<PlayerFamily> modifiedFamilies = new HashSet<>();
	    if (playerFamily == null) return modifiedFamilies;

	    PlayerFamily spouseFamily = getFamily(playerFamily.getSpouse());

	    if (spouseFamily != null) {
	        spouseFamily.setSpouse(null);
	        if (spouseFamily.getOldLastName() != null && spouseFamily.getOldLastName().length > 0) {
	            spouseFamily.setLastName(spouseFamily.getOldLastName());
	            spouseFamily.setOldLastName(new String[2]);
	        }
	        saveFamily(spouseFamily);
	        modifiedFamilies.add(spouseFamily);
	    }

	    playerFamily.setSpouse(null);
	    if (playerFamily.getOldLastName() != null && playerFamily.getOldLastName().length > 0) {
	        playerFamily.setLastName(playerFamily.getOldLastName());
	        playerFamily.setOldLastName(new String[2]);
	    } else {
	        playerFamily.setLastName(new String[2]);
	    }
	    saveFamily(playerFamily);
	    modifiedFamilies.add(playerFamily);
	    return modifiedFamilies;
	}

	public static boolean areGendersCompatibleForTraditional(PlayerFamily family1) {
    	Gender gender = family1.getGender();
        return gender != null && (gender == Gender.MALE || gender == Gender.FEMALE);
    }

	public static boolean areGendersCompatibleForTraditional(PlayerFamily family1, PlayerFamily family2) {
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
		if (player == null) {
			return "family_marry_private_prefix";
		}
		
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

	public static String selectSurname(String[] surnames, Gender gender) {
        if (surnames == null || surnames.length == 0) {
            return ""; // Якщо масив порожній або null, повертаємо порожній рядок
        }
        if (surnames.length == 1 || gender == Gender.MALE || gender == Gender.NON_BINARY) {
            // Якщо масив містить лише одне прізвище або гендер чоловічий/небінарний, повертаємо перше прізвище
            return surnames[0] != null ? surnames[0] : "";
        } else {
            // Якщо масив містить більше одного прізвища і гендер жіночий, спробуємо використати друге прізвище
            // Якщо друге прізвище відсутнє або null, повертаємо перше
            return (surnames[1] != null) ? surnames[1] : surnames[0] != null ? surnames[0] : "";
        }
    }
}
