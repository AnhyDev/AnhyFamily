package ink.anh.family.fplayer.permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ink.anh.api.enums.Access;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.util.TypeTargetComponent;

public abstract class AbstractPermission {
    
    protected Map<UUID, Access> permissionsMap = new HashMap<>();
    protected boolean allowAll = false;
    protected boolean denyAllExceptFamily = false;

    public abstract ActionsPermissions getActionsPermissions();

    /**
     * Determines the access level for a player considering personal, global, and detailed permissions.
     * 
     * @param playerFamily the {@link PlayerFamily} object representing the player for whom the access is being checked.
     * @param details the {@link FamilyDetails} object containing detailed information about the family.
     * @param typeTargetComponent the {@link TypeTargetComponent} representing the type of component for which access is being checked.
     * @return the {@link Access} object representing the access level.
     * 
     * Logic of condition checks:
     * <ol>
     *     <li>Personal permission {@link Access#FALSE}:
     *         <ul>
     *             <li>If the personal permission for the player is set to {@link Access#FALSE}, {@link Access#FALSE} is returned.</li>
     *         </ul>
     *     </li>
     *     <li>Global permission {@code allowAll} or personal permission {@link Access#TRUE}:
     *         <ul>
     *             <li>If the global permission {@code allowAll} is set to {@code true}, or if the personal permission for the player is set to {@link Access#TRUE}, {@link Access#TRUE} is returned.</li>
     *         </ul>
     *     </li>
     *     <li>Retrieving access from {@link FamilyDetails}:
     *         <ul>
     *             <li>The access from {@link FamilyDetails} for the player and the corresponding component type is determined.</li>
     *         </ul>
     *     </li>
     *     <li>Global restriction {@code denyAllExceptFamily}:
     *         <ul>
     *             <li>If the global restriction {@code denyAllExceptFamily} is set to {@code true}, and the access from {@link FamilyDetails} is not {@link Access#TRUE}, {@link Access#FALSE} is returned.</li>
     *         </ul>
     *     </li>
     *     <li>Personal permission {@code null} or {@link Access#DEFAULT} and access from {@link FamilyDetails} not {@link Access#FALSE}:
     *         <ul>
     *             <li>If the personal permission is absent (i.e., {@code null}) or set to {@link Access#DEFAULT}, and the access from {@link FamilyDetails} is not {@link Access#FALSE}, {@link Access#TRUE} is returned.</li>
     *         </ul>
     *     </li>
     *     <li>Returning {@link Access#FALSE} by default:
     *         <ul>
     *             <li>If none of the previous conditions are met, {@link Access#FALSE} is returned.</li>
     *         </ul>
     *     </li>
     * </ol>
     */
    public Access getPermission(PlayerFamily playerFamily, FamilyDetails details, TypeTargetComponent typeTargetComponent) {
        Access access = permissionsMap.get(playerFamily.getRoot());

        if (access == Access.FALSE) {
            return Access.FALSE;
        }

        if (allowAll || access == Access.TRUE) {
            return Access.TRUE;
        }

        Access detailsAccess = getFamilyDetailsAccess(playerFamily, details, typeTargetComponent);

        if (denyAllExceptFamily && detailsAccess != Access.TRUE) {
            return Access.FALSE;
        }

        if ((access == null || access == Access.DEFAULT) && detailsAccess != Access.FALSE) {
            return Access.TRUE;
        }

        return Access.FALSE;
    }

    public Access getFamilyDetailsAccess(PlayerFamily playerFamily, FamilyDetails details, TypeTargetComponent typeTargetComponent) {
        if (details == null || typeTargetComponent == null) {
            return null;
        }
        return details.getAccess(playerFamily, typeTargetComponent);
    }
    
    public boolean isFamilyDetailsAccess(PlayerFamily playerFamily, FamilyDetails details, TypeTargetComponent typeTargetComponent) {
        if (details == null || typeTargetComponent == null) {
            return true;
        }
        return details.hasAccess(playerFamily, typeTargetComponent);
    }

    public void setPermission(UUID uuid, Access access) {
        permissionsMap.put(uuid, access);
    }

    public boolean checkPermission(PlayerFamily playerFamily, FamilyDetails details, TypeTargetComponent typeTargetComponent) {
        Access access = getPermission(playerFamily, details, typeTargetComponent);
        return access == Access.TRUE;
    }

    public boolean isPermissionDeniedMap(UUID uuid) {
        Access access = permissionsMap.get(uuid);
        if (access == null) {
            return true;
        }
        return access == Access.FALSE;
    }

    public boolean isAllowAll() {
        return allowAll;
    }

    public void setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
    }

    public boolean isDenyAllExceptFamily() {
        return denyAllExceptFamily;
    }

    public void setDenyAllExceptFamily(boolean denyAllExceptFamily) {
        this.denyAllExceptFamily = denyAllExceptFamily;
    }

    public void removePermissionMap(UUID uuid) {
        permissionsMap.remove(uuid);
    }
}
