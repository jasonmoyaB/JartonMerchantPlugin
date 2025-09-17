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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        File file = new File(plugin.getDataFolder(), "rotated_items.yml");
        if (!file.exists()) {
            player.sendMessage("§cNo se encontró rotated_items.yml.");
            return true;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String rawTitle = "&f";
        String translatedTitle = ChatColor.translateAlternateColorCodes('&', rawTitle);
        Inventory gui = Bukkit.createInventory(null, 54, translatedTitle); // GUI elevada

        int[] itemSlots = {29, 31, 33}; // Cuarta fila
        int index = 0;

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
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name", "Ítem")));
                meta.setLore(config.getStringList(path + ".lore"));

                if (config.contains(path + ".customModelData")) {
                    meta.setCustomModelData(config.getInt(path + ".customModelData"));
                }

                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("§eBuy now for §6" + price + " emeralds");
                meta.setLore(lore);

                item.setItemMeta(meta);
            }

            if (commandToRun != null) {
                item = addCommandTag(item, commandToRun);
            }

            gui.setItem(itemSlots[index], item);
            index++;
        }

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

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;

        int price = 0;
        for (String line : lore) {
            if (ChatColor.stripColor(line).toLowerCase().contains("buy now for")) {
                String[] parts = ChatColor.stripColor(line).split(" ");
                try {
                    price = Integer.parseInt(parts[parts.length - 2]);
                } catch (NumberFormatException ignored) {}
                break;
            }
        }

        if (price <= 0) {
            player.sendMessage("§cEste ítem no tiene precio válido.");
            return;
        }

        Economy econ = EconomyManager.getEconomy();
        if (econ == null) {
            player.sendMessage("§cSistema de economía no disponible.");
            return;
        }

        if (econ.getBalance(player) < price) {
            player.sendMessage("§cNo tienes suficiente dinero. Precio: §4" + price);
            return;
        }

        econ.withdrawPlayer(player, price);

        String commandToRun = getCommandTag(clickedItem);
        if (commandToRun != null) {
            String finalCommand = commandToRun.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        } else {
            player.getInventory().addItem(clickedItem.clone());
        }

        player.sendMessage("§aHas comprado el ítem por §6" + price + " esmeraldas.");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    private ItemStack addCommandTag(ItemStack item, String command) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "buyCommand"), PersistentDataType.STRING, command);
        item.setItemMeta(meta);
        return item;
    }

    private String getCommandTag(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "buyCommand"), PersistentDataType.STRING);
    }
}
