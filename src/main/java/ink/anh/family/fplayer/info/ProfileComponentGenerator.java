package ink.anh.family.fplayer.info;

import java.util.UUID;
import java.util.Set;

import org.bukkit.Bukkit;
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

    private final int ROOT = 0;
    private final int PARENT = 1;
    private final int CHILD = 2;
    private final int SPOUSE = 3;
    
    private GlobalManager libraryManager;

    public ProfileComponentGenerator() {
        this.libraryManager = GlobalManager.getInstance();
    }

    public MessageComponents generateFamilyInfoComponent(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        return generateFamilyInfoComponent(FamilyUtils.getFamily(playerUUID), player);
    }

    public MessageComponents generateFamilyInfoComponent(Player player) {
        return generateFamilyInfoComponent(FamilyUtils.getFamily(player.getUniqueId()), player);
    }

    public MessageComponents generateFamilyInfoComponent(PlayerFamily playerFamily, Player player) {
        if (playerFamily == null) {
            return MessageComponents.builder()
                    .content(translate("family_info_family_not_found", player))
                    .color("RED")
                    .build();
        }

        MessageBuilder builder = MessageComponents.builder()
                .content("=========================================").color("GREEN").appendNewLine();
        
        // Root
        builder.append(generateFamilyMemberInfoComponent(ROOT, playerFamily, player)).appendNewLine();
        builder.content("-----------------------------------------").color("GREEN").appendNewLine();

        // Spouse
        UUID spouse = playerFamily.getSpouse();
        if (spouse != null) {
            builder.append(generateFamilyMemberInfoComponent(SPOUSE, FamilyUtils.getFamily(spouse), player)).appendNewLine();
        } else {
            builder.content(translate("family_info_role_partner", player))
                   .hexColor("#12ccad")
                   .append(MessageComponents.builder()
                           .content(" ")
                           .build())
                   .append(MessageComponents.builder()
                           .content(translate("family_info_spouse_not_found", player))
                           .color("RED")
                           .build())
                   .appendNewLine();
        }

        // Father
        UUID father = playerFamily.getFather();
        if (father != null) {
            builder.append(generateFamilyMemberInfoComponent(PARENT, FamilyUtils.getFamily(father), player)).appendNewLine();
        } else {
            builder.content(translate("family_info_role_father", player))
            		.hexColor("#12ccad")
            		.append(MessageComponents.builder()
            				.content(" ")
            				.build())
                   .append(MessageComponents.builder()
                           .content(translate("family_info_father_unknown", player))
                           .color("RED")
                           .build())
                   .appendNewLine();
        }

        // Mother
        UUID mother = playerFamily.getMother();
        if (mother != null) {
            builder.append(generateFamilyMemberInfoComponent(PARENT, FamilyUtils.getFamily(mother), player)).appendNewLine();
        } else {
            builder.content(translate("family_info_role_mother", player))
            		.hexColor("#12ccad")
            		.append(MessageComponents.builder()
            				.content(" ")
            				.build())
                   .append(MessageComponents.builder()
                           .content(translate("family_info_mother_unknown", player))
                           .color("RED")
                           .build())
                   .appendNewLine();
        }

        // Children
        Set<UUID> children = playerFamily.getChildren();
        if (children.isEmpty()) {
            builder.content(translate("family_info_children", player))
            		.hexColor("#12ccad")
            		.append(MessageComponents.builder()
            			.content(" ")
            			.build())
                   .append(MessageComponents.builder()
                           .content(translate("family_info_children_none", player))
                           .color("RED")
                           .build())
                   .appendNewLine();
        } else {
            builder.content(translate("family_info_children", player)).color("WHITE").appendNewLine();
            for (UUID childId : children) {
                builder.append(generateFamilyMemberInfoComponent(CHILD, FamilyUtils.getFamily(childId), player)).appendNewLine();
            }
        }

        return builder.build();
    }

    private MessageComponents generateFamilyMemberInfoComponent(int relationType, PlayerFamily playerFamily, Player player) {
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
        String relation = translate(getFamilyRole(gender, relationType), player);
        
        String hoverInfo = StringUtils.formatString(Translator.translateKyeWorld(libraryManager,"family_print_info", getLangs(player)), nickName);

        return MessageComponents.builder()
                .content(relation)
                .color("GREEN")
                .append(MessageComponents.builder()
                        .content(" (")
                        .color("WHITE")
                        .build())
                .append(MessageComponents.builder()
                        .content(Gender.getSymbol(gender))
                        .hexColor(Gender.getColor(gender))
                        .build())
                .append(MessageComponents.builder()
                        .content(") ")
                        .color("WHITE")
                        .build())
                .append(MessageComponents.builder()
                        .content(fullName)
                        .color("YELLOW")
                        .hoverComponent(MessageComponents.builder().content(hoverInfo).hexColor("#12ccad").build())
                        .clickActionRunCommand("/family profile " + nickName)
                        .build())
                .build();
    }

    private String getFamilyRole(Gender gender, int roleType) {
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
