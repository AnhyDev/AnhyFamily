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
    private boolean childrenAccessHome = false;
    private boolean childrenAccessChest = false;
    private boolean ancestorsAccessHome = false;
    private boolean ancestorsAccessChest = false;
    private Map<UUID, Access> specificAccessMap = new HashMap<>();
    private LocalDateTime homeSetDate = null;

    public FamilyDetails(UUID familyId, Location homeLocation, ItemStack[] familyChest, boolean childrenAccessHome, boolean childrenAccessChest, boolean ancestorsAccessHome, boolean ancestorsAccessChest, LocalDateTime homeSetDate) {
        this.familyId = familyId;
        this.homeLocation = homeLocation;
        this.familyChest = familyChest;
        this.childrenAccessHome = childrenAccessHome;
        this.childrenAccessChest = childrenAccessChest;
        this.ancestorsAccessHome = ancestorsAccessHome;
        this.ancestorsAccessChest = ancestorsAccessChest;
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

    public boolean isChildrenAccessHome() {
        return childrenAccessHome;
    }

    public void setChildrenAccessHome(boolean childrenAccessHome) {
        this.childrenAccessHome = childrenAccessHome;
    }

    public boolean isChildrenAccessChest() {
        return childrenAccessChest;
    }

    public void setChildrenAccessChest(boolean childrenAccessChest) {
        this.childrenAccessChest = childrenAccessChest;
    }

    public boolean isAncestorsAccessHome() {
        return ancestorsAccessHome;
    }

    public void setAncestorsAccessHome(boolean ancestorsAccessHome) {
        this.ancestorsAccessHome = ancestorsAccessHome;
    }

    public boolean isAncestorsAccessChest() {
        return ancestorsAccessChest;
    }

    public void setAncestorsAccessChest(boolean ancestorsAccessChest) {
        this.ancestorsAccessChest = ancestorsAccessChest;
    }

    public Map<UUID, Access> getSpecificAccessMap() {
        return specificAccessMap;
    }

    public void setSpecificAccessMap(Map<UUID, Access> specificAccessMap) {
        this.specificAccessMap = specificAccessMap;
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
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), ancestorsAccessHome);
            case CHILD_FAMILY_IDS:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), childrenAccessHome);
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
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), ancestorsAccessChest);
            case CHILD_FAMILY_IDS:
                return checkSpecificOrDefaultAccess(playerFamily.getRoot(), childrenAccessChest);
            default:
                return false;
        }
    }

    // Метод для перевірки специфічного або за замовчуванням доступу
    private boolean checkSpecificOrDefaultAccess(UUID playerId, boolean defaultAccess) {
        Access access = specificAccessMap.get(playerId);

        if (access != null) {
            switch (access) {
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                case DEFAULT:
                    return defaultAccess;
            }
        }

        return defaultAccess;
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