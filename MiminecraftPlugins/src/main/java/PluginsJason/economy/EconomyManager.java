package PluginsJason.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyManager {

    private static Economy economy;

    public static boolean setupEconomy(JavaPlugin plugin) {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public static boolean hasEnough(Player player, double amount) {
        return economy != null && economy.getBalance(player) >= amount;
    }

    public static boolean withdraw(Player player, double amount) {
        if (economy == null) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public static double getBalance(Player player) {
        return economy != null ? economy.getBalance(player) : 0;
    }

    public static Economy getEconomy() {
        return economy;
    }
}
