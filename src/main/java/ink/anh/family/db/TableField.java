package ink.anh.family.db;

public class TableField<T> {

    private T key;
    private String fieldName;
    private String fieldValue;

    public TableField(T key, String fieldName, String fieldValue) {
        this.key = key;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}
