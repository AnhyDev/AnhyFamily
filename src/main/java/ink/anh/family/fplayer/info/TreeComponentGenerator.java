package ink.anh.family.fplayer.info;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.gender.GenderManager;
import ink.anh.family.util.FamilyUtils;
import ink.anh.family.util.StringColorUtils;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.lingo.Translator;

public class TreeComponentGenerator {
    private FamilyRepeated root;
    private Map<UUID, FamilyRepeated> rootParents;
    private Map<UUID, FamilyRepeated> rootOffspring;

    public TreeComponentGenerator(UUID rootUuid) {
        this.root = new FamilyRepeated(FamilyUtils.getFamily(rootUuid));
        this.rootParents = new HashMap<>();
        this.rootOffspring = new HashMap<>();
        this.rootOffspring = buildDescendantsTree(this.root);
        resetRoot();
        buildAncestorsTree(this.root);
    }

    public TreeComponentGenerator(PlayerFamily rootFamily) {
        this.root = new FamilyRepeated(rootFamily);
        this.rootParents = new HashMap<>();
        this.rootOffspring = new HashMap<>();
        this.rootOffspring = buildDescendantsTree(this.root);
        resetRoot();
        buildAncestorsTree(this.root);
    }

    private void resetRoot() {
        this.root.setRepeated(0);
        this.root.setProcessed(false);
    }

    private void buildAncestorsTree(FamilyRepeated memberFam) {
        if (memberFam.isProcessed()) {
            return;
        }

        memberFam.setProcessed(true);

        PlayerFamily member = memberFam.getFamily();
        UUID fatherUuid = member.getFather();
        UUID motherUuid = member.getMother();

        if (fatherUuid != null) {
            FamilyRepeated father = rootParents.getOrDefault(fatherUuid, new FamilyRepeated(FamilyUtils.getFamily(fatherUuid)));
            rootParents.put(fatherUuid, father);
            buildAncestorsTree(father);
        }

        if (motherUuid != null) {
            FamilyRepeated mother = rootParents.getOrDefault(motherUuid, new FamilyRepeated(FamilyUtils.getFamily(motherUuid)));
            rootParents.put(motherUuid, mother);
            buildAncestorsTree(mother);
        }
    }

    public MessageComponents buildFamilyTreeComponent(Player player) {
        MessageBuilder treeBuilder = MessageComponents.builder().appendNewLine();
        
        MessageBuilder rootBuilder = MessageComponents.builder()
                .content(translate(" family_tree_title ", new String[] {}, player))
                .hexColor(StringColorUtils.TREE_TITLE_COLOR)  // Використовуємо TREE_TITLE_COLOR
                .decoration("BOLD", true)
                .append(getFormattedName(root.getFamily(), StringColorUtils.TREE_MEMBER_NAME_COLOR, 0, player))
                .appendNewLine();

        // Спочатку додаємо нащадків
        buildDescendantsTreeComponent(root, 0, " ", treeBuilder, player);

        // Потім додаємо центральний елемент
        treeBuilder.append(rootBuilder.build());

        resetRoot();
        // Потім додаємо предків
        buildAncestorsTreeComponent(root, 0, " ", treeBuilder, player);

        return treeBuilder.build();
    }

    private void buildAncestorsTreeComponent(FamilyRepeated memberFam, int level, String prefix, MessageBuilder treeBuilder, Player player) {
        PlayerFamily member = memberFam.getFamily();
        boolean isRepeated = memberFam.getRepeated() > 0;
        String title = (level == 0 ? translate("family_tree_ancestors", new String[] {}, player) + " " : "");

        String branchSymbol = "└─ ";
        MessageComponents memberLine = buildMemberLine(member, level, prefix, isRepeated, title, branchSymbol, player);
        treeBuilder.append(memberLine).appendNewLine();

        if (!isRepeated) {
            UUID fatherUuid = member.getFather();
            UUID motherUuid = member.getMother();

            if (fatherUuid != null) {
                FamilyRepeated father = rootParents.get(fatherUuid);
                if (father != null) {
                	buildAncestorsTreeComponent(father, level + 1, prefix + "  ", treeBuilder, player);
                }
            }

            if (motherUuid != null) {
                FamilyRepeated mother = rootParents.get(motherUuid);
                if (mother != null) {
                	buildAncestorsTreeComponent(mother, level + 1, prefix + "  ", treeBuilder, player);
                }
            }
        }
        memberFam.increaseRepeated();
    }

    private Map<UUID, FamilyRepeated> buildDescendantsTree(FamilyRepeated memberFam) {
        Map<UUID, FamilyRepeated> children = new HashMap<>();

        if (memberFam.isProcessed()) {
            return children;
        }

        memberFam.setProcessed(true);

        PlayerFamily member = memberFam.getFamily();
        Set<UUID> childrenUuids = member.getChildren();

        if (childrenUuids != null) {
            for (UUID childUuid : childrenUuids) {
                FamilyRepeated memberFamChild = rootOffspring.getOrDefault(childUuid, new FamilyRepeated(FamilyUtils.getFamily(childUuid)));
                children.put(childUuid, memberFamChild);
                children.putAll(buildDescendantsTree(memberFamChild));
            }
        }

        return children;
    }

    private void buildDescendantsTreeComponent(FamilyRepeated memberFam, int level, String prefix, MessageBuilder treeBuilder, Player player) {
        PlayerFamily member = memberFam.getFamily();
        boolean isRepeated = memberFam.getRepeated() > 0;

        // Рекурсивно додаємо всіх дітей спочатку
        Set<UUID> childrenUuids = member.getChildren();
        if (childrenUuids != null) {
            for (UUID childUuid : childrenUuids) {
                PlayerFamily child = rootOffspring.get(childUuid).getFamily();
                if (child != null && !isRepeated) {
                    buildDescendantsTreeComponent(new FamilyRepeated(child), level + 1, prefix + " ", treeBuilder, player);
                }
            }
        }

        // Додаємо інформацію про нащадків після обробки дітей
        String translateTitle = translate("family_tree_descendants", new String[] {}, player);
        String title = (level == 0 ? translateTitle + " " : "");
        String branchSymbol = level == 0 ? "┌─ " : prefix + "┌─ ";
        MessageComponents memberLine = buildMemberLine(member, level, prefix, isRepeated, title, branchSymbol, player);
        treeBuilder.append(memberLine).appendNewLine();

        memberFam.increaseRepeated();
    }

    private MessageComponents buildMemberLine(PlayerFamily member, int level, String prefix, boolean isRepeated, String title, String branchSymbol, Player player) {
        String hexColor = determineHexColor(level, isRepeated ? 1 : 0);
        
        MessageBuilder lineBuilder = MessageComponents.builder()
                .content(prefix)
                .append(MessageComponents.builder()
                        .content(branchSymbol)
                        .hexColor(hexColor)
                        .build())
                .append(MessageComponents.builder()
                        .content(title)
                        .hexColor(hexColor)
                        .build());

        if (level > 0) {
            lineBuilder.append(getFormattedName(member, hexColor, level, player)).build();
        }

        if (isRepeated) {
            lineBuilder.append(MessageComponents.builder()
                    .content("*")
                    .hexColor(StringColorUtils.TREE_REPEATED_COLOR)  // Використовуємо TREE_REPEATED_COLOR
                    .build());
        }

        return lineBuilder.build();
    }

    private MessageComponents getFormattedName(PlayerFamily member, String hexColor, int level, Player player) {
        Gender gender = GenderManager.getGender(member.getRoot());
        
        StringBuilder formattedName = new StringBuilder();
        
        if (member.getFirstName() != null && !member.getFirstName().isEmpty()) {
            formattedName.append(member.getFirstName()).append(" ");
        }
        
        String actualLastName = member.getActualLastName();
        if (actualLastName != null && !actualLastName.isEmpty()) {
            formattedName.append(actualLastName).append(" ");
        }
        
        String nickName = member.getRootrNickName();
        formattedName.append("(").append(nickName).append(")");
        String command = level == 0 ? "/family profile " + nickName : "/family tree " + nickName;
        String hoverInfo = level == 0 ? translate("family_print_info", new String[] {nickName}, player) :  translate("family_tree_title", new String[] {nickName}, player);
        
        MessageComponents playerComponent = MessageComponents.builder()
                .content("(")
                .hexColor(hexColor)
                .append(MessageComponents.builder()
                        .content(Gender.getSymbol(gender))
                        .hexColor(Gender.getColor(gender))
                        .build())
                .append(MessageComponents.builder()
                        .content(") ")
                        .hexColor(hexColor)
                        .build())
                .append(MessageComponents.builder()
                        .content(formattedName.toString())
                        .hexColor(hexColor)
                        .hoverComponent(MessageComponents.builder().content(hoverInfo).hexColor("#12ccad").build())
                        .clickActionRunCommand(command)
                        .build())
                .build();
        
        return playerComponent;
    }

    private String determineHexColor(int level, int repeatedCount) {
        if (repeatedCount > 0) {
            return StringColorUtils.TREE_REPEATED_COLOR;  // Темно-червоний
        }
        switch (level) {
            case 0: return StringColorUtils.TREE_LEVEL0_COLOR;  // Зелений
            case 1: return StringColorUtils.TREE_LEVEL1_COLOR;  // Золотий
            case 2: return StringColorUtils.TREE_LEVEL2_COLOR;  // Блакитний
            case 3: return StringColorUtils.TREE_LEVEL3_COLOR;  // Бірюзовий
            case 4: return StringColorUtils.TREE_LEVEL4_COLOR;  // Яскраво-рожевий
            case 5: return StringColorUtils.TREE_LEVEL5_COLOR;  // Помаранчево-червоний
            case 6: return StringColorUtils.TREE_LEVEL6_COLOR;  // Зелено-жовтий
            default: return StringColorUtils.TREE_DEFAULT_COLOR;  // Сірий
        }
    }

    private String translate(String messageKey, String[] replace, Player player) {
        String[] langs = LangUtils.getPlayerLanguage(player);
        String message = Translator.translateKyeWorld(GlobalManager.getInstance(), messageKey, langs);
        message = StringUtils.formatString(message, replace);
        return StringUtils.colorize(message);
    }

    public PlayerFamily getMember(UUID uuid) {
        return rootParents.get(uuid).getFamily();
    }

    public boolean addMember(PlayerFamily member) {
        if (!rootParents.containsKey(member.getRoot())) {
            rootParents.put(member.getRoot(), new FamilyRepeated(member));
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID uuid) {
        if (rootParents.containsKey(uuid)) {
            rootParents.remove(uuid);
            return true;
        }
        return false;
    }

    public boolean hasAncestorWithUUID(UUID uuid) {
        return rootParents.containsKey(uuid);
    }

    public boolean hasOffspringWithUUID(UUID uuid) {
        return rootOffspring.containsKey(uuid);
    }

    public boolean hasRelativesWithUUID(UUID uuid) {
        return rootParents.containsKey(uuid) || rootOffspring.containsKey(uuid);
    }

    class FamilyRepeated {
        
        private PlayerFamily playerFamily;
        private int repeated;
        private boolean processed;
        
        public FamilyRepeated(PlayerFamily playerFamily) {
            this.playerFamily = playerFamily;
            this.repeated = 0;
        }

        public PlayerFamily getFamily() {
            return playerFamily;
        }

        public int getRepeated() {
            return repeated;
        }

        public void setRepeated(int repeated) {
            this.repeated = repeated;
        }

        public void increaseRepeated() {
            this.repeated++;
        }

        public boolean isProcessed() {
            return processed;
        }

        public void setProcessed(boolean processed) {
            this.processed = processed;
        }
    }
}
