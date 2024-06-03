package ink.anh.family.db.fdetails;

public enum FamilyDetailsField {
    FAMILY_SYMBOL("family_symbol", "TEXT"),
    HOME_LOCATION("home_location", "TEXT"),
    FAMILY_CHEST("family_chest", "TEXT"),
    CHILDREN_ACCESS("children_access", "TEXT"),
    ANCESTORS_ACCESS("ancestors_access", "TEXT"),
    CHILDREN_ACCESS_MAP("children_access_map", "TEXT"),
    ANCESTORS_ACCESS_MAP("ancestors_access_map", "TEXT"),
    HOME_SET_DATE("home_set_date", "TEXT");

    private final String fieldName;
    private final String fieldType;

    FamilyDetailsField(String fieldName, String fieldType) {
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
        for (FamilyDetailsField field : FamilyDetailsField.values()) {
            if (field.getFieldName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    public static String getTableCreate() {
        StringBuilder sb = new StringBuilder(" (");
        sb.append("family_id VARCHAR(36) PRIMARY KEY,");
        for (FamilyDetailsField field : FamilyDetailsField.values()) {
            sb.append(field.getFieldName()).append(" ").append(field.getFieldType()).append(",");
        }
        sb.setLength(sb.length() - 1); // Видалити останню кому
        sb.append(");");
        return sb.toString();
    }

    public static String getTableInsert() {
        StringBuilder sbFields = new StringBuilder(" (family_id,");
        StringBuilder sbValues = new StringBuilder(" VALUES (?,");
        for (FamilyDetailsField field : FamilyDetailsField.values()) {
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
        for (FamilyDetailsField field : FamilyDetailsField.values()) {
            sb.append(field.getFieldName()).append(" = VALUES(").append(field.getFieldName()).append("), ");
        }
        sb.setLength(sb.length() - 2); // Видалити останню кому і пробіл
        sb.append(";");
        return sb.toString();
    }
}
