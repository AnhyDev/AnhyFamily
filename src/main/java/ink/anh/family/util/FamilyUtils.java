package ink.anh.family.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ink.anh.family.GlobalManager;
import ink.anh.family.db.fplayer.FamilyPlayerTable;
import ink.anh.family.fplayer.FamilyDataHandler;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.gender.Gender;
import ink.anh.family.info.FamilyTree;

public class FamilyUtils {
	
	private static FamilyPlayerTable familyTable = (FamilyPlayerTable) GlobalManager.getInstance().getDatabaseManager().getTable(PlayerFamily.class);
	
	public static void saveFamily(PlayerFamily playerFamily) {
		familyTable.insert(playerFamily);
	}
	
    // Для UUID
    public static PlayerFamily createNewFamily(UUID playerUUID) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        String displayName = (offlinePlayer != null) ? offlinePlayer.getName() : "Unknown";

        PlayerFamily playerFamily = new PlayerFamily(playerUUID, displayName);
        saveFamily(playerFamily);
        return playerFamily;
    }

    // Для Player
    public static PlayerFamily createNewFamily(Player player) {
        UUID playerUUID = player.getUniqueId();
        String displayName = player.getName();

        PlayerFamily playerFamily = new PlayerFamily(playerUUID, displayName);
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

        PlayerFamily playerFamily = new PlayerFamily(playerUUID, displayName);
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

	    PlayerFamily playerFamily = null;
	    try {
	        playerFamily = new FamilyDataHandler().getFamilyData(playerUUID);
	        if (playerFamily == null) {
	            playerFamily = familyTable.getFamily(playerUUID);
	            if (playerFamily == null) {
	                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
	                if (offlinePlayer.hasPlayedBefore()) {
	                    playerFamily = createNewFamily(playerUUID);
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
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
