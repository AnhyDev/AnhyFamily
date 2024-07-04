package ink.anh.family.fplayer.permissions;

import ink.anh.api.enums.Access;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;

import java.util.Map;
import java.util.UUID;

public class PermissionModifier {

    // Метод для додавання дозволу
    public static void addPermission(PlayerFamily playerFamily, ActionsPermissions action, AbstractPermission permission) {
        Map<ActionsPermissions, AbstractPermission> permissionsMap = playerFamily.getPermissionsMap();
        permissionsMap.put(action, permission);
        playerFamily.setPermissionsMap(permissionsMap);

        // Збереження змін у базі даних
        PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.PERMISSIONS_MAP);
    }

    // Метод для видалення дозволу
    public static void removePermission(PlayerFamily playerFamily, ActionsPermissions action) {
        Map<ActionsPermissions, AbstractPermission> permissionsMap = playerFamily.getPermissionsMap();
        permissionsMap.remove(action);
        playerFamily.setPermissionsMap(permissionsMap);

        // Збереження змін у базі даних
        PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.PERMISSIONS_MAP);
    }

    // Метод для зміни дозволу
    public static void setPermission(PlayerFamily playerFamily, UUID uuid, ActionsPermissions action, Access access) {
        Map<ActionsPermissions, AbstractPermission> permissionsMap = playerFamily.getPermissionsMap();
        AbstractPermission permission = permissionsMap.get(action);

        if (permission == null) {
            permission = PermissionManager.createPermission(action, playerFamily);
        }

        if (permission != null) {
            permission.setPermission(uuid, access);
            permissionsMap.put(action, permission);

            // Збереження змін у базі даних
            PlayerFamilyDBService.savePlayerFamily(playerFamily, FamilyPlayerField.PERMISSIONS_MAP);
        }
    }

    // Метод для отримання дозволу
    public static Access getPermission(PlayerFamily playerFamily, UUID uuid, ActionsPermissions action) {
        Map<ActionsPermissions, AbstractPermission> permissionsMap = playerFamily.getPermissionsMap();
        AbstractPermission permission = permissionsMap.get(action);

        if (permission != null) {
            return permission.getPermission(playerFamily, null, null);
        }
        return Access.DEFAULT;
    }
}
