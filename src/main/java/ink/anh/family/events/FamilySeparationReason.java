package ink.anh.family.events;

import ink.anh.api.lingo.Translator;
import ink.anh.family.GlobalManager;

public enum FamilySeparationReason {
    DIVORCE("family_separation_divorce"),
    DIVORCE_RELATIVE("family_separation_divorce_relative"),
    DISOWN_CHILD("family_separation_disown_child"),
    DISOWN_PARENT("family_separation_disown_parent"),
    FULL_SEPARATION("family_separation_full_separation");

    private final String key;

    FamilySeparationReason(String description) {
        this.key = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription(String[] langs) {
        return Translator.translateKyeWorld(GlobalManager.getInstance(), key, langs);
    }
}
