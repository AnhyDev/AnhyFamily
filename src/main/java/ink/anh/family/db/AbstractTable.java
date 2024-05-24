package ink.anh.family.db;

import ink.anh.family.AnhyFamily;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractTable<T> {
	
    protected DatabaseManager dbManager;
    protected String dbName;

    public AbstractTable(AnhyFamily familyPlugin, String dbName) {
        this.dbManager = familyPlugin.getDatabaseManager();
        this.dbName = dbManager.getTablePrefix() + dbName;
    }

    protected abstract void initialize();

    public abstract void insert(T entity);

    public abstract void update(T entity);

    public abstract <K> void updateField(TableField<K> tableField);

    public abstract void delete(T entity);

    public static String joinOrReturnNull(String[] elements) {
        if (elements == null || elements.length == 0) {
            return null;
        }

        boolean allNull = true;
        for (String element : elements) {
            if (element != null && !element.equalsIgnoreCase("null")) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (String element : elements) {
            if (element != null && !element.equalsIgnoreCase("null")) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(element);
            }
        }

        return result.length() > 0 ? result.toString() : null;
    }

    public static String[] splitStringAndNullify(String input, String delimiter) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String[] parts = input.split(delimiter);
        for (int i = 0; i < parts.length; i++) {
            if ("null".equalsIgnoreCase(parts[i])) {
                parts[i] = null;
            }
        }
        return parts;
    }

    @FunctionalInterface
    public interface SQLConsumer<T> {
        void accept(T t) throws SQLException;
    }

    protected void executeTransaction(SQLConsumer<Connection> sqlConsumer, String errorMessage) {
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false); // Початок транзакції

            try {
                sqlConsumer.accept(conn);
                conn.commit(); // Завершення транзакції
            } catch (SQLException e) {
                conn.rollback(); // Відкат транзакції у випадку помилки
                ErrorLogger.log(dbManager.plugin, e, errorMessage);
            }
        } catch (SQLException e) {
            ErrorLogger.log(dbManager.plugin, e, "Failed to establish database connection");
        }
    }
}
