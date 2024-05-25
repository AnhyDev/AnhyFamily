package ink.anh.family.events;

public enum ActionInitiator {
	
    PLAYER_SELF("Player for self"),
    PLAYER_WITH_PERMISSION("Player with permission for another"),
    CONSOLE("Console"),
    EXTERNAL("External (plugins, scripts)");

    private final String description;

    ActionInitiator(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
