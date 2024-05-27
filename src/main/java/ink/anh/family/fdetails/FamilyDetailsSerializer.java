package ink.anh.family.fdetails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ink.anh.api.items.ItemStackSerializer;
import ink.anh.api.enums.Access;

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

    public static String serializeSpecificAccessMap(Map<UUID, Access> specificAccessMap) {
        return gson.toJson(specificAccessMap);
    }
}
