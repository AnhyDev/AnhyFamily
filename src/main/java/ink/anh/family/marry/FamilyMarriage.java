package ink.anh.family.marry;

import ink.anh.api.messages.Logger;
import ink.anh.family.AnhyFamily;
import ink.anh.family.common.Family;
import ink.anh.family.info.FamilyTree;
import ink.anh.family.util.FamilyUtils;

public class FamilyMarriage {

	private AnhyFamily familiPlugin;
	private boolean isNonBinaryMarriageAllowed;
	
	public FamilyMarriage(AnhyFamily familiPlugin) {
		this.familiPlugin = familiPlugin;
		this.isNonBinaryMarriageAllowed = familiPlugin.getGlobalManager().getFamilyConfig().isNonBinaryMarry();
    }
    
	public boolean marry(Family family1, Family family2) {
	    // Перевіряємо, чи можливе одруження
	    if (!canMarry(family1, family2)) {
		    Logger.warn(familiPlugin, "Одруження не можливе між " + family1.getDisplayName() + ", " + family2.getDisplayName());
	        return false;
	    }

	    // Встановлюємо зв'язки між сім'ями
	    family1.setSpouse(family2.getRoot());
	    family2.setSpouse(family1.getRoot());

	    // Оновлення інформації у базі даних
	    FamilyUtils.saveFamily(family1);
	    FamilyUtils.saveFamily(family2);

	    Logger.info(familiPlugin, "Одруження відбулося між " + family1.getDisplayName() + ", " + family2.getDisplayName());
	    return true;
	}

    public boolean canMarry(Family family1, Family family2) {
    	FamilyTree tree = new FamilyTree(family2.getRoot());
        return orNotMarried(family1, family2) && !FamilyUtils.hasRelatives(tree, family1.getRoot())
                && isGenderCompatibleForMarriage(family1, family2);
    }
    
    private boolean orNotMarried(Family family1, Family family2) {
    	return family1.getSpouse() == null && family2.getSpouse() == null;
    }

    private boolean isGenderCompatibleForMarriage(Family family1, Family family2) {
        if (isNonBinaryMarriageAllowed) {
            return true; // Дозволяється усі типи шлюбів
        }
        return FamilyUtils.areGendersCompatibleForTraditional(family1, family2); // Традиційний шлюб між чоловіком і жінкою
    }
}
