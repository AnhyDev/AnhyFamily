package ink.anh.family.db;

import java.util.UUID;

import ink.anh.family.common.Family;

public abstract class AbstractFamilyTable {
    
    protected DatabaseManager dbManager;
    protected String dbName;

    public AbstractFamilyTable(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.dbName = dbManager.dbName;
        initialize();
    }

    protected abstract void initialize();

    public abstract void insertFamily(Family family);
    
    public Family getFamily(UUID playerUUID, String displayName) {

        Family family = null;
        
        if (playerUUID != null) {
        	family = getFamily(playerUUID);
        }
        
        if (family == null && displayName != null) {
        	family = getFamilyByDisplayName(displayName);
        }
        
        if (family != null) {
        	if (!family.getRoot().equals(playerUUID)) {
        		family.setRoot(playerUUID);
        	}
        }
        
        return family;
    }
    
    public abstract Family getFamily(UUID playerUUID);
    public abstract Family getFamilyByDisplayName(String displayName);
    public abstract void deleteFamily(UUID playerUUID);
    public abstract void updateFamilyField(UUID playerUUID, String fieldName, String fieldValue);
    
    public static String joinOrReturnNull(String[] elements) {
        if (elements == null || elements.length == 0) {
            return null; // Якщо масив порожній або null
        }

        // Перевіряємо, чи всі елементи є null або "null"
        boolean allNull = true;
        for (String element : elements) {
            if (element != null && !element.equalsIgnoreCase("null")) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            return null; // Повертаємо null, якщо всі елементи null або "null"
        }

        // Формуємо рядок з елементів, що не є null або "null", розділяючи їх комою
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null && !elements[i].equalsIgnoreCase("null")) {
                if (result.length() > 0) {
                    result.append(","); // Додаємо роздільник, якщо це не перший доданий елемент
                }
                result.append(elements[i]);
            }
        }

        return result.length() > 0 ? result.toString() : null; // Повертаємо сформований рядок або null, якщо жоден елемент не додано
    }

    public static String[] splitStringAndNullify(String input, String delimiter) {
        // Перевірка на null або порожній вхідний рядок
        if (input == null || input.isEmpty()) {
            return null;
        }

        // Розділення рядка на масив за допомогою заданого роздільника
        String[] parts = input.split(delimiter);

        // Перебір елементів масиву та заміна "null" на null
        for (int i = 0; i < parts.length; i++) {
            if ("null".equalsIgnoreCase(parts[i])) {
                parts[i] = null; // Заміна "null" на null
            }
        }
        return parts; // Повернення модифікованого масиву
    }
}
