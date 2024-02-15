package ink.anh.family.gender;

import java.util.UUID;

import org.bukkit.entity.Player;

import ink.anh.family.common.Family;
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
    	Family family = FamilyUtils.getFamily(playerName);
    	Gender gender = null;
        if (family != null) {
            gender = family.getGender();
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

    public static Gender getGender(Family family) {
    	Gender gender = null;
        if (family != null) {
            gender = family.getGender();
        }
        return gender;
    }

    public static boolean setGender(Player player, Gender gender) {
    	setGender(player.getUniqueId(), gender);
        return true;
    }

    public static boolean setGender(UUID uuid, Gender gender) {
    	Family family = FamilyUtils.getFamily(uuid);
    	family.setGender(gender);
    	FamilyUtils.saveFamily(family);
        return true;
    }

    public static boolean setGender(Family family, Gender gender) {
    	family.setGender(gender);
    	FamilyUtils.saveFamily(family);
        return true;
    }
}
