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
	private AnhyFamily familiPlugin;

    private boolean useMySQL;
    private MySQLConfig mySQLConfig;
	
	private LanguageManager langManager;
	private FamilyConfig familyConfig;
	
    private String pluginName;
    private String defaultLang;
    private boolean debug;
	
	private GlobalManager(AnhyFamily familiPlugin) {
		super(familiPlugin);
		this.familiPlugin = familiPlugin;
		this.saveDefaultConfig();
		this.loadFields(familiPlugin);
	}

    public static synchronized GlobalManager getManager(AnhyFamily familiPlugin) {
        if (instance == null) {
            instance = new GlobalManager(familiPlugin);
        }
        return instance;
    }
    
	@Override
	public Plugin getPlugin() {
		return familiPlugin;
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
		Bukkit.getScheduler().runTaskAsynchronously(familiPlugin, () -> {
	        try {
	        	saveDefaultConfig();
	            familiPlugin.reloadConfig();
	            loadFields(familiPlugin);
	            familyConfig.reloadConfig(familiPlugin);
	            Logger.info(familiPlugin, Translator.translateKyeWorld(instance, "family_configuration_reloaded" , new String[] {defaultLang}));
	        } catch (Exception e) {
	            e.printStackTrace();
	            Logger.error(familiPlugin, Translator.translateKyeWorld(instance, "family_err_reloading_configuration ", new String[] {defaultLang}));
	        }
		});
        return true;
    }
    
    private void loadFields(AnhyFamily familiPlugin) {
        defaultLang = familiPlugin.getConfig().getString("language", "en");
        pluginName = ChatColor.translateAlternateColorCodes('&',familiPlugin.getConfig().getString("plugin_name", "AnhyFamily"));
        debug = familiPlugin.getConfig().getBoolean("debug", false);
        useMySQL = "MySQL".equalsIgnoreCase(familiPlugin.getConfig().getString("database.type"));
        
        setMySQLConfig();
        
        if (this.langManager == null) {
            this.langManager = LangMessage.getInstance(this);;
        } else {
        	this.langManager.reloadLanguages();
        }
        
        familyConfig = FamilyConfig.getInstance(familiPlugin);
    }

	private void setMySQLConfig() {
		this.mySQLConfig = new MySQLConfig(
				familiPlugin.getConfig().getString("database.mysql.host"),
				familiPlugin.getConfig().getInt("database.mysql.port"),
				familiPlugin.getConfig().getString("database.mysql.database"),
				familiPlugin.getConfig().getString("database.mysql.username"),
				familiPlugin.getConfig().getString("database.mysql.password"),
				familiPlugin.getConfig().getString("database.mysql.prefix"),
				familiPlugin.getConfig().getBoolean("database.mysql.useSSL"),
				familiPlugin.getConfig().getBoolean("database.mysql.autoReconnect")
	        );
	}

    private void saveDefaultConfig() {
        File dataFolder = familiPlugin.getDataFolder();
        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdirs();
            if (!created) {
                Logger.error(familiPlugin, "Could not create plugin directory: " + dataFolder.getPath());
                return;
            }
        }
        
    	File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
        	familiPlugin.getConfig().options().copyDefaults(true);
        	familiPlugin.saveDefaultConfig();
        }
    }
}
