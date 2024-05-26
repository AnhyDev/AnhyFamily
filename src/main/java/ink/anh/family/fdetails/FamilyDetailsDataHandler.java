package ink.anh.family.fdetails;

import ink.anh.api.DataHandler;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FamilyDetailsDataHandler extends DataHandler {
    
    private Map<UUID, FamilyDetails> localDataMap = new ConcurrentHashMap<>();

    private static final String FAMILY_ID = "familyId";
    private static final String FAMILY_FATHER = "familyFather";
    private static final String FAMILY_MOTHER = "familyMother";
    private static final String FAMILY_CHILDREN = "familyChildren";
    
    private static FamilyDetailsDataHandler instance;

    // Приватний конструктор для запобігання створенню нових екземплярів
    private FamilyDetailsDataHandler() {
        super();
    }

    // Метод для отримання єдиного екземпляра класу
    public static synchronized FamilyDetailsDataHandler getInstance() {
        if (instance == null) {
            instance = new FamilyDetailsDataHandler();
        }
        return instance;
    }

    // Метод для додавання FamilyDetails до локальної мапи
    public void addFamilyDetails(FamilyDetails familyDetails) {
    	UUID familyId = familyDetails.getFamilyId();
        localDataMap.put(familyId, familyDetails);
    }

    // Метод для отримання FamilyDetails з локальної мапи
    public FamilyDetails getFamilyDetails(UUID familyId) {
        return localDataMap.get(familyId);
    }

    // Метод для видалення FamilyDetails з локальної мапи
    public void removeFamilyDetails(UUID familyId) {
        localDataMap.remove(familyId);
    }

    // Метод для перевірки наявності FamilyDetails в локальній мапі
    public boolean hasFamilyDetails(UUID familyId) {
        return getFamilyDetails(familyId) != null;
    }

    // Метод для додавання кореневих даних до глобальної мапи
    public void addRootDetails(UUID playerId, FamilyDetails familyDetails) {
        addData(playerId, FAMILY_ID, familyDetails);
    }

    // Метод для отримання кореневих даних з глобальної мапи
    public FamilyDetails getRootDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_ID);
        if (data != null && data instanceof FamilyDetails) {
            return (FamilyDetails) data;
        }
        return null;
    }

    // Метод для видалення кореневих даних з глобальної мапи
    public void removeRootDetails(UUID playerId) {
        removeData(playerId, FAMILY_ID);
    }

    // Метод для перевірки наявності кореневих даних в глобальній мапі
    public boolean hasRootDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_ID);
        return data != null && data instanceof FamilyDetails;
    }

    // Метод для додавання даних батька до глобальної мапи
    public void addFatherDetails(UUID playerId, FamilyDetails fatherDetails) {
        addData(playerId, FAMILY_FATHER, fatherDetails);
    }

    // Метод для отримання даних батька з глобальної мапи
    public FamilyDetails getFatherDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_FATHER);
        if (data != null && data instanceof FamilyDetails) {
            return (FamilyDetails) data;
        }
        return null;
    }

    // Метод для видалення даних батька з глобальної мапи
    public void removeFatherDetails(UUID playerId) {
        removeData(playerId, FAMILY_FATHER);
    }

    // Метод для перевірки наявності даних батька в глобальній мапі
    public boolean hasFatherDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_FATHER);
        return data != null && data instanceof FamilyDetails;
    }

    // Метод для додавання даних матері до глобальної мапи
    public void addMotherDetails(UUID playerId, FamilyDetails motherDetails) {
        addData(playerId, FAMILY_MOTHER, motherDetails);
    }

    // Метод для отримання даних матері з глобальної мапи
    public FamilyDetails getMotherDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_MOTHER);
        if (data != null && data instanceof FamilyDetails) {
            return (FamilyDetails) data;
        }
        return null;
    }

    // Метод для видалення даних матері з глобальної мапи
    public void removeMotherDetails(UUID playerId) {
        removeData(playerId, FAMILY_MOTHER);
    }

    // Метод для перевірки наявності даних матері в глобальній мапі
    public boolean hasMotherDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_MOTHER);
        return data != null && data instanceof FamilyDetails;
    }

    // Метод для додавання даних дітей до глобальної мапи
    public void addChildrenDetails(UUID playerId, List<FamilyDetails> childrenDetails) {
        addData(playerId, FAMILY_CHILDREN, childrenDetails);
    }

    // Метод для отримання даних дітей з глобальної мапи
    @SuppressWarnings("unchecked")
    public List<FamilyDetails> getChildrenDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_CHILDREN);
        if (data instanceof List) {
            return (List<FamilyDetails>) data;
        }
        return null;
    }

    // Метод для видалення даних дітей з глобальної мапи
    public void removeChildrenDetails(UUID playerId) {
        removeData(playerId, FAMILY_CHILDREN);
    }

    // Метод для перевірки наявності даних дітей в глобальній мапі
    public boolean hasChildrenDetails(UUID playerId) {
        Object data = getData(playerId, FAMILY_CHILDREN);
        return data != null && data instanceof List;
    }
}
