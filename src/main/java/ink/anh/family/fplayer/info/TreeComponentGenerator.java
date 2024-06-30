package ink.anh.family.fplayer.info;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import ink.anh.family.GlobalManager;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;
import ink.anh.api.utils.LangUtils;
import ink.anh.api.utils.StringUtils;
import ink.anh.api.messages.MessageComponents;
import ink.anh.api.messages.MessageComponents.MessageBuilder;
import ink.anh.api.lingo.Translator;

public class TreeComponentGenerator {
    private FamilyRepeated root;
    private Map<UUID, FamilyRepeated> rootParents;
    private Map<UUID, FamilyRepeated> rootOffspring;
    private GlobalManager libraryManager;

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
        MessageBuilder treeBuilder = MessageComponents.builder()
                .content(translate("family_tree_title", player))
                .hexColor("#FFD700")  // Золотий колір
                .decoration("BOLD", true)
                .appendNewLine()
                .append(MessageComponents.builder()
                        .content(getFormattedName(root.getFamily()))
                        .build())
                .appendNewLine();

        buildDescendantsTreeComponent(root, 0, "", treeBuilder, player);
        resetRoot();
        buildFamilyTreeComponent(root, 0, "", treeBuilder, player);
        return treeBuilder.build();
    }

    private void buildFamilyTreeComponent(FamilyRepeated memberFam, int level, String prefix, MessageBuilder treeBuilder, Player player) {
        PlayerFamily member = memberFam.getFamily();
        boolean isRepeated = memberFam.getRepeated() > 0;
        String title = (level == 0 ? translate("family_tree_ancestors", player) + " " : "");

        String branchSymbol = "└─ ";
        MessageComponents memberLine = buildMemberLine(member, level, prefix, isRepeated, title, branchSymbol, player);
        treeBuilder.append(memberLine).appendNewLine();

        if (!isRepeated) {
            UUID fatherUuid = member.getFather();
            UUID motherUuid = member.getMother();

            if (fatherUuid != null) {
                FamilyRepeated father = rootParents.get(fatherUuid);
                if (father != null) {
                    buildFamilyTreeComponent(father, level + 1, prefix + "  ", treeBuilder, player);
                }
            }

            if (motherUuid != null) {
                FamilyRepeated mother = rootParents.get(motherUuid);
                if (mother != null) {
                    buildFamilyTreeComponent(mother, level + 1, prefix + "  ", treeBuilder, player);
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

        String title = (level == 0 ? translate("family_tree_descendants", player) + " " : "");

        String branchSymbol = "┌─ ";
        MessageComponents memberLine = buildMemberLine(member, level, prefix, isRepeated, title, branchSymbol, player);
        treeBuilder.append(memberLine).appendNewLine();

        Set<UUID> childrenUuids = member.getChildren();
        if (childrenUuids != null) {
            for (UUID childUuid : childrenUuids) {
                PlayerFamily child = rootOffspring.get(childUuid).getFamily();
                if (child != null && !isRepeated) {
                    buildDescendantsTreeComponent(new FamilyRepeated(child), level + 1, prefix + "  ", treeBuilder, player);
                }
            }
        }
        memberFam.increaseRepeated();
    }

    private MessageComponents buildMemberLine(PlayerFamily member, int level, String prefix, boolean isRepeated, String title, String branchSymbol, Player player) {
        MessageBuilder lineBuilder = MessageComponents.builder()
                .content(prefix)
                .append(MessageComponents.builder()
                        .content(branchSymbol)
                        .hexColor(determineHexColor(level, isRepeated ? 1 : 0))
                        .build())
                .append(MessageComponents.builder()
                        .content(title)
                        .build());

        if (level > 0) {
        	String nickName = member.getRootrNickName();
        	
            String hoverInfo = StringUtils.formatString(Translator.translateKyeWorld(libraryManager,"family_print_info", getLangs(player)), nickName);
        	
            lineBuilder.append(MessageComponents.builder()
                    .content(getFormattedName(member))
                    .hexColor(determineHexColor(level, isRepeated ? 1 : 0))
                    .hoverComponent(MessageComponents.builder().content(hoverInfo).hexColor("#12ccad").build())
                    .clickActionRunCommand("/family tree " + nickName)
                    .build());
        }

        if (isRepeated) {
            lineBuilder.append(MessageComponents.builder()
                    .content("*")
                    .hexColor("#8B0000")  // Темно-червоний колір
                    .build());
        }

        return lineBuilder.build();
    }

    private String getFormattedName(PlayerFamily member) {
        StringBuilder formattedName = new StringBuilder();
        if (member.getFirstName() != null && !member.getFirstName().isEmpty()) {
            formattedName.append(member.getFirstName()).append(" ");
        }
        String actualLastName = member.getActualLastName();
        if (actualLastName != null && !actualLastName.isEmpty()) {
            formattedName.append(actualLastName).append(" ");
        }
        formattedName.append("(").append(member.getRootrNickName()).append(")");
        return formattedName.toString();
    }

    private String determineHexColor(int level, int repeatedCount) {
        if (repeatedCount > 0) {
            return "#8B0000";  // Темно-червоний
        }
        switch (level) {
            case 0: return "#00FF00";  // Зелений
            case 1: return "#FFD700";  // Золотий
            case 2: return "#1E90FF";  // Блакитний
            case 3: return "#00FFFF";  // Бірюзовий
            case 4: return "#FF69B4";  // Яскраво-рожевий
            case 5: return "#FF4500";  // Помаранчево-червоний
            case 6: return "#ADFF2F";  // Зелено-жовтий
            default: return "#808080";  // Сірий
        }
    }

    private String[] getLangs(Player player) {
        return player != null ? LangUtils.getPlayerLanguage(player) : new String[]{libraryManager.getDefaultLang()};
    }

    private String translate(String key, Player player) {
        String[] langs = getLangs(player);
        return StringUtils.colorize(Translator.translateKyeWorld(libraryManager, key, langs));
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
