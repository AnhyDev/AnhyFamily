package ink.anh.family.fdetails;

import ink.anh.family.GlobalManager;
import ink.anh.family.db.fdetails.FamilyDetailsTable;
import ink.anh.family.fplayer.FamilyUtils;
import ink.anh.family.fplayer.PlayerFamily;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class FamilyDetailsGet {

    private static FamilyDetailsTable familyDetailsTable = (FamilyDetailsTable) GlobalManager.getInstance().getDatabaseManager().getTable(FamilyDetails.class);
    private static FamilyDetailsCacheManager dataHandler = FamilyDetailsCacheManager.getInstance();

    public static FamilyDetails getFamilyDetails(UUID familyId) {
        if (familyId == null) {
            return null;
        }

        // Спочатку перевіряємо локальний кеш
        FamilyDetails familyDetails = dataHandler.getFamilyDetails(familyId);
        if (familyDetails != null) {
            return familyDetails;
        }

        // Якщо в кеші немає, завантажуємо з бази даних
        familyDetails = familyDetailsTable.getFamilyDetails(familyId);
        if (familyDetails != null) {
            // Додаємо в локальний кеш для подальшого використання
            dataHandler.addFamilyDetails(familyDetails);
        }

        return familyDetails;
    }

    public static FamilyDetails getRootFamilyDetails(UUID rootId) {
        return getRootFamilyDetailsInternal(rootId, null, null);
    }

    public static FamilyDetails getRootFamilyDetails(Player player) {
        return getRootFamilyDetailsInternal(null, player, null);
    }

    public static FamilyDetails getRootFamilyDetails(PlayerFamily playerFamily) {
        return getRootFamilyDetailsInternal(null, null, playerFamily);
    }

    public static FamilyDetails getFatherFamilyDetails(UUID rootId) {
        return getParentFamilyDetailsInternal(rootId, null, null, true);
    }

    public static FamilyDetails getFatherFamilyDetails(Player player) {
        return getParentFamilyDetailsInternal(null, player, null, true);
    }

    public static FamilyDetails getFatherFamilyDetails(PlayerFamily playerFamily) {
        return getParentFamilyDetailsInternal(null, null, playerFamily, true);
    }

    public static FamilyDetails getMotherFamilyDetails(UUID rootId) {
        return getParentFamilyDetailsInternal(rootId, null, null, false);
    }

    public static FamilyDetails getMotherFamilyDetails(Player player) {
        return getParentFamilyDetailsInternal(null, player, null, false);
    }

    public static FamilyDetails getMotherFamilyDetails(PlayerFamily playerFamily) {
        return getParentFamilyDetailsInternal(null, null, playerFamily, false);
    }

    public static List<FamilyDetails> getChildrenFamilyDetails(UUID rootId) {
        return getChildrenFamilyDetailsInternal(rootId, null, null);
    }

    public static List<FamilyDetails> getChildrenFamilyDetails(Player player) {
        return getChildrenFamilyDetailsInternal(null, player, null);
    }

    public static List<FamilyDetails> getChildrenFamilyDetails(PlayerFamily playerFamily) {
        return getChildrenFamilyDetailsInternal(null, null, playerFamily);
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

    public static Map<UUID, FamilyDetails> getAllFamilyDetails(PlayerFamily playerFamily) {
        Map<UUID, FamilyDetails> familyDetailsMap = new HashMap<>();
        Set<UUID> allUUIDs = new HashSet<>();

        // Отримуємо FamilyDetails для PlayerFamily
        FamilyDetails rootDetails = getRootFamilyDetailsInternal(null, null, playerFamily);
        
        if (rootDetails != null) {
            familyDetailsMap.put(rootDetails.getFamilyId(), rootDetails);

            // Додаємо UUID з childrenAccessMap
            allUUIDs.addAll(rootDetails.getChildrenAccessMap().keySet());

            // Додаємо UUID з ancestorsAccessMap
            allUUIDs.addAll(rootDetails.getAncestorsAccessMap().keySet());
        } 
        
        // Додаємо UUID дітей з PlayerFamily, якщо rootDetails == null
        allUUIDs.addAll(playerFamily.getChildren());

        // Додаємо UUID батьків з PlayerFamily, якщо rootDetails == null
        if (playerFamily.getFather() != null) {
            allUUIDs.add(playerFamily.getFather());
        }
        if (playerFamily.getMother() != null) {
            allUUIDs.add(playerFamily.getMother());
        }

        // Перетворюємо список UUID на Map<UUID, FamilyDetails>
        for (UUID uuid : allUUIDs) {
            FamilyDetails details = getRootFamilyDetailsInternal(uuid, null, null);
            if (details != null) {
                familyDetailsMap.put(uuid, details);
            }
        }

        return familyDetailsMap;
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

        rootDetails = getFamilyDetails(familyId);
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
