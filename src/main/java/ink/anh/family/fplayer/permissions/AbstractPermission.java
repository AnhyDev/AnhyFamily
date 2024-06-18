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

    protected PlayerFamily playerFamily;
    
    public AbstractPermission(PlayerFamily playerFamily) {
        this.playerFamily = playerFamily;
    }

    public abstract ActionsPermissions getActionsPermissions();

    public Access getPermission(FamilyDetails details, TypeTargetComponent typeTargetComponent) {
        if (allowAll) {
            return Access.TRUE;
        }

        boolean detailsAccess = isFamilyDetailsAccess(details, typeTargetComponent);

        if (denyAllExceptFamily && !detailsAccess) {
            return Access.FALSE;
        }

        Access access = permissionsMap.get(playerFamily.getRoot());
        return access != null ? access : (detailsAccess ? Access.TRUE : Access.FALSE);
    }

    public boolean isFamilyDetailsAccess(FamilyDetails details, TypeTargetComponent typeTargetComponent) {
        if (details == null || typeTargetComponent == null) {
            return true;
        }
        return details.hasAccess(playerFamily, typeTargetComponent);
    }

    public void setPermission(UUID uuid, Access access) {
        permissionsMap.put(uuid, access);
    }

    public boolean checkPermission(FamilyDetails details, TypeTargetComponent typeTargetComponent) {
        Access access = getPermission(details, typeTargetComponent);
        return access == Access.TRUE;
    }

    public boolean isPermissionDeniedMap(UUID uuid) {
        Access access = permissionsMap.get(uuid);
        if (access == null) {
            return true;
        }
        return access == Access.FALSE;
    }

    protected boolean isAllowAll() {
        return allowAll;
    }

    protected void setAllowAll(boolean allowAll) {
        this.allowAll = allowAll;
    }

    protected boolean isDenyAllExceptFamily() {
        return denyAllExceptFamily;
    }

    protected void setDenyAllExceptFamily(boolean denyAllExceptFamily) {
        this.denyAllExceptFamily = denyAllExceptFamily;
    }

    public void removePermissionMap(UUID uuid) {
        permissionsMap.remove(uuid);
    }
}
