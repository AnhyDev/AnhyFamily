package ink.anh.family.common;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ink.anh.family.util.Access;
import ink.anh.api.items.ItemStackSerializer;

public class FamilyDetails {

    private UUID familyId;
    private Location homeLocation;
    private ItemStack[] familyChest;
    private boolean childrenAccessHome;
    private boolean childrenAccessChest;
    private boolean ancestorsAccessHome;
    private boolean ancestorsAccessChest;
    private Map<UUID, Access> specificAccessMap = new HashMap<>();

    public FamilyDetails(UUID familyId, Location homeLocation, ItemStack[] familyChest, boolean childrenAccessHome, boolean childrenAccessChest, boolean ancestorsAccessHome, boolean ancestorsAccessChest) {
        this.familyId = familyId;
        this.homeLocation = homeLocation;
        this.familyChest = familyChest;
        this.childrenAccessHome = childrenAccessHome;
        this.childrenAccessChest = childrenAccessChest;
        this.ancestorsAccessHome = ancestorsAccessHome;
        this.ancestorsAccessChest = ancestorsAccessChest;
    }

    public FamilyDetails(UUID spouse1, UUID spouse2) {
        this.familyId = generateFamilyId(spouse1, spouse2);
        this.homeLocation = null;
        this.familyChest = new ItemStack[54];
        this.childrenAccessHome = false;
        this.childrenAccessChest = false;
        this.ancestorsAccessHome = false;
        this.ancestorsAccessChest = false;
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

    public static UUID generateFamilyId(UUID spouse1, UUID spouse2) {
        UUID namespace = UUID.nameUUIDFromBytes("FamilyDetails".getBytes());
        String combined = namespace.toString() + spouse1.toString() + spouse2.toString();
        return UUID.nameUUIDFromBytes(combined.getBytes());
    }

    // Метод для серіалізації
    public String serialize() {
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();
        
        JsonArray chestArray = new JsonArray();
        for (ItemStack item : familyChest) {
            chestArray.add(ItemStackSerializer.serializeItemStackToBase64(item));
        }
        jsonObject.add("familyChest", chestArray);
        
        return gson.toJson(jsonObject);
    }

    // Метод для десеріалізації
    public static FamilyDetails deserialize(String jsonString) {
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
        
        FamilyDetails familyDetails = gson.fromJson(jsonObject, FamilyDetails.class);
        JsonArray chestArray = jsonObject.getAsJsonArray("familyChest");
        ItemStack[] familyChest = new ItemStack[chestArray.size()];
        
        for (int i = 0; i < chestArray.size(); i++) {
            familyChest[i] = ItemStackSerializer.deserializeItemStackFromBase64(chestArray.get(i).getAsString());
        }
        familyDetails.setFamilyChest(familyChest);
        
        return familyDetails;
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
