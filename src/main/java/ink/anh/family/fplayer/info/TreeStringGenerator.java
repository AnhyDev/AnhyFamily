package ink.anh.family.fplayer.info;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;

import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.gender.GenderManager;
import ink.anh.family.util.FamilyUtils;

public class TreeStringGenerator {
    private FamilyRepeated root;
    private Map<UUID, FamilyRepeated> rootParents;
    private Map<UUID, FamilyRepeated> rootOffspring;

    public TreeStringGenerator(UUID rootUuid) {
        this.root = new FamilyRepeated(FamilyUtils.getFamily(rootUuid));
        this.rootParents = new HashMap<>();
        this.rootOffspring = new HashMap<>();
        this.rootOffspring = buildDescendantsTree(this.root);
        resetRoot();
        buildAncestorsTree(this.root);
    }

    public TreeStringGenerator(PlayerFamily rootFamily) {
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
            return; // Припинити, якщо особа вже була оброблена
        }

        memberFam.setProcessed(true);

        PlayerFamily member = memberFam.getFamily();
        UUID fatherUuid = member.getFather();
        UUID motherUuid = member.getMother();

        // Логіка для обробки батька
        if (fatherUuid != null) {
            FamilyRepeated father = rootParents.getOrDefault(fatherUuid, new FamilyRepeated(FamilyUtils.getFamily(fatherUuid)));
            rootParents.put(fatherUuid, father);
            buildAncestorsTree(father);
        };

        // Логіка для обробки матері
        if (motherUuid != null) {
            FamilyRepeated mother = rootParents.getOrDefault(motherUuid, new FamilyRepeated(FamilyUtils.getFamily(motherUuid)));
            rootParents.put(motherUuid, mother);
            buildAncestorsTree(mother);
        }
    }

    public String buildFamilyTreeString() {
        StringBuilder treeString = new StringBuilder();
        
        StringBuilder rootString = new StringBuilder()
                .append(ChatColor.GOLD)
                .append(" family_tree_title ")
                .append(ChatColor.BOLD)
                .append(getFormattedName(root.getFamily()))
                .append(ChatColor.RESET)
                .append("\n");

        buildDescendantsTreeString(root, 0, "", treeString);
        treeString.append(rootString);
        resetRoot();
        buildAncestorsTreeString(root, 0, "", treeString);
        return treeString.toString();
    }

    private void buildAncestorsTreeString(FamilyRepeated memberFam, int level, String prefix, StringBuilder treeString) {
        PlayerFamily member = memberFam.getFamily();
        boolean isRepeated = memberFam.getRepeated() > 0;
        String title = (level == 0 ? "family_tree_ancestors " : "");

        String branchSymbol = "└─ ";
        String line = buildMemberLine(member, level, prefix, isRepeated, title, branchSymbol);
        treeString.append(line);

        if (!isRepeated) {
            UUID fatherUuid = member.getFather();
            UUID motherUuid = member.getMother();

            if (fatherUuid != null) {
                PlayerFamily father = rootParents.get(fatherUuid).getFamily();
                if (father != null) {
                	buildAncestorsTreeString(new FamilyRepeated(father), level + 1, prefix + "  ", treeString);
                }
            }

            if (motherUuid != null) {
                PlayerFamily mother = rootParents.get(motherUuid).getFamily();
                if (mother != null) {
                	buildAncestorsTreeString(new FamilyRepeated(mother), level + 1, prefix + "  ", treeString);
                }
            }
        }
        memberFam.increaseRepeated();
    }

    private Map<UUID, FamilyRepeated> buildDescendantsTree(FamilyRepeated memberFam) {
        Map<UUID, FamilyRepeated> children = new HashMap<>();

        if (memberFam.isProcessed()) {
            return children; // Повертаємо порожню мапу, якщо особа вже була оброблена
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

    private void buildDescendantsTreeString(FamilyRepeated memberFam, int level, String prefix, StringBuilder treeString) {
        PlayerFamily member = memberFam.getFamily();
        boolean isRepeated = memberFam.getRepeated() > 0;

        // Рекурсивно додаємо всіх дітей спочатку
        Set<UUID> childrenUuids = member.getChildren();
        if (childrenUuids != null) {
            for (UUID childUuid : childrenUuids) {
                PlayerFamily child = rootOffspring.get(childUuid).getFamily();
                if (child != null && !isRepeated) {
                    buildDescendantsTreeString(new FamilyRepeated(child), level + 1, prefix + " ", treeString);
                }
            }
        }

        // Додаємо інформацію про нащадків після обробки дітей
        String title = (level == 0 ? "family_tree_descendants " : "");
        String branchSymbol = level == 0 ? "┌─ " : prefix + "┌─ ";
        String line = buildMemberLine(member, level, prefix, isRepeated, title, branchSymbol);
        treeString.append(line);

        memberFam.increaseRepeated();
    }

    private String buildMemberLine(PlayerFamily member, int level, String prefix, boolean isRepeated, String title, String branchSymbol) {
        StringBuilder line = new StringBuilder(prefix);
        ChatColor color = determineColor(level, isRepeated ? 1 : 0);
        String repeatedMark = isRepeated ? "*" : "";

        line.append(color)
            .append(branchSymbol)
            .append(title);

        if (level > 0) {
            line.append(getFormattedName(member));
        }

        line.append(repeatedMark)
            .append('\n');

        return line.toString();
    }

    private String getFormattedName(PlayerFamily member) {
        Gender gender = GenderManager.getGender(member.getRoot());
        
        String genderSymbol = Gender.getMinecraftColor(gender) + Gender.getSymbol(gender);
        StringBuilder formattedName = new StringBuilder();
        if (member.getFirstName() != null && !member.getFirstName().isEmpty()) {
            formattedName.append(member.getFirstName()).append(" ");
        }
        String actualLastName = member.getActualLastName();
        if (actualLastName != null && !actualLastName.isEmpty()) {
            formattedName.append(actualLastName).append(" ");
        }
        formattedName.append("(").append(member.getRootrNickName()).append(")");
        return " §r(" + genderSymbol + "§r) " + ChatColor.YELLOW + formattedName.toString();
    }
    
    private ChatColor determineColor(int level, int repeatedCount) {
        if (repeatedCount > 0) {
            return ChatColor.DARK_RED;
        }
        switch (level) {
            case 0: return ChatColor.GREEN;
            case 1: return ChatColor.YELLOW;
            case 2: return ChatColor.BLUE;
            case 3: return ChatColor.AQUA;
            case 4: return ChatColor.LIGHT_PURPLE;
            case 5: return ChatColor.DARK_AQUA;
            case 6: return ChatColor.DARK_PURPLE;
            default: return ChatColor.GRAY;
        }
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
