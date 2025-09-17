package PluginsJason.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.*;

public class ShopCommand implements CommandExecutor {

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

        // 🖼 Título con glyphs que activan el fondo visual del resource pack
        String rawTitle = "&f";
        String translatedTitle = ChatColor.translateAlternateColorCodes('&', rawTitle);
        Inventory gui = Bukkit.createInventory(null, 45, translatedTitle); // 5 filas

        // 🎯 Slots horizontales centrados en la cuarta fila
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

            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString(path + ".name", "Ítem")));
                meta.setLore(config.getStringList(path + ".lore"));

                if (config.contains(path + ".customModelData")) {
                    meta.setCustomModelData(config.getInt(path + ".customModelData"));
                }

                List<String> lore = meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("§aPrecio: §2" + price + " esmeraldas");
                meta.setLore(lore);

                item.setItemMeta(meta);
            }

            gui.setItem(itemSlots[index], item);
            index++;
        }

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DISAPPEARED, 1f, 1f);
        return true;
    }
}
