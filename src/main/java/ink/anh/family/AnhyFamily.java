package ink.anh.family;

import org.bukkit.plugin.java.JavaPlugin;

import ink.anh.family.db.DatabaseManager;
import ink.anh.family.db.MySQLDatabaseManager;
import ink.anh.family.db.SQLiteDatabaseManager;
import ink.anh.family.marry.MarriageManager;
import ink.anh.family.parents.ParentManager;
import ink.anh.family.util.EconomyHandler;


public class AnhyFamily extends JavaPlugin {
	
    // Singleton instance of AnhyFamily
    private static AnhyFamily instance;

    // Instance of GlobalManager to manage plugin-wide operations
    private GlobalManager manager;
    private DatabaseManager dbManager;
    
    private EconomyHandler economyHandler;
    private MarriageManager marriageManager;
    private ParentManager parentManager;

    @Override
    public void onLoad() {
    	if (!checkClass("ink.anh.api.LibraryManager")) {
            getLogger().severe("AnhyLibAPI library not found. The AnhyFamily plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
    	}
    }
    
    @Override
    public void onEnable() {
        instance = this;
        manager = GlobalManager.getManager(this);

        // Визначення, яку базу даних використовувати
        if (manager.isUseMySQL()) {
            // Ініціалізація MySQL
            dbManager = new MySQLDatabaseManager(this, manager.getMySQLConfig());
        } else {
            // Ініціалізація SQLite
            dbManager = new SQLiteDatabaseManager(this);
        }

        // Ініціалізація таблиць бази даних
        dbManager.initialize();
        
        if (checkClass("net.milkbowl.vault.Vault")) {
        	economyHandler = EconomyHandler.getInstance();
        }
        marriageManager = MarriageManager.getInstance(this);
        parentManager = ParentManager.getInstance(this);
    }

    @Override
    public void onDisable() {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }
    
    public boolean checkClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            getLogger().severe(className + " not found.");
            return false;
        }
    }

    /**
     * Gets the singleton instance of AnhyFamily.
     * 
     * @return The singleton instance of AnhyFamily.
     */
    public static AnhyFamily getInstance() {
        return instance;
    }

    /**
     * Gets the instance of GlobalManager.
     * 
     * @return The instance of GlobalManager used for managing plugin-wide operations.
     */
    public GlobalManager getGlobalManager() {
        return manager;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }

	public MarriageManager getMarriageManager() {
		return marriageManager;
	}

	public ParentManager getParentManager() {
		return parentManager;
	}
}
