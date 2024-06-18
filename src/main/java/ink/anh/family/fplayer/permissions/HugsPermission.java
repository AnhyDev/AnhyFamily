package ink.anh.family.fplayer.permissions;

import ink.anh.family.fplayer.PlayerFamily;


public class HugsPermission extends AbstractPermission {

    public HugsPermission(PlayerFamily playerFamily) {
		super(playerFamily);
	}

	@Override
	public ActionsPermissions getActionsPermissions() {
		return ActionsPermissions.HUGS_TO_ALL_PLAYERS;
	}
}
