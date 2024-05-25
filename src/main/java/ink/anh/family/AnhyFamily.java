package ink.anh.family;

import org.bukkit.plugin.java.JavaPlugin;

import ink.anh.family.command.CommandManager;
import ink.anh.family.listeners.ListenersRegistratar;
import ink.anh.family.marry.MarriageManager;
import ink.anh.family.parents.ParentManager;
import ink.anh.family.util.EconomyHandler;


public class AnhyFamily extends JavaPlugin {
	
    private static AnhyFamily instance;

    private GlobalManager manager;
    
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
        
        if (checkClass("net.milkbowl.vault.Vault")) {
        	economyHandler = EconomyHandler.getInstance();
        }
        
        manager = GlobalManager.getManager(this);
        manager.setDatabaseManager();
        manager.getDatabaseManager().initialize();
        manager.getDatabaseManager().initializeTables();
        
        marriageManager = MarriageManager.getInstance(this);
        parentManager = ParentManager.getInstance(this);
        
        new ListenersRegistratar(this).register();
        new CommandManager(this).registerCommands();
    }

    @Override
    public void onDisable() {
        if (manager.getDatabaseManager() != null) {
        	manager.getDatabaseManager().closeConnection();
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
