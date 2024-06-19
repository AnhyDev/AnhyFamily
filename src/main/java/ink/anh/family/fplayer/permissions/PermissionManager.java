package ink.anh.family.fplayer.permissions;

import java.util.HashMap;
import java.util.Map;
import ink.anh.family.fplayer.PlayerFamily;

public class PermissionManager {

    public static Map<ActionsPermissions, AbstractPermission> createDefaultPermissionsMap(PlayerFamily playerFamily) {
        Map<ActionsPermissions, AbstractPermission> permissionsMap = new HashMap<>();
        
        permissionsMap.put(ActionsPermissions.HUGS_TO_ALL_PLAYERS, new HugsPermission());

        return permissionsMap;
    }

    public static AbstractPermission createPermission(ActionsPermissions action, PlayerFamily playerFamily) {
        switch (action) {
            case HUGS_TO_ALL_PLAYERS:
                return new HugsPermission();
            default:
                return null;
        }
    }
}
