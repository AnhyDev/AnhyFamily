package ink.anh.family.fplayer.info;

import java.util.UUID;
import java.util.Set;

import org.bukkit.entity.Player;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.gender.GenderManager;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.lingo.Translator;

public class ProfileComponentGenerator {

    public static final String GREEN_COLOR = "#00FF00";
    public static final String WHITE_COLOR = "#FFFFFF";
    public static final String YELLOW_COLOR = "#FFFF00";
    public static final String RED_COLOR = "#FF0000";
    public static final String ACCENT_COLOR = "#12ccad";

    private enum FamilyRole {
        ROOT, PARENT, CHILD, SPOUSE
    }

    private GlobalManager libraryManager;

    public ProfileComponentGenerator() {
        this.libraryManager = GlobalManager.getInstance();
    }

    public MessageComponents generateFamilyInfoComponent(PlayerFamily playerFamily, Player player) {
        String familyNotFound = translate("family_info_family_not_found", player);
        String rolePartner = translate("family_info_role_partner", player);
        String spouseNotFound = translate("family_info_spouse_not_found", player);
        String roleFather = translate("family_info_role_father", player);
        String fatherUnknown = translate("family_info_father_unknown", player);
        String roleMother = translate("family_info_role_mother", player);
        String motherUnknown = translate("family_info_mother_unknown", player);
        String roleChildren = translate("family_info_children", player);
        String childrenNone = translate("family_info_children_none", player);

        if (playerFamily == null) {
            return MessageComponents.builder()
                    .content(familyNotFound)
                    .hexColor(RED_COLOR)
                    .build();
        }

        MessageBuilder builder = MessageComponents.builder()
                .content("=========================================").hexColor(GREEN_COLOR).appendNewLine();

        // Root
        builder.append(generateFamilyMemberInfoComponent(FamilyRole.ROOT, playerFamily, player, true)).appendNewLine();
        builder.content("-----------------------------------------").hexColor(GREEN_COLOR).appendNewLine();

        // Spouse
        UUID spouse = playerFamily.getSpouse();
        if (spouse != null) {
            builder.append(generateFamilyMemberInfoComponent(FamilyRole.SPOUSE, FamilyUtils.getFamily(spouse), player, false)).appendNewLine();
        } else {
            builder.content(" " + rolePartner)
                   .hexColor(ACCENT_COLOR)
                   .append(MessageComponents.builder()
                           .content(" ")
                           .build())
                   .append(MessageComponents.builder()
                           .content(spouseNotFound)
                           .hexColor(RED_COLOR)
                           .build())
                   .appendNewLine();
        }

        // Father
        UUID father = playerFamily.getFather();
        if (father != null) {
            builder.append(generateFamilyMemberInfoComponent(FamilyRole.PARENT, FamilyUtils.getFamily(father), player, false)).appendNewLine();
        } else {
            builder.content(" " + roleFather)
                   .hexColor(ACCENT_COLOR)
                   .append(MessageComponents.builder()
                           .content(" ")
                           .build())
                   .append(MessageComponents.builder()
                           .content(fatherUnknown)
                           .hexColor(RED_COLOR)
                           .build())
                   .appendNewLine();
        }

        // Mother
        UUID mother = playerFamily.getMother();
        if (mother != null) {
            builder.append(generateFamilyMemberInfoComponent(FamilyRole.PARENT, FamilyUtils.getFamily(mother), player, false)).appendNewLine();
        } else {
            builder.content(" " + roleMother)
                   .hexColor(ACCENT_COLOR)
                   .append(MessageComponents.builder()
                           .content(" ")
                           .build())
                   .append(MessageComponents.builder()
                           .content(motherUnknown)
                           .hexColor(RED_COLOR)
                           .build())
                   .appendNewLine();
        }

        // Children
        Set<UUID> children = playerFamily.getChildren();
        if (children.isEmpty()) {
            builder.content(" " + roleChildren)
                   .hexColor(ACCENT_COLOR)
                   .append(MessageComponents.builder()
                       .content(" ")
                       .build())
                   .append(MessageComponents.builder()
                           .content(childrenNone)
                           .hexColor(RED_COLOR)
                           .build())
                   .appendNewLine();
        } else {
            builder.content(" " + roleChildren).hexColor(GREEN_COLOR).appendNewLine();
            for (UUID childId : children) {
                builder.append(generateFamilyMemberInfoComponent(FamilyRole.CHILD, FamilyUtils.getFamily(childId), player, false, true)).appendNewLine();
            }
        }

        builder.content("=========================================").hexColor(GREEN_COLOR).appendNewLine();

        return builder.build();
    }

    private MessageComponents generateFamilyMemberInfoComponent(FamilyRole relationType, PlayerFamily playerFamily, Player player, boolean isRoot) {
        return generateFamilyMemberInfoComponent(relationType, playerFamily, player, isRoot, false);
    }

    private MessageComponents generateFamilyMemberInfoComponent(FamilyRole relationType, PlayerFamily playerFamily, Player player, boolean isRoot, boolean isChild) {
        Gender gender = GenderManager.getGender(playerFamily.getRoot());

        String firstName = playerFamily.getFirstName();
        String lastName = playerFamily.getCurrentSurname();
        String nickName = playerFamily.getRootrNickName();

        StringBuilder fullNameBuilder = new StringBuilder();
        if ((firstName != null && !firstName.isEmpty()) || (lastName != null && !lastName.isEmpty())) {
            if (firstName != null && !firstName.isEmpty()) {
                fullNameBuilder.append(firstName);
            }
            if (lastName != null && !lastName.isEmpty()) {
                if (fullNameBuilder.length() > 0) {
                    fullNameBuilder.append(" ");
                }
                fullNameBuilder.append(lastName);
            }
            fullNameBuilder.append(" (").append(nickName).append(")");
        } else {
            fullNameBuilder.append(nickName);
        }

        String fullName = fullNameBuilder.toString();
        String relation = " " + translate(getFamilyRole(gender, relationType), player);

        if (isRoot) {
            fullName = "✔ " + fullName;
        }

        String[] langs = getLangs(player);
        String hoverInfo = "family_print_info";
        hoverInfo = StringUtils.formatString(Translator.translateKyeWorld(libraryManager, hoverInfo, langs), nickName);
        if (hoverInfo.endsWith(":")) {
            hoverInfo = hoverInfo.substring(0, hoverInfo.length() - 1);
        }

        String command = "/family info " + nickName;

        String prefix = isChild ? "  " : "";

        return MessageComponents.builder()
                .content(prefix + relation)
                .hexColor(GREEN_COLOR)
                .append(MessageComponents.builder()
                        .content(" (")
                        .hexColor(WHITE_COLOR)
                        .build())
                .append(MessageComponents.builder()
                        .content(Gender.getSymbol(gender))
                        .hexColor(Gender.getColor(gender))
                        .build())
                .append(MessageComponents.builder()
                        .content(") ")
                        .hexColor(WHITE_COLOR)
                        .build())
                .append(MessageComponents.builder()
                        .content(fullName)
                        .hexColor(YELLOW_COLOR)
                        .hoverComponent(MessageComponents.builder().content(hoverInfo).hexColor(ACCENT_COLOR).build())
                        .clickActionRunCommand(command)
                        .build())
                .build();
    }

    private String getFamilyRole(Gender gender, FamilyRole roleType) {
        switch (roleType) {
            case ROOT: return "";
            case PARENT: return gender == Gender.MALE ? "family_info_role_father" : gender == Gender.FEMALE ? "family_info_role_mother" : "family_info_role_guardian";
            case CHILD: return gender == Gender.MALE ? "family_info_role_son" : gender == Gender.FEMALE ? "family_info_role_daughter" : "family_info_role_child";
            case SPOUSE: return gender == Gender.MALE ? "family_info_role_husband" : gender == Gender.FEMALE ? "family_info_role_wife" : "family_info_role_partner";
            default: return "family_info_role_relative";
        }
    }

    private String[] getLangs(Player player) {
        return player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};
    }

    private String translate(String key, Player player) {
        String[] langs = getLangs(player);
        return StringUtils.colorize(Translator.translateKyeWorld(libraryManager, key, langs));
    }
}
