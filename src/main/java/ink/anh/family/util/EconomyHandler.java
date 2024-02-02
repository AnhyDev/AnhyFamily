package ink.anh.family.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.Plugin;
import net.milkbowl.vault.economy.Economy;

public class EconomyHandler {

    private static EconomyHandler instance = null;
    private Economy econ = null;

    private EconomyHandler() {
        setupEconomy();
    }

    public static EconomyHandler getInstance() {
        if (instance == null) {
            instance = new EconomyHandler();
        }
        return instance;
    }

    private boolean setupEconomy() {
        Plugin vaultPlugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        if (vaultPlugin == null || !vaultPlugin.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() {
        return econ;
    }

    public double getBalance(Player player) {
        if (econ != null) {
            return econ.getBalance(player);
        }
        return 0.0;
    }

    public boolean addFunds(Player player, double amount) {
        if (econ != null) {
            econ.depositPlayer(player, amount);
            return true;
        }
        return false;
    }

    public boolean removeFunds(Player player, double amount) {
        if (econ != null && econ.has(player, amount)) {
            econ.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }
}
