package ink.anh.family.payment;

import ink.anh.api.items.ItemStackSerializer;
import ink.anh.api.messages.MessageForFormatting;
import ink.anh.api.messages.MessageType;
import ink.anh.api.messages.Sender;
import ink.anh.family.AnhyFamily;
import ink.anh.family.GlobalManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;

public class ItemCommandHandler extends Sender {

    private final AnhyFamily plugin;
    private final ItemPriceUpdater itemPriceUpdater;

    public ItemCommandHandler(AnhyFamily plugin) {
        super(GlobalManager.getInstance());
        this.plugin = plugin;
        this.itemPriceUpdater = new ItemPriceUpdater(plugin);
    }

    public boolean handleAdd(Player player, String key) {
        boolean success = itemPriceUpdater.updateItemPrice(player, key);

        if (success) {
            sendMessage(new MessageForFormatting("family_item_add_success", new String[]{key}), MessageType.NORMAL, player);
        } else {
            sendMessage(new MessageForFormatting("family_item_add_failure", new String[]{}), MessageType.WARNING, player);
        }

        return true;
    }

    public boolean handleClear(Player player) {
        File itemPricesFile = new File(plugin.getDataFolder(), "item_prices.yml");
        if (itemPricesFile.exists() && itemPricesFile.delete()) {
            sendMessage(new MessageForFormatting("family_item_clear_success", new String[]{}), MessageType.NORMAL, player);
            return true;
        } else {
            sendMessage(new MessageForFormatting("family_item_clear_failure", new String[]{}), MessageType.WARNING, player);
            return false;
        }
    }

    public boolean handleGet(Player player, String key) {
        File itemPricesFile = new File(plugin.getDataFolder(), "item_prices.yml");
        FileConfiguration itemPricesConfig = YamlConfiguration.loadConfiguration(itemPricesFile);

        if (itemPricesConfig.contains(key)) {
            String serializedItem = itemPricesConfig.getString(key);
            ItemStack item = ItemStackSerializer.deserializeItemStack(serializedItem);

            if (item == null) {
                sendMessage(new MessageForFormatting("family_item_get_failure", new String[]{key}), MessageType.WARNING, player);
                return false;
            }

            PlayerInventory inventory = player.getInventory();
            if (inventory.firstEmpty() != -1) {
                inventory.addItem(item);
            } else {
                player.getWorld().dropItem(player.getLocation(), item);
            }

            sendMessage(new MessageForFormatting("family_item_get_success", new String[]{key}), MessageType.NORMAL, player);
        } else {
            sendMessage(new MessageForFormatting("family_item_get_failure", new String[]{key}), MessageType.WARNING, player);
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
                sendMessage(new MessageForFormatting("family_item_remove_success", new String[]{key}), MessageType.NORMAL, player);
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage(new MessageForFormatting("family_item_remove_save_failure", new String[]{key}), MessageType.WARNING, player);
                return false;
            }
        } else {
            sendMessage(new MessageForFormatting("family_item_remove_failure", new String[]{key}), MessageType.WARNING, player);
        }

        return true;
    }
}
