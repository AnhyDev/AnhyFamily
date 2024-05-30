package ink.anh.family.fplayer;

import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.util.FamilyUtils;

import java.util.UUID;

public class FamilySeparation {
    
    private AnhyFamily familiPlugin;
    private GlobalManager globalManager;
    private String[] langs;
    
    public FamilySeparation(AnhyFamily familiPlugin) {
        this.familiPlugin = familiPlugin;
        this.globalManager = GlobalManager.getInstance();
        this.langs = new String[] {globalManager.getDefaultLang()};
    }

    public boolean separateChildFromParent(UUID childId, UUID parentId) {
        PlayerFamily childFamily = FamilyUtils.getFamily(childId);
        PlayerFamily parentFamily = FamilyUtils.getFamily(parentId);

        if (childFamily == null || parentFamily == null) {
            Logger.warn(familiPlugin, Translator.translateKyeWorld(globalManager, "family_separation_child_not_found", langs));
            return false;
        }

        // Використання існуючого методу для видалення батьківських зв'язків
        FamilyUtils.removeChildFromParents(parentFamily, childFamily);

        Logger.info(familiPlugin, Translator.translateKyeWorld(globalManager, "family_separation_successful", langs));
        return true;
    }

    public boolean separateParentFromChild(UUID parentId, UUID childId) {
        PlayerFamily parentFamily = FamilyUtils.getFamily(parentId);
        PlayerFamily childFamily = FamilyUtils.getFamily(childId);

        if (parentFamily == null || childFamily == null) {
            Logger.warn(familiPlugin, Translator.translateKyeWorld(globalManager, "family_separation_parent_not_found", langs));
            return false;
        }

        // Використання існуючого методу для видалення батьківських зв'язків
        FamilyUtils.removeChildFromParents(parentFamily, childFamily);

        Logger.info(familiPlugin, Translator.translateKyeWorld(globalManager, "family_separation_successful", langs));
        return true;
    }

    public boolean separateSpouses(PlayerFamily spouse1Family) {

        if (spouse1Family == null) {
            Logger.warn(familiPlugin, Translator.translateKyeWorld(globalManager, "family_separation_spouse_not_found", langs));
            return false;
        }

        // Використання методу для розлучення подружжя лише один раз
        FamilyUtils.removeSpouseAndRestoreLastName(spouse1Family);

        // Логування успішного розлучення
        Logger.info(familiPlugin, Translator.translateKyeWorld(globalManager, "family_separation_successful", langs));
        return true;
    }
}
