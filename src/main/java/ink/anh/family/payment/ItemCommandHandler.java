package ink.anh.family.payment;

import ink.anh.family.AnhyFamily;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class ItemCommandHandler {

    private final AnhyFamily plugin;
    private final ItemPriceUpdater itemPriceUpdater;

    public ItemCommandHandler(AnhyFamily plugin) {
        this.plugin = plugin;
        this.itemPriceUpdater = new ItemPriceUpdater(plugin);
    }

    public boolean handleAdd(Player player, String key) {
        boolean success = itemPriceUpdater.updateItemPrice(player, key);

        if (success) {
            player.sendMessage("Предмет успішно додано/перезаписано за ключем: " + key);
        } else {
            player.sendMessage("Не вдалося додати/перезаписати предмет. Переконайся, що ти тримаєш предмет у руці.");
        }

        return true;
    }

    public boolean handleClear(Player player) {
        File itemPricesFile = new File(plugin.getDataFolder(), "item_prices.yml");
        if (itemPricesFile.exists() && itemPricesFile.delete()) {
            player.sendMessage("Файл item_prices.yml успішно очищено.");
            return true;
        } else {
            player.sendMessage("Не вдалося очистити файл item_prices.yml.");
            return false;
        }
    }

    public boolean handleGet(Player player, String key) {
        File itemPricesFile = new File(plugin.getDataFolder(), "item_prices.yml");
        FileConfiguration itemPricesConfig = YamlConfiguration.loadConfiguration(itemPricesFile);

        if (itemPricesConfig.contains(key)) {
            String serializedItem = itemPricesConfig.getString(key);
            player.sendMessage("Значення для ключа " + key + ": " + serializedItem);
        } else {
            player.sendMessage("Ключ " + key + " не знайдено.");
        }

        return true;
    }

    public boolean handleRemove(Player player, String key) {
        File itemPricesFile = new File(plugin.getDataFolder(), "item_prices.yml");
        FileConfiguration itemPricesConfig = YamlConfiguration.loadConfiguration(itemPricesFile);

        if (itemPricesConfig.contains(key)) {
            itemPricesConfig.set(key, null);
            try {
                itemPricesConfig.save(itemPricesFile);
                player.sendMessage("Ключ " + key + " успішно видалено.");
            } catch (IOException e) {
                e.printStackTrace();
                player.sendMessage("Не вдалося зберегти файл після видалення ключа " + key + ".");
                return false;
            }
        } else {
            player.sendMessage("Ключ " + key + " не знайдено.");
        }

        return true;
    }
}
