package PluginsJason.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class AncientTravelerGUI implements Listener {

    private final JavaPlugin plugin;

    public AncientTravelerGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openFor(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Ancient Traveler");

        // Ícono decorativo con Unicode del resource pack
        ItemStack icon = new ItemStack(Material.PAPER);
        ItemMeta iconMeta = icon.getItemMeta();
        if (iconMeta != null) {
            iconMeta.setDisplayName("");
            iconMeta.setLore(Collections.singletonList("§7Ícono decorativo del menú"));
            icon.setItemMeta(iconMeta);
        }
        gui.setItem(4, icon); // Slot decorativo (centro de la primera fila)

        // Cargar ítems rotativos desde YAML
        File file = new File(plugin.getDataFolder(), "rotated_items.yml");
        if (!file.exists()) {
            player.sendMessage("§cNo se encontró rotated_items.yml.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("rotated")) {
            player.sendMessage("§cNo hay ítems rotativos definidos.");
            return;
        }

        for (String key : config.getConfigurationSection("rotated").getKeys(false)) {
            String path = "rotated." + key;

            Material material = Material.matchMaterial(config.getString(path + ".material"));
            if (material == null) continue;

            int amount = config.getInt(path + ".amount", 1);
            int slot = config.getInt(path + ".slot", 0);
            int price = config.getInt(path + ".price", 1);

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name", "Ítem")));
                meta.setLore(config.getStringList(path + ".lore"));
                item.setItemMeta(meta);
            }

            // Agregar precio en lore
            ItemStack displayItem = item.clone();
            ItemMeta displayMeta = displayItem.getItemMeta();
            if (displayMeta != null) {
                List<String> lore = displayMeta.getLore() != null ? new ArrayList<>(displayMeta.getLore()) : new ArrayList<>();
                lore.add("§aPrecio: §2" + price + " esmeraldas");
                displayMeta.setLore(lore);
                displayItem.setItemMeta(displayMeta);
            }

            gui.setItem(slot, displayItem);
        }

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().contains("Ancient Traveler")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        // Verificar precio desde lore
        int price = 0;
        List<String> lore = clicked.getItemMeta().getLore();
        if (lore != null) {
            for (String line : lore) {
                if (line.contains("Precio:")) {
                    try {
                        price = Integer.parseInt(ChatColor.stripColor(line).replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Verificar esmeraldas
        int emeralds = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                emeralds += item.getAmount();
            }
        }

        if (emeralds < price) {
            player.sendMessage("§cNo tenés suficientes esmeraldas.");
            return;
        }

        // Retirar esmeraldas
        int remaining = price;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.EMERALD) {
                int amt = item.getAmount();
                if (amt <= remaining) {
                    player.getInventory().remove(item);
                    remaining -= amt;
                } else {
                    item.setAmount(amt - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }

        // Entregar ítem
        ItemStack reward = clicked.clone();
        reward.setAmount(1);
        player.getInventory().addItem(reward);
        player.sendMessage("§a¡Compra exitosa!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }
}
