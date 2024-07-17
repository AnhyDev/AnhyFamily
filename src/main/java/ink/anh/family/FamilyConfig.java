package ink.anh.family;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import ink.anh.api.items.ItemStackSerializer;
import ink.anh.family.payment.Currency;
import ink.anh.family.payment.Prices;

public class FamilyConfig {

    private Prices prices;
    
    private String marriedSymbol;
    private String marriedColor;
    private String heartSymbol;
    private String heartColor;
    
    private boolean nonBinary;
    private boolean nonBinaryAdopt;
    private boolean nonBinaryMarry;
    
    private int ceremonyRadius;
    private int ceremonyHearingRadius;
    private Location privateCeremonyLocation;

    // Timeout before changing the family home point, specify in minutes
    private int homeChangeTimeoutMinutes;
    // It is allowed to teleport to the family home only in the world where it is installed
    private boolean homeWorld;
    
    // Allow the command (/fchest) to open the chest
    private boolean chestCommand;
    // Distance for the team (0 - no limits)
    private int chestDistance;
    // If without limitations, then only in this world
    private boolean chestWorld;
    // Allow chests to be opened by clicking
    private boolean chestClick;
    // List of blocks that can be used as a chest
    private List<Material> chestBlocks;
    //
    private int chestDistanceToHome;

    // Add a new field for languages limitation regex
    private String languagesLimitation;

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
        
        this.marriedSymbol = config.getString("marriedSymbol", "⚭");
        this.marriedColor = config.getString("marriedColor", "&6");
        this.heartSymbol = config.getString("heartSymbol", "❤");
        this.heartColor = config.getString("heartColor", "&5");
        
        this.nonBinary = config.getBoolean("non_binary", false);
        this.nonBinaryAdopt = config.getBoolean("non_binary_adoption", false);
        this.nonBinaryMarry = config.getBoolean("non_binary_marriage", false);
        
        this.ceremonyRadius = config.getInt("ceremonyRadius", 20);
        this.ceremonyHearingRadius = config.getInt("ceremonyHearingRadius", 100);
        setPrivateCeremonyLocationFromConfig(config);
        
        this.homeChangeTimeoutMinutes = config.getInt("home.timeout", 1440);
        this.homeWorld = config.getBoolean("home.world", false);

        this.chestCommand = config.getBoolean("chest.command", true);
        this.chestDistance = config.getInt("chest.distance", 0);
        this.chestWorld = config.getBoolean("chest.world", false);
        this.chestClick = config.getBoolean("chest.click", true);
        // Завантаження дозволених блоків для скринь
        loadChestBlocks(config);
        this.chestDistanceToHome = config.getInt("chest.distance_to_home", 20);

        // Зчитування regex для обмеження мов
        this.languagesLimitation = config.getString("languages_limitation", "^(?!.*[-']{2})(?!.*--)(?!.*'')\\p{L}['-]*[\\p{L}]+$");
        
        Currency currency = Currency.valueOf(config.getString("prices.currency", "ITEM"));
        if (plugin.getEconomyHandler() == null && currency == Currency.VIRTUAL) {
            currency = Currency.ITEM;
        }

        ItemStack[] items = loadItems(plugin);
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

    public int getHomeChangeTimeoutMinutes() {
        return homeChangeTimeoutMinutes;
    }

    public boolean isChestCommand() {
        return chestCommand;
    }

    public int getChestDistance() {
        return chestDistance;
    }

    public boolean isChestWorld() {
        return chestWorld;
    }

    public boolean isChestClick() {
        return chestClick;
    }

	public boolean isHomeWorld() {
		return homeWorld;
	}

	public void setHomeWorld(boolean homeWorld) {
		this.homeWorld = homeWorld;
	}

	public Location getPrivateCeremonyLocation() {
		return privateCeremonyLocation;
	}

	public List<Material> getChestBlocks() {
	    return chestBlocks;
	}

	public int getChestDistanceToHome() {
	    return chestDistanceToHome;
	}

    public String getLanguagesLimitation() {
        return languagesLimitation;
    }

	public void setPrivateCeremonyLocationFromConfig(FileConfiguration config) {
	    // Зчитування параметрів локації для приватного одруження
	    String worldName = config.getString("privateCeremony.world");
	    int x = config.getInt("privateCeremony.x");
	    int y = config.getInt("privateCeremony.y");
	    int z = config.getInt("privateCeremony.z");

	    World world = (worldName != null) ? Bukkit.getWorld(worldName) : null;
	    
	    if (world == null) {
	        world = Bukkit.getWorlds().get(0); // Головний світ сервера
	    }
	    
	    this.privateCeremonyLocation = new Location(world, x, y, z);
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
    
    private void loadChestBlocks(FileConfiguration config) {
        List<String> materialNames = config.getStringList("chest.material");
        this.chestBlocks = new ArrayList<>();
        
        for (String name : materialNames) {
            Material material = Material.matchMaterial(name.toUpperCase());
            if (material != null) {
                this.chestBlocks.add(material);
            }
        }
    }
}
