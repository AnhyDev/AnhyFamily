package ink.anh.family.gender;

import java.util.UUID;

import org.bukkit.entity.Player;

import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;

public class GenderManager {

    public static Gender getGender(Player player) {
        Gender gender = FamilyUtils.getFamily(player).getGender();
        if (gender == null) {
            gender = Gender.UNDECIDED;
        }
        return gender;
    }

    public static Gender getGender(String playerName) {
    	PlayerFamily playerFamily = FamilyUtils.getFamily(playerName);
    	Gender gender = null;
        if (playerFamily != null) {
            gender = playerFamily.getGender();
        }
        return gender;
    }

    public static Gender getGender(UUID uuid) {
        Gender gender = FamilyUtils.getFamily(uuid).getGender();
        if (gender == null) {
            gender = Gender.UNDECIDED;
        }
        return gender;
    }

    public static Gender getGender(PlayerFamily playerFamily) {
    	Gender gender = null;
        if (playerFamily != null) {
            gender = playerFamily.getGender();
        }
        return gender;
    }

    public static boolean setGender(Player player, Gender gender) {
    	setGender(player.getUniqueId(), gender);
        return true;
    }

    public static boolean setGender(UUID uuid, Gender gender) {
    	PlayerFamily playerFamily = FamilyUtils.getFamily(uuid);
    	playerFamily.setGender(gender);
    	FamilyUtils.saveFamily(playerFamily);
        return true;
    }

    public static boolean setGender(PlayerFamily playerFamily, Gender gender) {
    	playerFamily.setGender(gender);
    	FamilyUtils.saveFamily(playerFamily);
        return true;
    }
}
