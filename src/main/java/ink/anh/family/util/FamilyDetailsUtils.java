package ink.anh.family.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import ink.anh.api.items.ItemStackSerializer;
import java.util.Map;
import java.util.UUID;

public class FamilyDetailsUtils {

    private static final Gson gson = new GsonBuilder().create();

    public static String serializeLocation(Location location) {
        if (location == null) {
            return null;
        }
        return location.getWorld().getName() + ";" +
               location.getX() + ";" +
               location.getY() + ";" +
               location.getZ() + ";" +
               location.getYaw() + ";" +
               location.getPitch();
    }

    public static Location deserializeLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        String[] parts = locationString.split(";");
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );
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
