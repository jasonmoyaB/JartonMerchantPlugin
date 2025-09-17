package PluginsJason.commands;

import PluginsJason.economy.EconomyManager;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.util.*;

public class ShopCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    public ShopCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only allow players to execute this command
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;

        // Load the rotated_items.yml configuration file
        File file = new File(plugin.getDataFolder(), "rotated_items.yml");
        if (!file.exists()) {
            player.sendMessage("§crotated_items.yml not found.");
            return true;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Create the GUI with a custom title using glyphs
        String rawTitle = "&f";
        String translatedTitle = ChatColor.translateAlternateColorCodes('&', rawTitle);
        Inventory gui = Bukkit.createInventory(null, 54, translatedTitle); // Elevated GUI

        int[] itemSlots = {29, 31, 33}; // Centered row (row 4)
        int index = 0;

        // Load up to 3 items from the config
        for (int i = 1; i <= 3; i++) {
            if (index >= itemSlots.length) break;

            String path = "rotated.item" + i;
            if (!config.contains(path + ".material")) continue;

            Material material = Material.matchMaterial(config.getString(path + ".material"));
            if (material == null) continue;

            int amount = config.getInt(path + ".amount", 1);
            int price = config.getInt(path + ".price", 1);
            String commandToRun = config.getString(path + ".command");

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name", "Item")));
                meta.setLore(config.getStringList(path + ".lore"));

                if (config.contains(path + ".customModelData")) {
                    meta.setCustomModelData(config.getInt(path + ".customModelData"));
                }

                // Append price info to the lore
                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("§7Price: §6$" + price);
                meta.setLore(lore);

                item.setItemMeta(meta);
            }

            // Store the command as hidden metadata (optional)
            if (commandToRun != null) {
                item = addCommandTag(item, commandToRun);
            }

            gui.setItem(itemSlots[index], item);
            index++;
        }

        // Open the GUI and play a sound
        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DISAPPEARED, 1f, 1f);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        String expectedTitle = ChatColor.translateAlternateColorCodes('&', "&f");
        if (!event.getView().getTitle().equals(expectedTitle)) return;

        event.setCancelled(true); // Prevent item movement

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;

        // Extract price from lore
        int price = 0;
        for (String line : lore) {
            if (ChatColor.stripColor(line).toLowerCase().contains("price:")) {
                String[] parts = ChatColor.stripColor(line).split("\\$");
                try {
                    price = Integer.parseInt(parts[1].trim());
                } catch (Exception ignored) {}
                break;
            }
        }

        if (price <= 0) {
            player.sendMessage("§cThis item has an invalid price.");
            return;
        }

        Economy econ = EconomyManager.getEconomy();
        if (econ == null) {
            player.sendMessage("§cEconomy system is not available.");
            return;
        }

        // Check if player has enough money
        if (econ.getBalance(player) < price) {
            player.sendMessage("§cYou don't have enough balance. Price: §6$" + price);
            return;
        }

        // Withdraw money
        econ.withdrawPlayer(player, price);

        // Execute command or give item
        String commandToRun = getCommandTag(clickedItem);
        if (commandToRun != null) {
            String finalCommand = commandToRun.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        } else {
            player.getInventory().addItem(clickedItem.clone());
        }

        // Confirmation message and sound
        player.sendMessage("§aYou purchased the item for §6$" + price + " using Vault.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    // Adds a hidden command tag to the item using PersistentDataContainer
    private ItemStack addCommandTag(ItemStack item, String command) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "buyCommand"), PersistentDataType.STRING, command);
        item.setItemMeta(meta);
        return item;
    }

    // Retrieves the hidden command tag from the item
    private String getCommandTag(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "buyCommand"), PersistentDataType.STRING);
    }
}
