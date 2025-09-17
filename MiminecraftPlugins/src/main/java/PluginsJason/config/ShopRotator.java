package PluginsJason.config;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopRotator {

    private final JavaPlugin plugin;

    public ShopRotator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void rotateItems() {
        File copiedFile = new File(plugin.getDataFolder(), "copied_items.yml");
        if (!copiedFile.exists()) {
            plugin.getLogger().warning("copied_items.yml does not exist.");
            return;
        }

        YamlConfiguration copiedConfig = YamlConfiguration.loadConfiguration(copiedFile);
        ConfigurationSection section = copiedConfig.getConfigurationSection("copied_items");
        if (section == null || section.getKeys(false).isEmpty()) {
            plugin.getLogger().warning("There are no items in copied_items.yml to rotate.");
            return;
        }

        List<String> keys = new ArrayList<>(section.getKeys(false));
        Collections.shuffle(keys);
        List<String> selected = keys.subList(0, Math.min(3, keys.size()));

        YamlConfiguration rotated = new YamlConfiguration();
        int index = 1;

        for (String id : selected) {
            ConfigurationSection item = section.getConfigurationSection(id);
            if (item == null) continue;

            String path = "rotated.item" + index;
            rotated.set(path + ".material", item.getString("material"));
            rotated.set(path + ".amount", item.getInt("amount", 1));
            rotated.set(path + ".name", item.getString("name"));
            rotated.set(path + ".lore", item.getStringList("lore"));
            rotated.set(path + ".customModelData", item.getInt("customModelData"));

            int price = item.getInt("price", -1);
            if (price > 0) {
                rotated.set(path + ".price", price);
            }

            index++;
        }

        File rotatedFile = new File(plugin.getDataFolder(), "rotated_items.yml");
        try {
            rotated.save(rotatedFile);
            plugin.getLogger().info("✅ Rotation completed. Updated: " + selected.size() + " ítems.");
        } catch (IOException e) {
            plugin.getLogger().severe("❌ error saving rotated_items.yml.");
            e.printStackTrace();
        }
    }

    public void startRotationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                rotateItems();
            }
        }.runTaskTimer(plugin, 0L, 20L * 60 * 60 * 24); // Cada 24 horas
    }
}
