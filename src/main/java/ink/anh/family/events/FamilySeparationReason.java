package ink.anh.family.events;

public enum FamilySeparationReason {
    DIVORCE("Divorce"),
    DISOWN_CHILD("Disowning a child"),
    DISOWN_PARENT("Disowning a parent"),
    FULL_SEPARATION("Full separation of all family relations");

    private final String description;

    FamilySeparationReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
