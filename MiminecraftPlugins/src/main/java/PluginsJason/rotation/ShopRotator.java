package PluginsJason.rotation;

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
    private static long lastRotationTime = System.currentTimeMillis();

    public ShopRotator(JavaPlugin plugin) {
        this.plugin = plugin;
        loadRotationTime();
    }

    public void rotateItems() {
        File copiedFile = new File(plugin.getDataFolder(), "copied_items.yml");
        if (!copiedFile.exists()) return;

        YamlConfiguration copiedConfig = YamlConfiguration.loadConfiguration(copiedFile);
        ConfigurationSection section = copiedConfig.getConfigurationSection("copied_items");
        if (section == null || section.getKeys(false).isEmpty()) return;

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

            if (item.contains("price")) {
                rotated.set(path + ".price", item.getInt("price"));
            }

            // ✅ Copiar encantamientos si existen
            if (item.contains("enchantments")) {
                rotated.set(path + ".enchantments", item.getStringList("enchantments"));
            }

            index++;
        }

        File rotatedFile = new File(plugin.getDataFolder(), "rotated_items.yml");
        try {
            rotated.save(rotatedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastRotationTime = System.currentTimeMillis();
        saveRotationTime();
    }

    public static long getLastRotationTime() {
        return lastRotationTime;
    }

    private void saveRotationTime() {
        File file = new File(plugin.getDataFolder(), "rotation_time.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("lastRotation", lastRotationTime);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRotationTime() {
        File file = new File(plugin.getDataFolder(), "rotation_time.yml");
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        lastRotationTime = config.getLong("lastRotation", System.currentTimeMillis());
    }

    public void startRotationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                rotateItems();
                Bukkit.broadcastMessage("§e🛒 The store has been automatically updated.");
            }
        }.runTaskTimer(plugin, 0L, 20L * 60 * 60 * 24); // Cada 24h
    }
}
