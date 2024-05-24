package ink.anh.family.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ink.anh.family.AnhyFamily;
import ink.anh.family.fplayer.Currency;
import ink.anh.family.fplayer.FamilyService;
import ink.anh.family.fplayer.Prices;

import java.math.BigInteger;

public class PaymentManager {

    private EconomyHandler economyHandler;
    private Prices prices;

    public PaymentManager(AnhyFamily familiPlugin) {
        this.economyHandler = familiPlugin.getEconomyHandler();
        this.prices = familiPlugin.getGlobalManager().getFamilyConfig().getPrices();
    }

    public boolean canAfford(Player player, FamilyService action) {
        BigInteger cost = prices.getAmountForService(action);
        ItemStack item = prices.getItemForService(action);

        if (prices.getCurrency() == Currency.VIRTUAL && economyHandler != null) {
            if (cost.equals(BigInteger.ZERO)) {
                return true; // Безкоштовна послуга для віртуальної валюти
            }
            double balance = economyHandler.getBalance(player);
            return balance >= cost.doubleValue();
        } else if (prices.getCurrency() == Currency.ITEM) {
            if (item == null || item.getType() == Material.AIR) {
                return true; // Безкоштовна послуга для предметної валюти
            }
            int count = getItemCountInInventory(player, item);
            return count >= item.getAmount();
        }
        return false;
    }

    public boolean makePayment(Player player, FamilyService action) {
        if (canAfford(player, action)) {
            BigInteger cost = prices.getAmountForService(action);
            ItemStack item = prices.getItemForService(action);

            if (prices.getCurrency() == Currency.VIRTUAL && economyHandler != null) {
                if (cost.equals(BigInteger.ZERO)) {
                    return true; // Оплата не потрібна для віртуальної валюти
                }
                return economyHandler.removeFunds(player, cost.doubleValue());
            } else if (prices.getCurrency() == Currency.ITEM) {
                if (item == null || item.getType() == Material.AIR) {
                    return true; // Оплата не потрібна для предметної валюти
                }
                return removeItemFromInventory(player, item);
            }
        }
        return false;
    }

    private int getItemCountInInventory(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack inventoryItem : player.getInventory().getContents()) {
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                count += inventoryItem.getAmount();
            }
        }
        return count;
    }

    private boolean removeItemFromInventory(Player player, ItemStack item) {
        int toRemove = item.getAmount();
        ItemStack[] items = player.getInventory().getContents();
        boolean successfullyRemoved = false;

        for (int i = 0; i < items.length; i++) {
            ItemStack inventoryItem = items[i];
            if (inventoryItem != null && inventoryItem.isSimilar(item)) {
                int amountInSlot = inventoryItem.getAmount();

                if (amountInSlot <= toRemove) {
                    player.getInventory().clear(i);
                    toRemove -= amountInSlot;
                } else {
                    inventoryItem.setAmount(amountInSlot - toRemove);
                    player.getInventory().setItem(i, inventoryItem);
                    toRemove = 0;
                }

                if (toRemove <= 0) {
                    successfullyRemoved = true;
                    break;
                }
            }
        }

        return successfullyRemoved;
    }

}
