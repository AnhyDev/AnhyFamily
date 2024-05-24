package ink.anh.family;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import ink.anh.api.items.ItemStackSerializer;
import ink.anh.family.fplayer.Currency;
import ink.anh.family.fplayer.Prices;

public class FamilyConfig {

    private Prices prices;
    private Map<String, Boolean> answers;
    
    private String marriedSymbol;
    private String marriedColor;
    private String heartSymbol;
    private String heartColor;
    
    private boolean nonBinary;
    private boolean nonBinaryAdopt;
	private boolean nonBinaryMarry;
	
	private int ceremonyRadius;
	private int ceremonyHearingRadius;

    private static FamilyConfig instance;

    private FamilyConfig(AnhyFamily plugin) {
        loadConfig(plugin);
    }

    public static FamilyConfig getInstance(AnhyFamily plugin) {
        if (instance == null) {
            instance = new FamilyConfig(plugin);
        }
        return instance;
    }

    public void reloadConfig(AnhyFamily plugin) {
        loadConfig(plugin);
    }

    private void loadConfig(AnhyFamily plugin) {
        FileConfiguration config = plugin.getConfig();

        this.answers = parseAnswers(config.getConfigurationSection("answers"));
        
        this.marriedSymbol = config.getString("marriedSymbol", "⚭");
        this.marriedColor = config.getString("marriedColor", "&6");
        this.heartSymbol = config.getString("heartSymbol", "❤");
        this.heartColor = config.getString("heartColor", "&5");
        
        this.nonBinary = config.getBoolean("non_binary", false);
        this.nonBinaryAdopt = config.getBoolean("non_binary_adoption", false);
        this.nonBinaryMarry = config.getBoolean("non_binary_marriage", false);
        
        this.ceremonyRadius = config.getInt("ceremonyRadius", 20);
        this.ceremonyHearingRadius = config.getInt("ceremonyHearingRadius", 100);

        ItemStack[] items = loadItems(plugin);
        
        Currency currency = Currency.valueOf(config.getString("prices.currency", "ITEM"));
        if (plugin.getEconomyHandler() == null && currency == Currency.VIRTUAL) {
        	currency = Currency.ITEM;
        }
        
        this.prices = new Prices(
        	currency,
            BigInteger.valueOf(config.getLong("prices.marriage", 0)),
            items[0],
            BigInteger.valueOf(config.getLong("prices.divorce", 0)),
            items[1],
            BigInteger.valueOf(config.getLong("prices.adoption", 0)),
            items[2]
        );
    }

    // Getters
    public Prices getPrices() {
        return prices;
    }

    public Map<String, Boolean> getAnswers() {
        return answers;
    }

    public String getMarriedSymbol() {
        return marriedSymbol;
    }

    public String getMarriedColor() {
        return marriedColor;
    }

    public String getHeartSymbol() {
        return heartSymbol;
    }

    public String getHeartColor() {
        return heartColor;
    }

    public boolean isNonBinary() {
		return nonBinary;
	}

	public boolean isNonBinaryAdopt() {
		return nonBinaryAdopt;
	}

	public boolean isNonBinaryMarry() {
		return nonBinaryMarry;
	}

	
    public int getCeremonyRadius() {
		return ceremonyRadius;
	}

	public int getCeremonyHearingRadius() {
		return ceremonyHearingRadius;
	}

	// Method to check the answer
    public int checkAnswer(String word) {
        if (answers.containsKey(word)) {
            return answers.get(word) ? 1 : 2;
        }
        return 0;
    }

    // Helper methods
    private static Map<String, Boolean> parseAnswers(ConfigurationSection answersConfig) {
        Map<String, Boolean> answers = new HashMap<>();
        if (answersConfig != null) {
            for (String key : answersConfig.getKeys(false)) {
                List<?> list = answersConfig.getList(key);
                if (list != null) {
                    for (Object obj : list) {
                        if (obj instanceof String) {
                            answers.put((String) obj, "yes".equals(key));
                        }
                    }
                }
            }
        }
        return answers;
    }

    private static ItemStack[] loadItems(AnhyFamily plugin) {
        ItemStack[] items = new ItemStack[3];
        ItemStack defaultStack = new ItemStack(Material.AIR);
        File itemPricesFile = new File(plugin.getDataFolder(), "item_prices.yml");
        FileConfiguration itemPricesConfig = itemPricesFile.exists() ? YamlConfiguration.loadConfiguration(itemPricesFile) : null;

        String[] keys = new String[]{"marriage", "divorce", "adoption"};
        for (int i = 0; i < keys.length; i++) {
            if (itemPricesConfig != null) {
                String serializedItem = itemPricesConfig.getString(keys[i]);
                if (serializedItem != null) {
                    ItemStack deserializedItem = ItemStackSerializer.deserializeItemStackFromYaml(serializedItem);
                    items[i] = deserializedItem != null ? deserializedItem : defaultStack;
                } else {
                    items[i] = defaultStack;
                }
            } else {
                items[i] = defaultStack;
            }
        }

        return items;
    }
}
