package ink.anh.family.fdetails;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Arrays;
import java.util.HashMap;
import java.time.Duration;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ink.anh.family.fplayer.FamilyRelationType;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.api.enums.Access;

public class FamilyDetails {

    private UUID familyId;
    private Location homeLocation = null;
    private ItemStack[] familyChest = new ItemStack[54];
    private Access childrenAccess = Access.FALSE;
    private Access ancestorsAccess = Access.FALSE;
    private Map<UUID, AccessControl> childrenAccessMap = new HashMap<>();
    private Map<UUID, AccessControl> ancestorsAccessMap = new HashMap<>();
    private LocalDateTime homeSetDate = null;

    public FamilyDetails(UUID familyId, Location homeLocation, ItemStack[] familyChest, Access childrenAccess, Access ancestorsAccess,
            Map<UUID, AccessControl> childrenAccessMap, Map<UUID, AccessControl> ancestorsAccessMap, LocalDateTime homeSetDate) {
        this.familyId = familyId;
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

    public Location getHomeLocation() {
        return homeLocation;
    }

    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
        this.homeSetDate = LocalDateTime.now();
    }

    public ItemStack[] getFamilyChest() {
        return familyChest;
    }

    public void setFamilyChest(ItemStack[] familyChest) {
        this.familyChest = familyChest;
    }

    public Access getChildrenAccess() {
        return childrenAccess;
    }

    public void setChildrenAccess(Access childrenAccess) {
        this.childrenAccess = childrenAccess;
    }

    public Access getAncestorsAccess() {
        return ancestorsAccess;
    }

    public void setAncestorsAccess(Access ancestorsAccess) {
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

        // Відсортуємо UUID за допомогою compareTo
        UUID[] uuids = {spouse1, spouse2};
        Arrays.sort(uuids);

        // Об'єднаємо відсортовані UUID
        String combined = namespace.toString() + uuids[0].toString() + uuids[1].toString();
        return UUID.nameUUIDFromBytes(combined.getBytes());
    }

    public boolean canChangeHome(int minutes) {
        if (homeSetDate == null) {
            return true; // Якщо дата не встановлена, можна змінювати
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(homeSetDate, now);
        return duration.toMinutes() >= minutes;
    }

    // Метод для перевірки доступу до дому
    public boolean hasAccessHome(PlayerFamily playerFamily) {
        FamilyRelationType relationType = playerFamily.checkUUIDRelation(this.familyId);

        switch (relationType) {
            case FAMILY_ID:
                return true;
            case PARENT_FAMILY_ID:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), ancestorsAccessMap, ancestorsAccess, "homeAccess");
            case CHILD_FAMILY_IDS:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), childrenAccessMap, childrenAccess, "homeAccess");
            default:
                return false;
        }
    }

    // Метод для перевірки доступу до скрині
    public boolean hasAccessChest(PlayerFamily playerFamily) {
        FamilyRelationType relationType = playerFamily.checkUUIDRelation(this.familyId);

        switch (relationType) {
            case FAMILY_ID:
                return true;
            case PARENT_FAMILY_ID:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), ancestorsAccessMap, ancestorsAccess, "chestAccess");
            case CHILD_FAMILY_IDS:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), childrenAccessMap, childrenAccess, "chestAccess");
            default:
                return false;
        }
    }

    // Метод для перевірки специфічного або за замовчуванням доступу
    private boolean checkSpecificOrDefaultAccess(UUID playerId, Map<UUID, AccessControl> accessMap, Access defaultAccess, String accessType) {
        AccessControl accessControl = accessMap.get(playerId);

        if (accessControl != null) {
            Access access;
            switch (accessType) {
                case "homeAccess":
                    access = accessControl.getHomeAccess();
                    break;
                case "chestAccess":
                    access = accessControl.getChestAccess();
                    break;
                default:
                    return defaultAccess == Access.TRUE;
            }

            switch (access) {
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                case DEFAULT:
                    return defaultAccess == Access.TRUE;
            }
        }

        return defaultAccess == Access.TRUE;
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
