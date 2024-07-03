package ink.anh.family;

import org.bukkit.plugin.java.JavaPlugin;

import ink.anh.family.command.CommandManager;
import ink.anh.family.listeners.ListenersRegistry;
import ink.anh.family.util.EconomyHandler;
import ink.anh.family.util.StringColorUtils;


public class AnhyFamily extends JavaPlugin {
	
    private static AnhyFamily instance;
    
    private EconomyHandler economyHandler;

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
        
        StringColorUtils.initialize(getDataFolder());
        
        if (checkClass("net.milkbowl.vault.Vault")) {
        	economyHandler = EconomyHandler.getInstance();
        }
        
        GlobalManager.getInstance();
        
        new ListenersRegistry(this).register();
        new CommandManager(this).registerCommands();
    }

    @Override
    public void onDisable() {
        if (GlobalManager.getInstance().getDatabaseManager() != null) {
        	GlobalManager.getInstance().getDatabaseManager().closeConnection();
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

    public EconomyHandler getEconomyHandler() {
        return economyHandler;
    }
}
