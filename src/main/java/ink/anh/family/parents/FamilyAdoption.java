package ink.anh.family.parents;

import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.common.PlayerFamily;
import ink.anh.family.gender.Gender;
import ink.anh.family.info.FamilyTree;
import ink.anh.family.util.FamilyUtils;

public class FamilyAdoption {

	private AnhyFamily familiPlugin;
	private GlobalManager globalManager;
	private boolean isNonTraditionalAdoptionAllowed;
	private String[] langs;
	
	public FamilyAdoption(AnhyFamily familiPlugin) {
		this.familiPlugin = familiPlugin;
		this.globalManager = familiPlugin.getGlobalManager();
		this.isNonTraditionalAdoptionAllowed = globalManager.getFamilyConfig().isNonBinaryAdopt();
		this.langs = new String[] {globalManager.getDefaultLang()};
    }
	
	public boolean adoption(PlayerFamily adopted, PlayerFamily adopter1, PlayerFamily adopter2) {
		
	    if (!canAdopt(adopted, adopter1, adopter2)) {
            Logger.warn(familiPlugin, adopter1.getRootrNickName() + ", " + adopter2.getRootrNickName() + Translator.translateKyeWorld(globalManager, "family_log_adoption_impossible_between", langs) + adopted.getLoverCaseName());
            return false;
	    }
	    
        Gender gender1 = adopter1.getGender();
        Gender gender2 = adopter2.getGender();

        // Логіка для встановлення батьківських зв'язків
        if (isNonTraditionalAdoptionAllowed && (gender1 == gender2 || !FamilyUtils.areGendersCompatibleForTraditional(adopter1) || !FamilyUtils.areGendersCompatibleForTraditional(adopter2))) {
            adopted.setFather(adopter1.getRoot());
            adopted.setMother(adopter2.getRoot());
        } else {
            // Для традиційного усиновлення
            if (gender1 == Gender.MALE) {
                adopted.setFather(adopter1.getRoot());
                adopted.setMother(adopter2.getRoot());
            } else if (gender1 == Gender.FEMALE) {
                adopted.setMother(adopter1.getRoot());
                adopted.setFather(adopter2.getRoot());
            } else if (gender2 == Gender.MALE) {
                adopted.setFather(adopter2.getRoot());
                adopted.setMother(adopter1.getRoot());
            } else if (gender2 == Gender.FEMALE) {
                adopted.setMother(adopter2.getRoot());
                adopted.setFather(adopter1.getRoot());
            }
        }

        // Додавання дитини до списку обох усиновлювачів
        adopter1.addChild(adopted.getRoot());
        adopter2.addChild(adopted.getRoot());

        // Збереження змін у базі даних
        FamilyUtils.saveFamily(adopter1);
        FamilyUtils.saveFamily(adopter2);
        FamilyUtils.saveFamily(adopted);

        Logger.info(familiPlugin, adopter1.getRootrNickName() + ", " + adopter2.getRootrNickName() + Translator.translateKyeWorld(globalManager, "family_log_adoption_successful_between", langs) + adopted.getLoverCaseName());
        return true; 
	}

	public boolean adoption(PlayerFamily adopted, PlayerFamily adopter1) {
		
	    if (!canAdopt(adopted, adopter1)) {
	    	String rawMessage = Translator.translateKyeWorld(globalManager, "family_log_adoption_impossible_single", langs);
	    	String message = StringUtils.formatString(rawMessage, new String[] {adopter1.getLoverCaseName(), adopted.getLoverCaseName()});
	    	Logger.warn(familiPlugin, message);
            return false;
	    }
	    
        Gender adopterGender = adopter1.getGender();

        if (adopterGender == Gender.MALE && adopted.getFather() == null) {
            adopted.setFather(adopter1.getRoot());
            adopter1.addChild(adopted.getRoot());
        } else if (adopterGender == Gender.FEMALE && adopted.getMother() == null) {
            adopted.setMother(adopter1.getRoot());
            adopter1.addChild(adopted.getRoot());
        } else if (isNonTraditionalAdoptionAllowed) {
            // Якщо небінарні усиновлення дозволені, вибираємо перший вільний слот
            if (adopted.getFather() == null) {
                adopted.setFather(adopter1.getRoot());
                adopter1.addChild(adopted.getRoot());
            } else if (adopted.getMother() == null) {
                adopted.setMother(adopter1.getRoot());
                adopter1.addChild(adopted.getRoot());
            }
        }

        FamilyUtils.saveFamily(adopter1);
        FamilyUtils.saveFamily(adopted);

    	String rawMessage = Translator.translateKyeWorld(globalManager, "family_log_adoption_successful_single", langs);
    	String message = StringUtils.formatString(rawMessage, new String[] {adopter1.getLoverCaseName(), adopted.getLoverCaseName()});
    	Logger.info(familiPlugin, message);
        return true;
    
	}

	public boolean canAdopt(PlayerFamily adopted, PlayerFamily adopter1, PlayerFamily adopter2) {
	    FamilyTree tree = new FamilyTree(adopted.getRoot());
	    return !FamilyUtils.hasRelatives(tree, adopter1.getRoot())
	            && !FamilyUtils.hasRelatives(tree, adopter2.getRoot())
	            && isGenderCompatibleAdoption(adopter1, adopter2)
	            && isParentSlotAvailable(adopted, false, null);
	}

	public boolean canAdopt(PlayerFamily adopted, PlayerFamily adopter) {
	    FamilyTree tree = new FamilyTree(adopted.getRoot());
	    return !FamilyUtils.hasRelatives(tree, adopter.getRoot())
	            && isGenderCompatibleAdoption(adopter)
	            && isParentSlotAvailable(adopted, true, adopter);
	}

	private boolean isGenderCompatibleAdoption(PlayerFamily adopter) {
        if (isNonTraditionalAdoptionAllowed) {
            return true; // Якщо одностатеві та небінарні усиновлення дозволені
        }

        return FamilyUtils.areGendersCompatibleForTraditional(adopter);
    }

	private boolean isGenderCompatibleAdoption(PlayerFamily adopter1, PlayerFamily adopter2) {
        if (isNonTraditionalAdoptionAllowed) {
            return true; // Якщо одностатеві та небінарні усиновлення дозволені
        }

        return FamilyUtils.areGendersCompatibleForTraditional(adopter1, adopter2);
    }

	private boolean isParentSlotAvailable(PlayerFamily adopted, boolean isSingleAdopter, PlayerFamily adopter) {
        if (!isSingleAdopter) {
            // Усиновлення парою: обидва слоти мають бути вільні
            return adopted.getFather() == null && adopted.getMother() == null;
        }
        
        if (isNonTraditionalAdoptionAllowed) {
            // Для небінарних випадків: хоча б один слот вільний
            return adopted.getFather() == null || adopted.getMother() == null;
        }
        
        // Усиновлення однією бінарною особою
        if (adopter == null) {
            return false; // Не можливо усиновити без вказівки усиновлювача
        }

        if (adopter.getGender() == Gender.MALE) {
            return adopted.getFather() == null; // Слот для батька вільний
        }

        if (adopter.getGender() == Gender.FEMALE) {
            return adopted.getMother() == null; // Слот для матері вільний
        }

        // непередбачувані варіанти
        return false;
    }
}
