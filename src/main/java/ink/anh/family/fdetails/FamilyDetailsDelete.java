package ink.anh.family.fdetails;

import ink.anh.family.AnhyFamily;
import ink.anh.family.db.fdetails.FamilyDetailsTable;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.FamilyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;

public class FamilyDetailsDelete {

    private static FamilyDetailsTable familyDetailsTable = (FamilyDetailsTable) AnhyFamily.getInstance().getGlobalManager().getDatabaseManager().getTable(FamilyDetails.class);
    private static FamilyDetailsDataHandler dataHandler = FamilyDetailsDataHandler.getInstance();

    public static void deleteFamilyDetails(UUID familyId) {
        if (familyId != null) {
            // Видаляємо з локальної мапи
            dataHandler.removeFamilyDetails(familyId);

            // Видаляємо з бази даних
            familyDetailsTable.deleteFamilyDetails(familyId);
        }
    }

    public static void deleteRootFamilyDetails(UUID rootId) {
        FamilyDetails rootDetails = getRootFamilyDetailsInternal(rootId, null, null);
        if (rootDetails != null) {
            dataHandler.removeRootDetails(rootId);
            deleteFamilyDetails(rootDetails.getFamilyId());
        }
    }

    public static void deleteRootFamilyDetails(Player player) {
        FamilyDetails rootDetails = getRootFamilyDetailsInternal(null, player, null);
        if (rootDetails != null) {
            dataHandler.removeRootDetails(player.getUniqueId());
            deleteFamilyDetails(rootDetails.getFamilyId());
        }
    }

    public static void deleteRootFamilyDetails(PlayerFamily playerFamily) {
        FamilyDetails rootDetails = getRootFamilyDetailsInternal(null, null, playerFamily);
        if (rootDetails != null) {
            dataHandler.removeRootDetails(playerFamily.getRoot());
            deleteFamilyDetails(rootDetails.getFamilyId());
        }
    }

    public static void deleteFatherFamilyDetails(UUID rootId) {
        FamilyDetails fatherDetails = getParentFamilyDetailsInternal(rootId, null, null, true);
        if (fatherDetails != null) {
            dataHandler.removeFatherDetails(rootId);
            deleteFamilyDetails(fatherDetails.getFamilyId());
        }
    }

    public static void deleteFatherFamilyDetails(Player player) {
        FamilyDetails fatherDetails = getParentFamilyDetailsInternal(null, player, null, true);
        if (fatherDetails != null) {
            dataHandler.removeFatherDetails(player.getUniqueId());
            deleteFamilyDetails(fatherDetails.getFamilyId());
        }
    }

    public static void deleteFatherFamilyDetails(PlayerFamily playerFamily) {
        FamilyDetails fatherDetails = getParentFamilyDetailsInternal(null, null, playerFamily, true);
        if (fatherDetails != null) {
            dataHandler.removeFatherDetails(playerFamily.getRoot());
            deleteFamilyDetails(fatherDetails.getFamilyId());
        }
    }

    public static void deleteMotherFamilyDetails(UUID rootId) {
        FamilyDetails motherDetails = getParentFamilyDetailsInternal(rootId, null, null, false);
        if (motherDetails != null) {
            dataHandler.removeMotherDetails(rootId);
            deleteFamilyDetails(motherDetails.getFamilyId());
        }
    }

    public static void deleteMotherFamilyDetails(Player player) {
        FamilyDetails motherDetails = getParentFamilyDetailsInternal(null, player, null, false);
        if (motherDetails != null) {
            dataHandler.removeMotherDetails(player.getUniqueId());
            deleteFamilyDetails(motherDetails.getFamilyId());
        }
    }

    public static void deleteMotherFamilyDetails(PlayerFamily playerFamily) {
        FamilyDetails motherDetails = getParentFamilyDetailsInternal(null, null, playerFamily, false);
        if (motherDetails != null) {
            dataHandler.removeMotherDetails(playerFamily.getRoot());
            deleteFamilyDetails(motherDetails.getFamilyId());
        }
    }

    public static void deleteChildrenFamilyDetails(UUID rootId) {
        List<FamilyDetails> childrenDetails = getChildrenFamilyDetailsInternal(rootId, null, null);
        if (childrenDetails != null) {
            for (FamilyDetails childDetails : childrenDetails) {
                deleteFamilyDetails(childDetails.getFamilyId());
            }
            dataHandler.removeChildrenDetails(rootId);
        }
    }

    public static void deleteChildrenFamilyDetails(Player player) {
        List<FamilyDetails> childrenDetails = getChildrenFamilyDetailsInternal(null, player, null);
        if (childrenDetails != null) {
            for (FamilyDetails childDetails : childrenDetails) {
                deleteFamilyDetails(childDetails.getFamilyId());
            }
            dataHandler.removeChildrenDetails(player.getUniqueId());
        }
    }

    public static void deleteChildrenFamilyDetails(PlayerFamily playerFamily) {
        List<FamilyDetails> childrenDetails = getChildrenFamilyDetailsInternal(null, null, playerFamily);
        if (childrenDetails != null) {
            for (FamilyDetails childDetails : childrenDetails) {
                deleteFamilyDetails(childDetails.getFamilyId());
            }
            dataHandler.removeChildrenDetails(playerFamily.getRoot());
        }
    }

    private static UUID resolveRootId(UUID rootId, Player player, PlayerFamily playerFamily) {
        if (rootId == null && player != null) {
            return player.getUniqueId();
        } else if (rootId == null && player == null && playerFamily != null) {
            return playerFamily.getRoot();
        } else {
            return rootId;
        }
    }

    private static PlayerFamily resolvePlayerFamily(UUID rootId, Player player, PlayerFamily playerFamily) {
        if (playerFamily == null) {
            if (player != null) {
                playerFamily = FamilyUtils.getFamily(player);
            } else if (rootId != null) {
                playerFamily = FamilyUtils.getFamily(rootId);
            }
        }
        return playerFamily;
    }

    private static FamilyDetails getRootFamilyDetailsInternal(UUID rootId, Player player, PlayerFamily playerFamily) {
        rootId = resolveRootId(rootId, player, playerFamily);
        FamilyDetails rootDetails = null;

        if (rootId != null) {
            rootDetails = dataHandler.getRootDetails(rootId);
        }

        if (rootDetails != null) {
            return rootDetails;
        }

        playerFamily = resolvePlayerFamily(rootId, player, playerFamily);
        if (playerFamily == null) {
            return null;
        }

        UUID familyId = playerFamily.getFamilyId();
        if (familyId == null) {
            return null;
        }

        rootDetails = FamilyDetailsGet.getFamilyDetails(familyId);
        if (rootDetails != null) {
            dataHandler.addRootDetails(rootId, rootDetails);
        }

        return rootDetails;
    }

    private static FamilyDetails getParentFamilyDetailsInternal(UUID rootId, Player player, PlayerFamily playerFamily, boolean isFather) {
        rootId = resolveRootId(rootId, player, playerFamily);
        FamilyDetails parentDetails = null;

        if (rootId != null) {
            parentDetails = isFather ? dataHandler.getFatherDetails(rootId) : dataHandler.getMotherDetails(rootId);
        }

        if (parentDetails != null) {
            return parentDetails;
        }

        playerFamily = resolvePlayerFamily(rootId, player, playerFamily);
        if (playerFamily == null) {
            return null;
        }

        UUID parentId = isFather ? playerFamily.getFather() : playerFamily.getMother();
        if (parentId == null) {
            return null;
        }

        parentDetails = getRootFamilyDetailsInternal(parentId, null, null);
        if (parentDetails != null) {
            if (isFather) {
                dataHandler.addFatherDetails(rootId, parentDetails);
            } else {
                dataHandler.addMotherDetails(rootId, parentDetails);
            }
        }

        return parentDetails;
    }

    private static List<FamilyDetails> getChildrenFamilyDetailsInternal(UUID rootId, Player player, PlayerFamily playerFamily) {
        rootId = resolveRootId(rootId, player, playerFamily);
        List<FamilyDetails> childrenDetails = null;

        if (rootId != null) {
            childrenDetails = dataHandler.getChildrenDetails(rootId);
        }

        if (childrenDetails != null && !childrenDetails.isEmpty()) {
            return childrenDetails;
        }

        playerFamily = resolvePlayerFamily(rootId, player, playerFamily);
        if (playerFamily == null) {
            return null;
        }

        List<FamilyDetails> loadedChildrenDetails = new ArrayList<>();
        for (UUID childId : playerFamily.getChildren()) {
            FamilyDetails childDetails = getRootFamilyDetailsInternal(childId, null, null);
            if (childDetails != null) {
                loadedChildrenDetails.add(childDetails);
            }
        }

        if (!loadedChildrenDetails.isEmpty()) {
            dataHandler.addChildrenDetails(rootId, loadedChildrenDetails);
        }

        return loadedChildrenDetails;
    }
}
