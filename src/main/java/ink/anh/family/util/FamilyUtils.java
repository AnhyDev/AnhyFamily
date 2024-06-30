package ink.anh.family.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import ink.anh.family.fplayer.FamilyCacheManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBServsce;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.info.FamilyTree;

public class FamilyUtils {

    // Для Player
    public static PlayerFamily createNewFamily(Player player) {
        UUID playerUUID = player.getUniqueId();
        String displayName = player.getName();

        PlayerFamily playerFamily = new PlayerFamily(playerUUID, displayName);
        PlayerFamilyDBServsce.savePlayerFamily(playerFamily, null);
        if (player.isOnline()) {
            FamilyCacheManager.getInstance().addFamily(playerFamily);
        }
        return playerFamily;
    }

    // Для OfflinePlayer
    public static PlayerFamily createNewFamily(OfflinePlayer offlinePlayer) {
        UUID playerUUID = offlinePlayer.getUniqueId();
        String displayName = offlinePlayer.getName();

        PlayerFamily playerFamily = new PlayerFamily(playerUUID, displayName);
        PlayerFamilyDBServsce.savePlayerFamily(playerFamily, null);
        return playerFamily;
    }

	public static PlayerFamily getFamily(Player onlinePlayer) {
		if (onlinePlayer == null) {
			return null;
		}
		
		UUID playerUUID = onlinePlayer.getUniqueId();
	    PlayerFamily playerFamily = FamilyCacheManager.getInstance().getFamilyData(playerUUID);
	    if (playerFamily == null) {
	        playerFamily = PlayerFamilyDBServsce.getFamilyPlayerTable().getFamily(playerUUID, onlinePlayer.getDisplayName());
	        if (playerFamily == null) {
                playerFamily = createNewFamily(onlinePlayer);
	        }
	    }
	    return playerFamily;
	}

	public static PlayerFamily getFamily(UUID playerUUID) {
		if (playerUUID == null) {
			return null;
		}
		
	    PlayerFamily playerFamily = null;
	    try {
	        playerFamily = FamilyCacheManager.getInstance().getFamilyData(playerUUID);
	        if (playerFamily == null) {
	            playerFamily = PlayerFamilyDBServsce.getFamilyPlayerTable().getFamily(playerUUID);
	            if (playerFamily == null) {
	                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
	                if (offlinePlayer.hasPlayedBefore()) {
	                    playerFamily = createNewFamily(offlinePlayer);
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return playerFamily;
	}

	public static PlayerFamily getFamily(String playerName) {
		if (playerName == null) {
			return null;
		}
		
	    Player onlinePlayer = Bukkit.getPlayerExact(playerName);
	    UUID playerUUID;

	    if (onlinePlayer != null) {
		    return getFamily(onlinePlayer);
	    } else {
	        PlayerFamily playerFamily = PlayerFamilyDBServsce.getFamilyPlayerTable().getFamilyByDisplayName(playerName.toLowerCase());
	        
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
		
        Gender gender = getFamily(player.getUniqueId()).getGender();
        String displayName = player.getDisplayName();
        return (gender == Gender.MALE ? "family_marry_priest_male" :
                gender == Gender.FEMALE ? "family_marry_priest_female" : "family_marry_priest_nonbinary")
                + " " + displayName;
    }

	public static String getBrideTitle(Player player) {
        Gender gender = getFamily(player.getUniqueId()).getGender();
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

    public static RelationshipDegree getRelationshipDegree(PlayerFamily playerFamily, UUID uuid) {
        if (uuid == null) {
            return RelationshipDegree.UNKNOWN;
        }

        if (uuid.equals(playerFamily.getSpouse())) {
            return RelationshipDegree.SPOUSE;
        } else if (uuid.equals(playerFamily.getFather())) {
            return RelationshipDegree.FATHER;
        } else if (uuid.equals(playerFamily.getMother())) {
            return RelationshipDegree.MOTHER;
        } else if (playerFamily.getChildren().contains(uuid)) {
            return RelationshipDegree.GRANDCHILD;
        } else {
            PlayerFamily fatherFamily = FamilyUtils.getFamily(playerFamily.getFather());
            PlayerFamily motherFamily = FamilyUtils.getFamily(playerFamily.getMother());

            if (fatherFamily != null) {
                if (fatherFamily.getFather() != null && fatherFamily.getFather().equals(uuid)) {
                    return RelationshipDegree.GRANDPARENT;
                }
                if (fatherFamily.getMother() != null && fatherFamily.getMother().equals(uuid)) {
                    return RelationshipDegree.GRANDPARENT;
                }
            }

            if (motherFamily != null) {
                if (motherFamily.getFather() != null && motherFamily.getFather().equals(uuid)) {
                    return RelationshipDegree.GRANDPARENT;
                }
                if (motherFamily.getMother() != null && motherFamily.getMother().equals(uuid)) {
                    return RelationshipDegree.GRANDPARENT;
                }
            }

            for (UUID child : playerFamily.getChildren()) {
                PlayerFamily childFamily = FamilyUtils.getFamily(child);
                if (childFamily != null) {
                    if (childFamily.getChildren().contains(uuid)) {
                        return RelationshipDegree.GREAT_GRANDCHILD;
                    }
                }
            }

            if (fatherFamily != null) {
                for (UUID sibling : fatherFamily.getChildren()) {
                    if (sibling.equals(uuid)) {
                        return RelationshipDegree.UNKNOWN;  // Не є прямим родичем
                    }
                    PlayerFamily siblingFamily = FamilyUtils.getFamily(sibling);
                    if (siblingFamily != null && siblingFamily.getChildren().contains(uuid)) {
                        return RelationshipDegree.GREAT_GRANDCHILD;
                    }
                }
            }

            if (motherFamily != null) {
                for (UUID sibling : motherFamily.getChildren()) {
                    if (sibling.equals(uuid)) {
                        return RelationshipDegree.UNKNOWN;  // Не є прямим родичем
                    }
                    PlayerFamily siblingFamily = FamilyUtils.getFamily(sibling);
                    if (siblingFamily != null && siblingFamily.getChildren().contains(uuid)) {
                        return RelationshipDegree.GREAT_GRANDCHILD;
                    }
                }
            }
        }

        return RelationshipDegree.UNKNOWN;
    }
}
