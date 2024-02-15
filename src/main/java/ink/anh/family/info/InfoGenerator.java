package ink.anh.family.info;

import java.util.UUID;
import java.util.Set;

import org.bukkit.ChatColor;

import ink.anh.family.common.Family;
import ink.anh.family.common.FamilyDataHandler;
import ink.anh.family.gender.Gender;
import ink.anh.family.gender.GenderManager;

public class InfoGenerator {

    private static final int PARENT = 1;
    private static final int CHILD = 2;
    private static final int SPOUSE = 3;

    private static String generateFamilyMemberInfo(int relationType, Family family) {
    	UUID memberId = family.getRoot();
        Gender gender = GenderManager.getGender(memberId);
        String genderSymbol = Gender.getSymbol(gender);
        String lastName = "";
        String oldLastName = "";

        if (family.getLastName() != null && family.getLastName().length == 2) {
            lastName = (gender == Gender.MALE || gender == Gender.NON_BINARY) ? 
                       (family.getLastName()[0] != null ? family.getLastName()[0] : "") : 
                       (family.getLastName()[1] != null ? family.getLastName()[1] : "");
        }

        if (family.getOldLastName() != null && family.getOldLastName().length == 2) {
            String oldName = (gender == Gender.MALE || gender == Gender.NON_BINARY) ? 
                             (family.getOldLastName()[0] != null ? family.getOldLastName()[0] : "") : 
                             (family.getOldLastName()[1] != null ? family.getOldLastName()[1] : "");
            if (!oldName.isEmpty()) {
                oldLastName = ChatColor.GRAY + "\n family_info_previous_lastname" + oldName;
            }
        }

        String relation = getFamilyRole(gender, relationType);
        return ChatColor.GREEN + relation + ChatColor.YELLOW + family.getRootrNickName() + " " + lastName + oldLastName + " (" + genderSymbol + ")\n";
    }

    public static String generateFamilyInfo(UUID playerUUID) {
        FamilyDataHandler familyDataHandler = new FamilyDataHandler();
        Family family = familyDataHandler.getFamilyData(playerUUID);
    	return generateFamilyInfo(family);
    }

    public static String generateFamilyInfo(Family family) {
        FamilyDataHandler familyDataHandler = new FamilyDataHandler();
        if (family == null) {
            return ChatColor.RED + "family_info_family_not_found";
        }

        String line1 = ChatColor.GREEN + "=========================================\n";
        String line2 = ChatColor.GREEN + "-----------------------------------------\n";
        StringBuilder info = new StringBuilder();

        info.append(generateFamilyMemberInfo(SPOUSE, family));
        info.append(line1);

        // Співмешканець
        UUID spouse = family.getSpouse();
        String spouseInfo = spouse != null ? generateFamilyMemberInfo(SPOUSE, familyDataHandler.getFamilyData(spouse)) : "family_info_role_partner" + ChatColor.RED + "family_info_spouse_not_found \n";
        info.append(spouseInfo);

        // Батько
        UUID father = family.getFather();
        String fatherInfo = father != null ? generateFamilyMemberInfo(PARENT, familyDataHandler.getFamilyData(father)) : "family_info_role_father" + ChatColor.RED + "family_info_father_unknown \n";
        info.append(line2).append(fatherInfo);

        // Мати
        UUID mother = family.getMother();
        String motherInfo = mother != null ? generateFamilyMemberInfo(PARENT, familyDataHandler.getFamilyData(mother)) : "family_info_role_mother" + ChatColor.RED + "family_info_mother_unknown \n";
        info.append(line2).append(motherInfo);

        // Діти
        Set<UUID> children = family.getChildren();
        if (children.isEmpty()) {
            info.append(line2).append("family_info_children").append(ChatColor.RED).append("family_info_children_none \n");
        } else {
            info.append(line2).append("family_info_children\n");
            for (UUID childId : children) {
                Family childFamily = familyDataHandler.getFamilyData(childId);
                info.append(generateFamilyMemberInfo(CHILD, childFamily));
            }
        }

        return info.toString();
    }

    private static String getFamilyRole(Gender gender, int roleType) {
        switch (roleType) {
            case PARENT:
                return gender == Gender.MALE ? "family_info_role_father" : gender == Gender.FEMALE ? "family_info_role_mother" : "family_info_role_guardian";
            case CHILD:
                return gender == Gender.MALE ? "family_info_role_son" : gender == Gender.FEMALE ? "family_info_role_daughter" : "family_info_role_child";
            case SPOUSE:
                return gender == Gender.MALE ? "family_info_role_husband" : gender == Gender.FEMALE ? "family_info_role_wife" : "family_info_role_partner";
            default:
                return "family_info_role_relative";
        }
    }
}
