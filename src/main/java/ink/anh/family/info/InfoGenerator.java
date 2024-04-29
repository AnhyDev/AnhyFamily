package ink.anh.family.info;

import java.util.UUID;
import java.util.Set;

import org.bukkit.ChatColor;

import ink.anh.family.AnhyFamily;
import ink.anh.family.common.Family;
import ink.anh.family.gender.Gender;
import ink.anh.family.gender.GenderManager;
import ink.anh.family.util.FamilyUtils;

public class InfoGenerator {

    private static final int ROOT = 0;
    private static final int PARENT = 1;
    private static final int CHILD = 2;
    private static final int SPOUSE = 3;

    private static String generateFamilyMemberInfo(int relationType, Family family) {
        Gender gender = GenderManager.getGender(family.getRoot());
        String genderSymbol = Gender.getMinecraftColor(gender) + Gender.getSymbol(gender);
        
        String lastName = family.getCurrentSurname();
        lastName = (lastName != null && !lastName.isEmpty()) ? lastName : "";
        
        String oldLastName = FamilyUtils.selectSurname(family.getOldLastName(), gender);
        oldLastName = (oldLastName != null && !oldLastName.isEmpty()) ? "\n family_info_previous_lastname " + oldLastName : "";

        String relation = getFamilyRole(gender, relationType);
        return ChatColor.GREEN + relation + " §r(" + genderSymbol + "§r) " + ChatColor.YELLOW + family.getRootrNickName() + " " + lastName + oldLastName + "\n";
    }

    public static String generateFamilyInfo(UUID playerUUID) {
        Family family = FamilyUtils.getFamily(playerUUID);
        
    	return generateFamilyInfo(family);
    }

    public static String generateFamilyInfo(Family family) {
        
        String infoRaw = null;
        try {
            if (family == null) {
                return ChatColor.RED + " family_info_family_not_found";
            }

            String line1 = ChatColor.GREEN + "=========================================\n";
            String line2 = ChatColor.GREEN + "-----------------------------------------\n";
            StringBuilder info = new StringBuilder();

            info.append(generateFamilyMemberInfo(ROOT, family));
            info.append(line1);

            // Співмешканець
            UUID spouse = family.getSpouse();
            String spouseInfo = spouse != null ? generateFamilyMemberInfo(SPOUSE, FamilyUtils.getFamily(spouse)) : " family_info_role_partner" + ChatColor.RED + " family_info_spouse_not_found \n";
            info.append(spouseInfo);

            // Батько
            UUID father = family.getFather();
            String fatherInfo = father != null ? generateFamilyMemberInfo(PARENT, FamilyUtils.getFamily(father)) : " family_info_role_father" + ChatColor.RED + " family_info_father_unknown \n";
            info.append(line2).append(fatherInfo);

            // Мати
            UUID mother = family.getMother();
            String motherInfo = mother != null ? generateFamilyMemberInfo(PARENT, FamilyUtils.getFamily(mother)) : " family_info_role_mother" + ChatColor.RED + " family_info_mother_unknown \n";
            info.append(line2).append(motherInfo);

            // Діти
            Set<UUID> children = family.getChildren();
            if (children.isEmpty()) {
                info.append(line2).append(" family_info_children").append(ChatColor.RED).append(" family_info_children_none \n");
            } else {
                info.append(line2).append(" family_info_children \n");
                for (UUID childId : children) {
                    Family childFamily = FamilyUtils.getFamily(childId);
                    info.append(generateFamilyMemberInfo(CHILD, childFamily));
                }
            }
            infoRaw = info.toString();
        } catch (Exception e) {
        	AnhyFamily.getInstance().getLogger().severe("Помилка при генерації інформації про сім'ю: " + e.getMessage());
            e.printStackTrace(); // Додатково виводить stack trace в консоль сервера
        }


        return infoRaw;
    }

    private static String getFamilyRole(Gender gender, int roleType) {
        switch (roleType) {
        	case ROOT:
        		return "";
            case PARENT:
                return gender == Gender.MALE ? " family_info_role_father" : gender == Gender.FEMALE ? " family_info_role_mother" : " family_info_role_guardian";
            case CHILD:
                return gender == Gender.MALE ? " family_info_role_son" : gender == Gender.FEMALE ? " family_info_role_daughter" : " family_info_role_child";
            case SPOUSE:
                return gender == Gender.MALE ? " family_info_role_husband" : gender == Gender.FEMALE ? " family_info_role_wife" : " family_info_role_partner";
            default:
                return " family_info_role_relative";
        }
    }
}
