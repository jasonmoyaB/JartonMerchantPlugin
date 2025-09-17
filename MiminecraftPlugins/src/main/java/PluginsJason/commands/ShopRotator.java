package PluginsJason.commands;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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

            index++;
        }

        File rotatedFile = new File(plugin.getDataFolder(), "rotated_items.yml");
        try {
            rotated.save(rotatedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
