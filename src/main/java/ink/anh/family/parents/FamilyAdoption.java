package ink.anh.family.parents;

import ink.anh.api.lingo.Translator;
import ink.anh.api.messages.Logger;
import ink.anh.api.utils.StringUtils;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;
import ink.anh.family.db.fplayer.FamilyPlayerField;
import ink.anh.family.fplayer.PlayerFamily;
import ink.anh.family.fplayer.PlayerFamilyDBService;
import ink.anh.family.fplayer.gender.Gender;
import ink.anh.family.fplayer.info.TreeStringGenerator;
import ink.anh.family.util.FamilyUtils;

public class FamilyAdoption {

    private AnhyFamily familiPlugin;
    private GlobalManager globalManager;
    private boolean isNonTraditionalAdoptionAllowed;
    private String[] langs;

    public FamilyAdoption(AnhyFamily familiPlugin) {
        this.familiPlugin = familiPlugin;
        this.globalManager = GlobalManager.getInstance();
        this.isNonTraditionalAdoptionAllowed = globalManager.getFamilyConfig().isNonBinaryAdopt();
        this.langs = new String[]{globalManager.getDefaultLang()};
    }

    public boolean adoption(PlayerFamily adopted, PlayerFamily... adopters) {
        if (adopters.length == 1) {
            return singleAdoption(adopted, adopters[0]);
        } else {
            return coupleAdoption(adopted, adopters[0], adopters[1]);
        }
    }

    private boolean singleAdoption(PlayerFamily adopted, PlayerFamily adopter) {
        if (!canAdopt(adopted, adopter)) {
            String rawMessage = Translator.translateKyeWorld(globalManager, "family_log_adoption_impossible_single", langs);
            String message = StringUtils.formatString(rawMessage, new String[]{adopter.getLoverCaseName(), adopted.getLoverCaseName()});
            Logger.warn(familiPlugin, message);
            return false;
        }

        Gender adopterGender = adopter.getGender();

        if (adopterGender == Gender.MALE && adopted.getFather() == null) {
            adopted.setFather(adopter.getRoot());
            adopter.addChild(adopted.getRoot());
        } else if (adopterGender == Gender.FEMALE && adopted.getMother() == null) {
            adopted.setMother(adopter.getRoot());
            adopter.addChild(adopted.getRoot());
        } else if (isNonTraditionalAdoptionAllowed) {
            if (adopted.getFather() == null) {
                adopted.setFather(adopter.getRoot());
                adopter.addChild(adopted.getRoot());
            } else if (adopted.getMother() == null) {
                adopted.setMother(adopter.getRoot());
                adopter.addChild(adopted.getRoot());
            }
        }

        PlayerFamilyDBService.savePlayerFamily(adopter, FamilyPlayerField.CHILDREN);
        PlayerFamilyDBService.savePlayerFamily(adopted, null);

        String rawMessage = Translator.translateKyeWorld(globalManager, "family_log_adoption_successful_single", langs);
        String message = StringUtils.formatString(rawMessage, new String[]{adopter.getRootrNickName(), adopted.getRootrNickName()});
        Logger.info(familiPlugin, message);
        return true;
    }

    private boolean coupleAdoption(PlayerFamily adopted, PlayerFamily adopter1, PlayerFamily adopter2) {
        if (!canAdopt(adopted, adopter1, adopter2)) {
            return false;
        }

        Gender gender1 = adopter1.getGender();
        Gender gender2 = adopter2.getGender();

        if (isNonTraditionalAdoptionAllowed && (gender1 == gender2 || !FamilyUtils.areGendersCompatibleForTraditional(adopter1) || !FamilyUtils.areGendersCompatibleForTraditional(adopter2))) {
            adopted.setFather(adopter1.getRoot());
            adopted.setMother(adopter2.getRoot());
        } else {
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

        adopter1.addChild(adopted.getRoot());
        adopter2.addChild(adopted.getRoot());

        PlayerFamilyDBService.savePlayerFamily(adopter1, FamilyPlayerField.CHILDREN);
        PlayerFamilyDBService.savePlayerFamily(adopter2, FamilyPlayerField.CHILDREN);
        PlayerFamilyDBService.savePlayerFamily(adopted, null);

        Logger.info(familiPlugin, adopter1.getRootrNickName() + ", " + adopter2.getRootrNickName() + Translator.translateKyeWorld(globalManager, "family_log_adoption_successful_between", langs) + adopted.getRootrNickName());
        return true;
    }

    public boolean canAdopt(PlayerFamily adopted, PlayerFamily adopter1, PlayerFamily adopter2) {
        TreeStringGenerator tree = new TreeStringGenerator(adopted.getRoot());
        return !FamilyUtils.hasRelatives(tree, adopter1)
                && !FamilyUtils.hasRelatives(tree, adopter2)
                && isGenderCompatibleAdoption(adopter1, adopter2)
                && isParentSlotAvailable(adopted, false, null);
    }

    public boolean canAdopt(PlayerFamily adopted, PlayerFamily adopter) {
        TreeStringGenerator tree = new TreeStringGenerator(adopted.getRoot());
        return !FamilyUtils.hasRelatives(tree, adopter)
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
            return adopted.getFather() == null && adopted.getMother() == null;
        }

        if (isNonTraditionalAdoptionAllowed) {
            return adopted.getFather() == null || adopted.getMother() == null;
        }

        if (adopter == null) {
            return false;
        }

        if (adopter.getGender() == Gender.MALE) {
            return adopted.getFather() == null;
        }

        if (adopter.getGender() == Gender.FEMALE) {
            return adopted.getMother() == null;
        }

        return false;
    }
}
