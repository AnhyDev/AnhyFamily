package ink.anh.family.fplayer;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import ink.anh.family.fplayer.permissions.AbstractPermission;
import ink.anh.family.fplayer.permissions.ActionsPermissions;

public class PlayerFamilySerializer {

    private static final Gson gson = new Gson();

    public static String serializeUuidSet(Set<UUID> uuidSet) {
        return gson.toJson(uuidSet);
    }

    public static Set<UUID> deserializeUuidSet(String uuidString) {
        Type setType = new TypeToken<Set<UUID>>(){}.getType();
        return gson.fromJson(uuidString, setType);
    }

    // Метод для серіалізації мапи
    public static String serializePermissionsMap(Map<ActionsPermissions, AbstractPermission> permissionsMap) {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(AbstractPermission.class, new AbstractPermissionAdapter())
            .create();
        return gson.toJson(permissionsMap);
    }

    // Метод для десеріалізації мапи
    public static Map<ActionsPermissions, AbstractPermission> deserializePermissionsMap(String json) {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(AbstractPermission.class, new AbstractPermissionAdapter())
            .create();
        Type type = new TypeToken<Map<ActionsPermissions, AbstractPermission>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private static class AbstractPermissionAdapter implements JsonSerializer<AbstractPermission>, JsonDeserializer<AbstractPermission> {

        @Override
        public JsonElement serialize(AbstractPermission src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", src.getClass().getName());
            jsonObject.add("data", context.serialize(src, src.getClass()));
            return jsonObject;
        }

        @Override
        public AbstractPermission deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String className = jsonObject.get("type").getAsString();
            try {
                Class<?> clazz = Class.forName(className);
                return context.deserialize(jsonObject.get("data"), clazz);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
