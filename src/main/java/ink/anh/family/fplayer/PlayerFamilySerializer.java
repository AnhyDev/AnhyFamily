package ink.anh.family.fplayer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import ink.anh.family.fplayer.permissions.AbstractPermission;
import ink.anh.family.fplayer.permissions.ActionsPermissions;

public class PlayerFamilySerializer {

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(AbstractPermission.class, new AbstractPermissionAdapter())
        .create();

    public static String serializeUuidSet(Set<UUID> uuidSet) {
        return gson.toJson(uuidSet);
    }

    public static Set<UUID> deserializeUuidSet(String uuidString) {
        Type setType = new TypeToken<Set<UUID>>() {}.getType();
        return gson.fromJson(uuidString, setType);
    }

    // Метод для серіалізації мапи
    public static String serializePermissionsMap(Map<ActionsPermissions, AbstractPermission> permissionsMap) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<ActionsPermissions, AbstractPermission> entry : permissionsMap.entrySet()) {
            JsonObject permissionObject = new JsonObject();
            permissionObject.addProperty("type", entry.getValue().getClass().getName());
            permissionObject.add("data", gson.toJsonTree(entry.getValue()));
            jsonObject.add(entry.getKey().name(), permissionObject);
        }
        return gson.toJson(jsonObject);
    }

    // Метод для десеріалізації мапи
    public static Map<ActionsPermissions, AbstractPermission> deserializePermissionsMap(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        Map<ActionsPermissions, AbstractPermission> permissionsMap = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            ActionsPermissions actionsPermissions = ActionsPermissions.valueOf(entry.getKey());
            JsonObject permissionObject = entry.getValue().getAsJsonObject();
            String className = permissionObject.get("type").getAsString();
            JsonElement dataElement = permissionObject.get("data");

            try {
                Class<?> clazz = Class.forName(className);
                AbstractPermission permission = (AbstractPermission) gson.fromJson(dataElement, clazz);
                permissionsMap.put(actionsPermissions, permission);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        return permissionsMap;
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
            JsonElement typeElement = jsonObject.get("type");
            JsonElement dataElement = jsonObject.get("data");

            if (typeElement == null || dataElement == null) {
                throw new JsonParseException("Missing 'type' or 'data' in JSON object");
            }

            String className = typeElement.getAsString();
            try {
                Class<?> clazz = Class.forName(className);
                return context.deserialize(dataElement, clazz);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
