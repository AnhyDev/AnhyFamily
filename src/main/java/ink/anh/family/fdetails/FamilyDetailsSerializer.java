package ink.anh.family.fdetails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import ink.anh.api.items.ItemStackSerializer;
import ink.anh.family.fdetails.chest.Chest;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class FamilyDetailsSerializer {

    private static final Gson gson = new GsonBuilder().create();

    public static String serializeLocation(Location location) {
        if (location == null) {
            return null;
        }
        return gson.toJson(location.serialize());
    }

    public static Location deserializeLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> locationMap = gson.fromJson(locationString, type);
        return Location.deserialize(locationMap);
    }

    public static String serializeFamilyChest(ItemStack[] familyChest) {
        JsonArray jsonArray = new JsonArray();
        for (ItemStack itemStack : familyChest) {
            jsonArray.add(ItemStackSerializer.serializeItemStackToBase64(itemStack));
        }
        return gson.toJson(jsonArray);
    }

    public static ItemStack[] deserializeFamilyChest(String familyChestString) {
        JsonArray jsonArray = gson.fromJson(familyChestString, JsonArray.class);
        ItemStack[] familyChest = new ItemStack[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            familyChest[i] = ItemStackSerializer.deserializeItemStackFromBase64(jsonArray.get(i).getAsString());
        }
        return familyChest;
    }

    public static String serializeAccessControl(AccessControl accessControl) {
        return gson.toJson(accessControl);
    }

    public static AccessControl deserializeAccessControl(String accessControlString) {
        return gson.fromJson(accessControlString, AccessControl.class);
    }

    public static String serializeAccessControlMap(Map<UUID, AccessControl> accessControlMap) {
        return gson.toJson(accessControlMap);
    }

    public static Map<UUID, AccessControl> deserializeAccessControlMap(String accessControlMapString) {
        Type type = new TypeToken<Map<UUID, AccessControl>>() {}.getType();
        return gson.fromJson(accessControlMapString, type);
    }

    public static String serializeChest(Chest chest) {
        if (chest == null) {
            return null;
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("chestLocation", serializeLocation(chest.getChestLocation()));
        jsonObject.addProperty("familyChest", serializeFamilyChest(chest.getFamilyChest()));
        return gson.toJson(jsonObject);
    }

    public static Chest deserializeChest(String chestString) {
        if (chestString == null || chestString.equals("null") || chestString.isEmpty()) {
            return null;
        }

        JsonObject jsonObject = JsonParser.parseString(chestString).getAsJsonObject();
        Location chestLocation = deserializeLocation(jsonObject.get("chestLocation").getAsString());
        ItemStack[] familyChest = deserializeFamilyChest(jsonObject.get("familyChest").getAsString());
        return new Chest(familyChest, chestLocation);
    }
}
