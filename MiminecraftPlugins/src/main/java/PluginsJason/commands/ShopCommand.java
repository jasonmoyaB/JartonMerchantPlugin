package PluginsJason.commands;

import PluginsJason.economy.EconomyManager;
import PluginsJason.rotation.ShopRotator;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
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
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }

        File file = new File(plugin.getDataFolder(), "rotated_items.yml");
        if (!file.exists()) {
            player.sendMessage("§crotated_items.yml not found.");
            return true;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String rawTitle = "&f";
        String translatedTitle = ChatColor.translateAlternateColorCodes('&', rawTitle);
        Inventory gui = Bukkit.createInventory(null, 45, translatedTitle);

        long now = System.currentTimeMillis();
        long nextRotation = ShopRotator.getLastRotationTime() + (1000L * 60 * 60 * 24);
        long remainingMillis = nextRotation - now;
        long hours = (remainingMillis / (1000 * 60 * 60)) % 24;
        long minutes = (remainingMillis / (1000 * 60)) % 60;
        String timeFormatted = String.format("%02dh %02dm", hours, minutes);

        ItemStack travelerInfo = new ItemStack(Material.STICK);
        ItemMeta travelerMeta = travelerInfo.getItemMeta();

        if (travelerMeta != null) {
            travelerMeta.setCustomModelData(11);
            travelerMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6Ancient Traveler"));

            List<String> lore = Arrays.asList(
                    "",
                    ChatColor.translateAlternateColorCodes('&', "&7Here you can find rare items"),
                    ChatColor.translateAlternateColorCodes('&', "&7that cannot be purchased otherwise."),
                    "",
                    ChatColor.translateAlternateColorCodes('&', "&e⌚ &fNew items in: &e" + timeFormatted),
                    "",
                    ChatColor.translateAlternateColorCodes('&', "&7Ancient Traveler's shop"),
                    ChatColor.translateAlternateColorCodes('&', "&7is restocked every day.")
            );

            travelerMeta.setLore(lore);
            travelerMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "nonClickable"), PersistentDataType.INTEGER, 1);
            travelerInfo.setItemMeta(travelerMeta);
        }

        gui.setItem(8, travelerInfo);

        int[] itemSlots = {29, 31, 33};
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
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name", "Item")));
                itemMeta.setLore(config.getStringList(path + ".lore"));

                if (config.contains(path + ".customModelData")) {
                    itemMeta.setCustomModelData(config.getInt(path + ".customModelData"));
                }

                List<String> lore = itemMeta.getLore() != null ? new ArrayList<>(itemMeta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add("§6 § ʙᴜʏ §f§l" + amount + " §7ꜰᴏʀ §e$" + price);
                lore.add("");
                lore.add("§a§l✔ ᴄʟɪᴄᴋ ᴛᴏ ʙᴜʏ");

                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
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
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // ✅ Solo procesar clics dentro del inventario de la tienda
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.CHEST) return;

        String expectedTitle = ChatColor.translateAlternateColorCodes('&', "&f");
        if (!event.getView().getTitle().equals(expectedTitle)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "nonClickable"), PersistentDataType.INTEGER)) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        int price = 0;
        for (String line : lore) {
            if (line.contains("$")) {
                String stripped = ChatColor.stripColor(line);
                String[] parts = stripped.split("\\$");
                if (parts.length > 1) {
                    try {
                        price = Integer.parseInt(parts[1].replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}
                }
                break;
            }
        }

        if (price <= 0) return;

        Economy econ = EconomyManager.getEconomy();
        if (econ == null || econ.getBalance(player) < price) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 0.8f);
            player.sendMessage("§cɪɴѕᴜꜰꜰɪᴄɪᴇɴᴛ ɢᴏʟᴅ!");
            return;
        }

        econ.withdrawPlayer(player, price);

        String commandToRun = getCommandTag(clickedItem);
        if (commandToRun != null) {
            String finalCommand = commandToRun.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);

            // ✅ Ejecutar sonido si el comando lo incluye
            if (commandToRun.contains("itempurchasesound")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "itempurchasesound " + player.getName());
            }
        } else {
            player.getInventory().addItem(clickedItem.clone());
        }

        player.sendMessage("§a\uE110✔ ᴘᴜʀᴄʜᴀѕᴇᴅ ᴀɴᴅ ɪᴛᴇᴍ ꜰᴏʀ §e$" + price + " ɢᴏʟᴅ ");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
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
