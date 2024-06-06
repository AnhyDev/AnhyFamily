package ink.anh.family.fdetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;

import ink.anh.api.enums.Access;
import ink.anh.family.fdetails.chest.Chest;
import ink.anh.family.fplayer.FamilyRelationType;
import ink.anh.family.fplayer.PlayerFamily;

public class FamilyDetails {

    private UUID familyId;
    private String familySymbol;
    private Location homeLocation = null;
    private Chest familyChest = null;
    private AccessControl childrenAccess = new AccessControl(Access.FALSE, Access.FALSE, Access.FALSE);
    private AccessControl ancestorsAccess = new AccessControl(Access.FALSE, Access.FALSE, Access.FALSE);
    private Map<UUID, AccessControl> childrenAccessMap = new HashMap<>();
    private Map<UUID, AccessControl> ancestorsAccessMap = new HashMap<>();
    private LocalDateTime homeSetDate = null;

    public FamilyDetails(UUID familyId, String familySymbol, Location homeLocation, Chest familyChest, AccessControl childrenAccess, AccessControl ancestorsAccess,
                         Map<UUID, AccessControl> childrenAccessMap, Map<UUID, AccessControl> ancestorsAccessMap, LocalDateTime homeSetDate) {
        this.familyId = familyId;
        this.familySymbol = familySymbol;
        this.homeLocation = homeLocation;
        this.familyChest = familyChest;
        this.childrenAccess = childrenAccess;
        this.ancestorsAccess = ancestorsAccess;
        this.childrenAccessMap = childrenAccessMap;
        this.ancestorsAccessMap = ancestorsAccessMap;
        this.homeSetDate = homeSetDate;
    }

    public FamilyDetails(UUID spouse1, UUID spouse2) {
        this.familyId = generateFamilyId(spouse1, spouse2);
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public String getFamilySymbol() {
        return familySymbol;
    }

    public void setFamilySymbol(String familySymbol) {
        this.familySymbol = familySymbol;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
        this.homeSetDate = LocalDateTime.now();
    }

    public Chest getFamilyChest() {
        return familyChest;
    }

    public void setFamilyChest(Chest familyChest) {
        this.familyChest = familyChest;
    }

    public AccessControl getChildrenAccess() {
        return childrenAccess;
    }

    public void setChildrenAccess(AccessControl childrenAccess) {
        this.childrenAccess = childrenAccess;
    }

    public AccessControl getAncestorsAccess() {
        return ancestorsAccess;
    }

    public void setAncestorsAccess(AccessControl ancestorsAccess) {
        this.ancestorsAccess = ancestorsAccess;
    }

    public Map<UUID, AccessControl> getChildrenAccessMap() {
        return childrenAccessMap;
    }

    public void setChildrenAccessMap(Map<UUID, AccessControl> childrenAccessMap) {
        this.childrenAccessMap = childrenAccessMap;
    }

    public Map<UUID, AccessControl> getAncestorsAccessMap() {
        return ancestorsAccessMap;
    }

    public void setAncestorsAccessMap(Map<UUID, AccessControl> ancestorsAccessMap) {
        this.ancestorsAccessMap = ancestorsAccessMap;
    }

    public LocalDateTime getHomeSetDate() {
        return homeSetDate;
    }

    public void setHomeSetDate(LocalDateTime homeSetDate) {
        this.homeSetDate = homeSetDate;
    }

    public static UUID generateFamilyId(UUID spouse1, UUID spouse2) {
        UUID namespace = UUID.nameUUIDFromBytes("FamilyDetails".getBytes());

        UUID[] uuids = {spouse1, spouse2};
        Arrays.sort(uuids);

        String combined = namespace.toString() + uuids[0].toString() + uuids[1].toString();
        return UUID.nameUUIDFromBytes(combined.getBytes());
    }

    public boolean canChangeHome(int minutes) {
        if (homeSetDate == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(homeSetDate, now);
        return duration.toMinutes() >= minutes;
    }

    private boolean hasAccess(PlayerFamily playerFamily, String accessType) {
        FamilyRelationType relationType = getRelationType(playerFamily);

        switch (relationType) {
            case FAMILY_ID:
                return true;
            case PARENT_FAMILY_ID:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), ancestorsAccessMap, ancestorsAccess, accessType);
            case CHILD_FAMILY_IDS:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), childrenAccessMap, childrenAccess, accessType);
            default:
                return false;
        }
    }

    public FamilyRelationType getRelationType(PlayerFamily playerFamily) {
        UUID playerId = playerFamily.getRoot();

        if (playerFamily.getFamilyId().equals(familyId)) {
            return FamilyRelationType.FAMILY_ID;
        }
        if (ancestorsAccessMap.containsKey(playerId)) {
            return FamilyRelationType.PARENT_FAMILY_ID;
        }
        if (childrenAccessMap.containsKey(playerId)) {
            return FamilyRelationType.CHILD_FAMILY_IDS;
        }
        return FamilyRelationType.NOT_FOUND;
    }

    public boolean hasAccessHome(PlayerFamily playerFamily) {
        return hasAccess(playerFamily, "homeAccess");
    }

    public boolean hasAccessChest(PlayerFamily playerFamily) {
        return hasAccess(playerFamily, "chestAccess");
    }

    public boolean hasAccessChat(PlayerFamily playerFamily) {
        return hasAccess(playerFamily, "chatAccess");
    }

    private boolean checkSpecificOrDefaultAccess(UUID playerId, Map<UUID, AccessControl> accessMap, AccessControl defaultAccessControl, String accessType) {
        AccessControl accessControl = accessMap.get(playerId);

        Access access;
        if (accessControl != null) {
            switch (accessType) {
                case "homeAccess":
                    access = accessControl.getHomeAccess();
                    break;
                case "chestAccess":
                    access = accessControl.getChestAccess();
                    break;
                case "chatAccess":
                    access = accessControl.getChatAccess();
                    break;
                default:
                    return false;
            }
        } else {
            switch (accessType) {
                case "homeAccess":
                    access = defaultAccessControl.getHomeAccess();
                    break;
                case "chestAccess":
                    access = defaultAccessControl.getChestAccess();
                    break;
                case "chatAccess":
                    access = defaultAccessControl.getChatAccess();
                    break;
                default:
                    return false;
            }
        }

        switch (access) {
            case TRUE:
                return true;
            case FALSE:
                return false;
            case DEFAULT:
                return defaultAccessControl.getHomeAccess() == Access.TRUE;
            default:
                return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(familyId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FamilyDetails other = (FamilyDetails) obj;
        return Objects.equals(familyId, other.familyId);
    }
}
