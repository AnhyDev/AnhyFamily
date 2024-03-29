package ink.anh.family.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import ink.anh.family.AnhyFamily;

public class SQLiteDatabaseManager extends DatabaseManager {

	protected SQLiteDatabaseManager(AnhyFamily plugin) {
    	super(plugin);
    	
        initialize();
    }

    @Override
	public void initialize() {
        try {
            File dataFolder = new File(plugin.getDataFolder(), dbName + ".db");
            if (!dataFolder.exists()) {
                dataFolder.createNewFile();
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);

            initializeTables();
        } catch (IOException | ClassNotFoundException | SQLException e) {
            ErrorLogger.log(plugin, e, "Failed to initialize database");
        }
    }
    
    @Override
    public AbstractFamilyTable getFamilyTable() {
        return new SQLiteFamilyTable(this);
    }

    @Override
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                return DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/" + dbName + ".db");
            }
        } catch (SQLException e) {
            ErrorLogger.log(plugin, e, "Failed to get database connection");
        }
        return connection;
    }
}
