package ink.anh.family.db;

import java.sql.Connection;
import java.sql.SQLException;

import ink.anh.family.AnhyFamily;

public abstract class DatabaseManager {

    protected AnhyFamily plugin;
    protected Connection connection;
    protected String dbName = "family";

    public DatabaseManager(AnhyFamily plugin) {
        this.plugin = plugin;
    }

    public abstract void initialize();
    
    public abstract AbstractFamilyTable getFamilyTable();

    public abstract Connection getConnection();

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
