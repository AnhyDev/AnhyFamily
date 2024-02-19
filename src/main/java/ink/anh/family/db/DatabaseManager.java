package ink.anh.family.db;

import java.sql.Connection;
import java.sql.SQLException;

import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

public abstract class DatabaseManager {
    private static DatabaseManager instance;

    protected AnhyFamily plugin;
    protected Connection connection;
    protected String dbName = "family";

    public static DatabaseManager getInstance(AnhyFamily plugin) {
        if (instance == null) {
        	GlobalManager manager = plugin.getGlobalManager();
            if (manager.isUseMySQL()) {
                instance = new MySQLDatabaseManager(plugin, manager.getMySQLConfig());
            } else {
                instance = new SQLiteDatabaseManager(plugin);
            }
        }
        return instance;
    }

    protected DatabaseManager(AnhyFamily plugin) {
        this.plugin = plugin;
    }

    public abstract void initialize();

    public abstract Connection getConnection();

    public abstract AbstractFamilyTable getFamilyTable();
    
    // Метод для перезавантаження конфігурації та бази даних
    public static void reload(AnhyFamily plugin) {
        if (instance != null) {
            instance.closeConnection(); // Закриваємо існуюче з'єднання перед переініціалізацією
        }
        instance = null; // Скидаємо інстанс для створення нового з оновленою конфігурацією
        getInstance(plugin); // Створюємо новий інстанс з оновленою конфігурацією
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            ErrorLogger.log(plugin, e, "Failed to close database connection");
        }
    }
    
    public void initializeTables() {
    	getFamilyTable().initialize();
    }
}
