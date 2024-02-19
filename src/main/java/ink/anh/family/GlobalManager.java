package ink.anh.family;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import ink.anh.api.LibraryManager;
import ink.anh.api.lingo.Translator;
import ink.anh.api.lingo.lang.LanguageManager;
import ink.anh.api.messages.Logger;
import ink.anh.family.common.FamilyConfig;
import ink.anh.family.db.MySQLConfig;
import ink.anh.family.lang.LangMessage;
import net.md_5.bungee.api.ChatColor;

public class GlobalManager extends LibraryManager {

    private static GlobalManager instance;
	private AnhyFamily familyPlugin;

    private boolean useMySQL;
    private MySQLConfig mySQLConfig;
	
	private LanguageManager langManager;
	private FamilyConfig familyConfig;
	
    private String pluginName;
    private String defaultLang;
    private boolean debug;
	
	private GlobalManager(AnhyFamily familyPlugin) {
		super(familyPlugin);
		this.familyPlugin = familyPlugin;
		this.saveDefaultConfig();
		this.loadFields(familyPlugin);
	}

    public static synchronized GlobalManager getManager(AnhyFamily familyPlugin) {
        if (instance == null) {
            instance = new GlobalManager(familyPlugin);
        }
        return instance;
    }
    
	@Override
	public Plugin getPlugin() {
		return familyPlugin;
	}

	@Override
	public String getPluginName() {
		return pluginName;
	}

	@Override
	public LanguageManager getLanguageManager() {
		return this.langManager;
	}

	@Override
	public String getDefaultLang() {
		return defaultLang;
	}

	@Override
	public boolean isDebug() {
		return debug;
	}

	public boolean isUseMySQL() {
		return useMySQL;
	}

	public MySQLConfig getMySQLConfig() {
		return mySQLConfig;
	}

	public FamilyConfig getFamilyConfig() {
		return familyConfig;
	}

	public boolean reload() {
		Bukkit.getScheduler().runTaskAsynchronously(familyPlugin, () -> {
	        try {
	        	saveDefaultConfig();
	            familyPlugin.reloadConfig();
	            loadFields(familyPlugin);
	            familyConfig.reloadConfig(familyPlugin);
	            Logger.info(familyPlugin, Translator.translateKyeWorld(instance, "family_configuration_reloaded" , new String[] {defaultLang}));
	        } catch (Exception e) {
	            e.printStackTrace();
	            Logger.error(familyPlugin, Translator.translateKyeWorld(instance, "family_err_reloading_configuration ", new String[] {defaultLang}));
	        }
		});
        return true;
    }
    
    private void loadFields(AnhyFamily familyPlugin) {
        defaultLang = familyPlugin.getConfig().getString("language", "en");
        pluginName = ChatColor.translateAlternateColorCodes('&',familyPlugin.getConfig().getString("plugin_name", "AnhyFamily"));
        debug = familyPlugin.getConfig().getBoolean("debug", false);
        useMySQL = "MySQL".equalsIgnoreCase(familyPlugin.getConfig().getString("database.type"));
        
        setMySQLConfig();
        
        if (this.langManager == null) {
            this.langManager = LangMessage.getInstance(this);;
        } else {
        	this.langManager.reloadLanguages();
        }
        
        familyConfig = FamilyConfig.getInstance(familyPlugin);
    }

	private void setMySQLConfig() {
		this.mySQLConfig = new MySQLConfig(
				familyPlugin.getConfig().getString("database.mysql.host"),
				familyPlugin.getConfig().getInt("database.mysql.port"),
				familyPlugin.getConfig().getString("database.mysql.database"),
				familyPlugin.getConfig().getString("database.mysql.username"),
				familyPlugin.getConfig().getString("database.mysql.password"),
				familyPlugin.getConfig().getString("database.mysql.prefix"),
				familyPlugin.getConfig().getBoolean("database.mysql.useSSL"),
				familyPlugin.getConfig().getBoolean("database.mysql.autoReconnect")
	        );
	}

    private void saveDefaultConfig() {
        File dataFolder = familyPlugin.getDataFolder();
        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdirs();
            if (!created) {
                Logger.error(familyPlugin, "Could not create plugin directory: " + dataFolder.getPath());
                return;
            }
        }
        
    	File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
        	familyPlugin.getConfig().options().copyDefaults(true);
        	familyPlugin.saveDefaultConfig();
        }
    }
}
