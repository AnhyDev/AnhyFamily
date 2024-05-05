package ink.anh.family.info;

import java.util.UUID;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import ink.anh.api.lingo.Translator;
import ink.anh.api.utils.LangUtils;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.common.Family;
import ink.anh.family.gender.Gender;
import ink.anh.family.gender.GenderManager;
import ink.anh.family.util.FamilyUtils;

public class InfoGenerator {

    private final int ROOT = 0;
    private final int PARENT = 1;
    private final int CHILD = 2;
    private final int SPOUSE = 3;
    
    private GlobalManager libraryManager;

    public InfoGenerator(AnhyFamily familyPlugin) {
        this.libraryManager = familyPlugin.getGlobalManager();
    }

    public String generateFamilyInfo(UUID playerUUID) {
        return Translator.translateKyeWorld(libraryManager, generateFamilyInfo(FamilyUtils.getFamily(playerUUID)), getLangs(null));
    }

    public String generateFamilyInfo(Player player) {
        return Translator.translateKyeWorld(libraryManager, generateFamilyInfo(FamilyUtils.getFamily(player.getUniqueId())), getLangs(player));
    }

    public String generateFamilyInfo(Family family) {
        if (family == null) {
            return ChatColor.RED + " family_info_family_not_found";
        }
        StringBuilder info = new StringBuilder();
        info.append(ChatColor.GREEN).append("=========================================\n");

        // Root
        info.append(generateFamilyMemberInfo(ROOT, family)).append(ChatColor.GREEN).append("-----------------------------------------\n");

        // Spouse
        UUID spouse = family.getSpouse();
        String spouseInfo = spouse != null ? generateFamilyMemberInfo(SPOUSE, FamilyUtils.getFamily(spouse)) : " family_info_role_partner" + ChatColor.RED + " family_info_spouse_not_found \n";
        info.append(spouseInfo);

        // Father
        UUID father = family.getFather();
        String fatherInfo = father != null ? generateFamilyMemberInfo(PARENT, FamilyUtils.getFamily(father)) : " family_info_role_father" + ChatColor.RED + " family_info_father_unknown \n";
        info.append(fatherInfo);

        // Mother
        UUID mother = family.getMother();
        String motherInfo = mother != null ? generateFamilyMemberInfo(PARENT, FamilyUtils.getFamily(mother)) : " family_info_role_mother" + ChatColor.RED + " family_info_mother_unknown \n";
        info.append(motherInfo);

        // Children
        Set<UUID> children = family.getChildren();
        if (children.isEmpty()) {
            info.append(" family_info_children").append(ChatColor.RED).append(" family_info_children_none \n");
        } else {
            info.append(" family_info_children \n");
            for (UUID childId : children) {
                info.append(generateFamilyMemberInfo(CHILD, FamilyUtils.getFamily(childId)));
            }
        }
        return info.toString();
    }

    private String generateFamilyMemberInfo(int relationType, Family family) {
        Gender gender = GenderManager.getGender(family.getRoot());
        String genderSymbol = Gender.getMinecraftColor(gender) + Gender.getSymbol(gender);

        String lastName = family.getCurrentSurname();
        lastName = (lastName != null && !lastName.isEmpty()) ? lastName : "";

        String oldLastName = FamilyUtils.selectSurname(family.getOldLastName(), gender);
        oldLastName = (oldLastName != null && !oldLastName.isEmpty()) ? "\n family_info_previous_lastname " + oldLastName : "";

        String relation = getFamilyRole(gender, relationType);
        return ChatColor.GREEN + relation + " §r(" + genderSymbol + "§r) " + ChatColor.YELLOW + family.getRootrNickName() + " " + lastName + oldLastName + "\n";
    }

    private String getFamilyRole(Gender gender, int roleType) {
        switch (roleType) {
            case ROOT: return "";
            case PARENT: return gender == Gender.MALE ? " family_info_role_father" : gender == Gender.FEMALE ? " family_info_role_mother" : " family_info_role_guardian";
            case CHILD: return gender == Gender.MALE ? " family_info_role_son" : gender == Gender.FEMALE ? " family_info_role_daughter" : " family_info_role_child";
            case SPOUSE: return gender == Gender.MALE ? " family_info_role_husband" : gender == Gender.FEMALE ? " family_info_role_wife" : " family_info_role_partner";
            default: return " family_info_role_relative";
        }
    }

    private String[] getLangs(Player player) {
        return player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};
    }

    public String getTranslate(Player player, Family family) {
    	return Translator.translateKyeWorld(libraryManager, generateFamilyInfo(family), getLangs(player));
    }
}
