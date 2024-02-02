package ink.anh.family.common;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

import ink.anh.api.items.ItemStackSerializer;
import ink.anh.family.AnhyFamily;

import java.io.File;
import java.io.IOException;

public class ItemPriceUpdater {

    private AnhyFamily plugin;

    public ItemPriceUpdater(AnhyFamily plugin) {
        this.plugin = plugin;
    }

    public boolean updateItemPrice(Player player, String key) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Перевірка чи предмет у руці гравця не є повітрям (пустим слотом)
        if (itemInHand == null || itemInHand.getType().isAir()) {
            return false;
        }

        String serializedItem = ItemStackSerializer.serializeItemStackToYaml(itemInHand);
        File itemPricesFile = new File(plugin.getDataFolder(), "item_prices.yml");

        // Створення файлу, якщо він не існує
        if (!itemPricesFile.exists()) {
            try {
                itemPricesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        FileConfiguration itemPricesConfig = YamlConfiguration.loadConfiguration(itemPricesFile);

        // Запис серіалізованого предмету в файл
        itemPricesConfig.set(key, serializedItem);
        try {
            itemPricesConfig.save(itemPricesFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

