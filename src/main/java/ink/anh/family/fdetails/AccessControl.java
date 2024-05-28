package ink.anh.family.fdetails;

import ink.anh.api.enums.Access;

public class AccessControl {
    private Access homeAccess;
    private Access chestAccess;
    private Access chatAccess;

    public AccessControl(Access homeAccess, Access chestAccess, Access chatAccess) {
        this.homeAccess = homeAccess;
        this.chestAccess = chestAccess;
        this.chatAccess = chatAccess;
    }

    public Access getHomeAccess() {
        return homeAccess;
    }

    public void setHomeAccess(Access homeAccess) {
        this.homeAccess = homeAccess;
    }

    public Access getChestAccess() {
        return chestAccess;
    }

    public void setChestAccess(Access chestAccess) {
        this.chestAccess = chestAccess;
    }

    public Access getChatAccess() {
        return chatAccess;
    }

    public void setChatAccess(Access chatAccess) {
        this.chatAccess = chatAccess;
    }
}
