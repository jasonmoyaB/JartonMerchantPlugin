package PluginsJason.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ItemManager {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> itemConfigs = new HashMap<>();

    public ItemManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadItems();
    }

    private void loadItems() {
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);
        itemConfigs.put("default", config);
    }

    public FileConfiguration getItemsConfig() {
        return itemConfigs.get("default");
    }
}

