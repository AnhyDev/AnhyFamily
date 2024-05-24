package ink.anh.family.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

public abstract class DatabaseManager {
    private static DatabaseManager instance;

    public final AnhyFamily plugin;
    protected Connection connection;
    private Map<Class<?>, AbstractTable<?>> tables = new ConcurrentHashMap<>();

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
    
    public abstract String getTablePrefix();

    public <T> void registerTable(Class<T> clazz, AbstractTable<T> table) {
        tables.put(clazz, table);
    }

    @SuppressWarnings("unchecked")
    public <T> AbstractTable<T> getTable(Class<T> clazz) {
        return (AbstractTable<T>) tables.get(clazz);
    }

    public static void reload(AnhyFamily plugin) {
        if (instance != null) {
            instance.closeConnection();
        }
        instance = null;
        getInstance(plugin);
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
        TableRegistry.registerAllTables(this);
        tables.values().forEach(AbstractTable::initialize);
    }
}
