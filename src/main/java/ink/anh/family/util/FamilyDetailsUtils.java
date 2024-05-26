package ink.anh.family.util;

import ink.anh.family.AnhyFamily;
import ink.anh.family.db.fdetails.AbstractFamilyDetailsTable;
import ink.anh.family.fdetails.FamilyDetails;
import ink.anh.family.fdetails.FamilyDetailsDataHandler;
import ink.anh.family.fplayer.PlayerFamily;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FamilyDetailsUtils {

    private static AbstractFamilyDetailsTable familyDetailsTable = (AbstractFamilyDetailsTable) AnhyFamily.getInstance().getGlobalManager().getDatabaseManager().getTable(FamilyDetails.class);
    private static FamilyDetailsDataHandler dataHandler = FamilyDetailsDataHandler.getInstance();

    public static void saveFamilyDetails(FamilyDetails familyDetails) {
        familyDetailsTable.insert(familyDetails);
    }

    public static FamilyDetails getFamilyDetails(UUID familyId) {
        // Спочатку перевіряємо локальний кеш
        FamilyDetails familyDetails = dataHandler.getFamilyDetails(familyId);
        if (familyDetails != null) {
            return familyDetails;
        }

        // Якщо в кеші немає, завантажуємо з бази даних
        familyDetails = familyDetailsTable.getFamilyDetails(familyId);
        if (familyDetails != null) {
            // Додаємо в локальний кеш для подальшого використання
            dataHandler.addFamilyDetails(familyId, familyDetails);
        }

        return familyDetails;
    }

    public static FamilyDetails getRootFamilyDetails(UUID rootId) {
        FamilyDetails rootDetails = dataHandler.getRootDetails(rootId);
        if (rootDetails != null) {
            return rootDetails;
        }

        // Отримання PlayerFamily гравця
        PlayerFamily playerFamily = FamilyUtils.getFamily(rootId);
        if (playerFamily == null) {
            return null;
        }
        
        UUID familyId = playerFamily.getFamilyId();
        if (familyId == null) {
            return null;
        }

        rootDetails = getFamilyDetails(familyId);
        if (rootDetails != null) {
            dataHandler.addRootDetails(rootId, rootDetails);
        }

        return rootDetails;
    }

    public static FamilyDetails getFatherFamilyDetails(UUID rootId) {
        // Перевірка наявності даних батька у глобальній мапі
        FamilyDetails fatherDetails = dataHandler.getFatherDetails(rootId);
        if (fatherDetails != null) {
            return fatherDetails;
        }

        // Отримання PlayerFamily гравця
        PlayerFamily playerFamily = FamilyUtils.getFamily(rootId);
        if (playerFamily == null) {
            return null;
        }

        // Отримання UUID батька з PlayerFamily
        UUID fatherId = playerFamily.getFather();
        if (fatherId == null) {
            return null;
        }

        // Отримання FamilyDetails батька
        fatherDetails = getRootFamilyDetails(fatherId);
        if (fatherDetails != null) {
            // Додавання FamilyDetails батька у глобальну мапу
            dataHandler.addFatherDetails(rootId, fatherDetails);
        }

        return fatherDetails;
    }

    public static FamilyDetails getMotherFamilyDetails(UUID rootId) {
        // Перевірка наявності даних матері у глобальній мапі
        FamilyDetails motherDetails = dataHandler.getMotherDetails(rootId);
        if (motherDetails != null) {
            return motherDetails;
        }

        // Отримання PlayerFamily гравця
        PlayerFamily playerFamily = FamilyUtils.getFamily(rootId);
        if (playerFamily == null) {
            return null;
        }

        // Отримання UUID матері з PlayerFamily
        UUID motherId = playerFamily.getMother();
        if (motherId == null) {
            return null;
        }

        // Отримання FamilyDetails матері
        motherDetails = getRootFamilyDetails(motherId);
        if (motherDetails != null) {
            // Додавання FamilyDetails матері у глобальну мапу
            dataHandler.addMotherDetails(rootId, motherDetails);
        }

        return motherDetails;
    }

    public static List<FamilyDetails> getChildrenFamilyDetails(UUID rootId) {
        // Перевірка наявності даних дітей у глобальній мапі
        List<FamilyDetails> childrenDetails = dataHandler.getChildrenDetails(rootId);
        if (childrenDetails != null && !childrenDetails.isEmpty()) {
            return childrenDetails;
        }

        // Отримання PlayerFamily гравця
        PlayerFamily playerFamily = FamilyUtils.getFamily(rootId);
        if (playerFamily == null) {
            return null;
        }

        // Отримання UUID дітей з PlayerFamily
        List<FamilyDetails> loadedChildrenDetails = new ArrayList<>();
        for (UUID childId : playerFamily.getChildren()) {
            FamilyDetails childDetails = getRootFamilyDetails(childId);
            if (childDetails != null) {
                loadedChildrenDetails.add(childDetails);
            }
        }

        if (!loadedChildrenDetails.isEmpty()) {
            // Додавання FamilyDetails дітей у глобальну мапу
            dataHandler.addChildrenDetails(rootId, loadedChildrenDetails);
        }

        return loadedChildrenDetails;
    }
}
