package ink.anh.family.fplayer.permissions;


public class HugsPermission extends AbstractPermission {

	private final ActionsPermissions actionsPermissions = ActionsPermissions.HUGS_TO_ALL_PLAYERS;

	@Override
	public ActionsPermissions getActionsPermissions() {
		return actionsPermissions;
	}
}
