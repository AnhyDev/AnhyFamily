package ink.anh.family.info;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;

import ink.anh.family.common.Family;
import ink.anh.family.util.FamilyUtils;

public class FamilyTree {
    private FamilyRepeated root;
    private Map<UUID, FamilyRepeated> rootParents;
    private Map<UUID, FamilyRepeated> rootOffspring;

    public FamilyTree(UUID rootUuid) {
        this.root = new FamilyRepeated(FamilyUtils.getFamily(rootUuid));
        this.rootParents = new HashMap<>();
        this.rootOffspring = new HashMap<>();
        this.rootOffspring = buildDescendantsTree(this.root);
        resetRoot();
        buildAncestorsTree(this.root);
    }

    public FamilyTree(Family rootFamily) {
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

        Family member = memberFam.getFamily();
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
        StringBuilder treeString = new StringBuilder(ChatColor.GOLD + "family_tree_title" 
        		+ ChatColor.BOLD + root.getFamily().getRootrNickName() + ChatColor.RESET + "\n");
        buildDescendantsTreeString(root, 0, "", treeString);

        resetRoot();

        buildFamilyTreeString(root, 0, "", treeString);
        return treeString.toString();
    }

    private void buildFamilyTreeString(FamilyRepeated memberFam, int level, String prefix, StringBuilder treeString) {
    	Family member = memberFam.getFamily();
        boolean isRepeated = memberFam.getRepeated() > 0;
        ChatColor color = determineColor(level, memberFam.getRepeated());

        String repeatedMark = isRepeated ? "*" : "";
        String title = (level == 0 ? "family_tree_ancestors" : "");
        StringBuilder line = new StringBuilder(prefix);
        line.append(color).append("└─ ").append(title).append(member.getRootrNickName()).append(repeatedMark).append('\n');

        treeString.append(line.toString());

        if (!isRepeated) {
            UUID fatherUuid = member.getFather();
            UUID motherUuid = member.getMother();

            if (fatherUuid != null) {
                Family father = rootParents.get(fatherUuid).getFamily();
                if (father != null) {
                    buildFamilyTreeString(new FamilyRepeated(father), level + 1, prefix + "  ", treeString);
                }
            }

            if (motherUuid != null) {
                Family mother = rootParents.get(motherUuid).getFamily();
                if (mother != null) {
                    buildFamilyTreeString(new FamilyRepeated(mother), level + 1, prefix + "  ", treeString);
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

        Family member = memberFam.getFamily();
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
    	Family member = memberFam.getFamily();
        boolean isRepeated = memberFam.getRepeated() > 0;
        ChatColor color = determineColor(level, memberFam.getRepeated());

        String repeatedMark = isRepeated ? "*" : "";
        String title = (level == 0 ? "family_tree_descendants" : "");
        StringBuilder line = new StringBuilder(prefix);
        line.append(color).append("┌─ ").append(title).append(member.getRootrNickName()).append(repeatedMark).append('\n');

        treeString.insert(0, line.toString());

        Set<UUID> childrenUuids = member.getChildren();
        if (childrenUuids != null) {
            for (UUID childUuid : childrenUuids) {
            	
                Family child = rootOffspring.get(childUuid).getFamily();
                
                if (child != null && !isRepeated) {
                    buildDescendantsTreeString(new FamilyRepeated(child), level + 1, prefix + "  ", treeString);
                }
            }
        }
        memberFam.increaseRepeated();
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
            default: return ChatColor.GRAY;
        }
    }

    public Family getMember(UUID uuid) {
        return rootParents.get(uuid).getFamily();
    }

    public boolean addMember(Family member) {
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
    	
    	private Family family;
    	private int repeated;
    	private boolean processed;
    	
		public FamilyRepeated(Family family) {
			this.family = family;
			this.repeated = 0;
		}

		public Family getFamily() {
			return family;
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
