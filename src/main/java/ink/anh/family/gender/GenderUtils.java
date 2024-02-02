package ink.anh.family.gender;

import java.util.UUID;

import org.bukkit.entity.Player;

import ink.anh.family.common.Family;
import ink.anh.family.util.FamilyUtils;

public class GenderUtils {

    public static Gender getGender(Player player) {
        return getGender(player.getUniqueId());
    }

    public static Gender getGender(UUID uuid) {
        Gender gender = FamilyUtils.getFamily(uuid).getGender();
        if (gender == null) {
            gender = Gender.UNDECIDED;
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
}
