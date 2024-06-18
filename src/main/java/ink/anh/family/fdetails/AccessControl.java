package ink.anh.family.fdetails;

import ink.anh.api.enums.Access;

public class AccessControl {
    private Access homeAccess;
    private Access chestAccess;
    private Access chatAccess;
    private Access hugsAccess;

    public AccessControl(Access homeAccess, Access chestAccess, Access chatAccess, Access hugsAccess) {
        this.homeAccess = homeAccess != null ? homeAccess : Access.DEFAULT;
        this.chestAccess = chestAccess != null ? chestAccess : Access.DEFAULT;
        this.chatAccess = chatAccess != null ? chatAccess : Access.DEFAULT;
        this.hugsAccess = hugsAccess != null ? hugsAccess : Access.DEFAULT;
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

	public Access getHugsAccess() {
		return hugsAccess;
	}

	public void setHugsAccess(Access hugsAccess) {
		this.hugsAccess = hugsAccess;
	}
}
