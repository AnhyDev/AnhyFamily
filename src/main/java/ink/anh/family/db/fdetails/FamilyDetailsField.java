package ink.anh.family.db.fdetails;

public enum FamilyDetailsField {
    HOME_LOCATION("home_location", "TEXT"),
    FAMILY_CHEST("family_chest", "TEXT"),
    CHILDREN_ACCESS_HOME("children_access_home", "BOOLEAN"),
    CHILDREN_ACCESS_CHEST("children_access_chest", "BOOLEAN"),
    ANCESTORS_ACCESS_HOME("ancestors_access_home", "BOOLEAN"),
    ANCESTORS_ACCESS_CHEST("ancestors_access_chest", "BOOLEAN"),
    SPECIFIC_ACCESS_MAP("specific_access_map", "TEXT"),
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
}
