package ink.anh.family.db.fplayer;

public enum FamilyPlayerField {
    PLAYER_UUID("player_uuid", "VARCHAR(36) PRIMARY KEY"),
    GENDER("gender", "VARCHAR(36)"),
    DISPLAY_NAME("display_name", "VARCHAR(255) NOT NULL UNIQUE"),
    FIRST_NAME("first_name", "VARCHAR(255)"),
    LAST_NAME("last_name", "TEXT"),
    OLD_LAST_NAME("old_last_name", "TEXT"),
    FATHER("father", "VARCHAR(36)"),
    MOTHER("mother", "VARCHAR(36)"),
    SPOUSE("spouse", "VARCHAR(36)"),
    CHILDREN("children", "TEXT"),
    FAMILY_ID("family_id", "VARCHAR(36)"),
    PERMISSIONS_MAP("permissions_map", "TEXT");

    private final String fieldName;
    private final String fieldType;

    FamilyPlayerField(String fieldName, String fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public static boolean contains(String fieldName) {
        for (FamilyPlayerField field : FamilyPlayerField.values()) {
            if (field.getFieldName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static String getTableCreate() {
        StringBuilder sb = new StringBuilder(" (");
        for (FamilyPlayerField field : FamilyPlayerField.values()) {
            sb.append(field.getFieldName()).append(" ").append(field.getFieldType()).append(",");
        }
        sb.setLength(sb.length() - 1); // Видалити останню кому
        sb.append(");");
        return sb.toString();
    }

    public static String getTableInsert() {
        StringBuilder sbFields = new StringBuilder(" (");
        StringBuilder sbValues = new StringBuilder(" VALUES (");
        for (FamilyPlayerField field : FamilyPlayerField.values()) {
            sbFields.append(field.getFieldName()).append(",");
            sbValues.append("?,");
        }
        sbFields.setLength(sbFields.length() - 1); // Видалити останню кому
        sbValues.setLength(sbValues.length() - 1); // Видалити останню кому
        sbFields.append(")");
        sbValues.append(")");
        return sbFields.toString() + sbValues.toString();
    }

    public static String getUpdateFields() {
        StringBuilder sb = new StringBuilder();
        for (FamilyPlayerField field : FamilyPlayerField.values()) {
            if (!field.fieldName.equals("player_uuid")) { // виключити первинний ключ
                sb.append(field.getFieldName()).append(" = VALUES(").append(field.getFieldName()).append("), ");
            }
        }
        sb.setLength(sb.length() - 2); // Видалити останню кому і пробіл
        sb.append(";");
        return sb.toString();
    }
}
