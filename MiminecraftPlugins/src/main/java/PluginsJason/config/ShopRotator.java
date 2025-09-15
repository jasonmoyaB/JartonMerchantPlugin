package PluginsJason.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopRotator {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public ShopRotator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startRotationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                rotateItems();
            }
        }.runTaskTimer(plugin, 0L, 20L * 60 * 60 * 24); // cada 24h
    }

    public void rotateItems() {
        File sourceFile = new File(plugin.getDataFolder(), "copied_items.yml");
        if (!sourceFile.exists()) return;

        YamlConfiguration sourceConfig = YamlConfiguration.loadConfiguration(sourceFile);
        ConfigurationSection section = sourceConfig.getConfigurationSection("copied_items");
        if (section == null) return;

        Map<String, ItemStack> items = new HashMap<>();
        Map<String, Integer> weights = new HashMap<>();

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) continue;

            Material material = Material.matchMaterial(itemSection.getString("material", "STONE"));
            if (material == null) continue;

            ItemStack item = new ItemStack(material, itemSection.getInt("amount", 1));
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            if (itemSection.contains("name")) meta.setDisplayName(itemSection.getString("name"));
            if (itemSection.contains("lore")) meta.setLore(itemSection.getStringList("lore"));
            if (itemSection.contains("customModelData")) meta.setCustomModelData(itemSection.getInt("customModelData"));

            if (itemSection.contains("enchantments")) {
                List<String> enchants = itemSection.getStringList("enchantments");
                for (String enchant : enchants) {
                    String[] parts = enchant.split(":");
                    if (parts.length == 2) {
                        Enchantment ench = Enchantment.getByName(parts[0].toUpperCase());
                        int level = Integer.parseInt(parts[1]);
                        if (ench != null) item.addUnsafeEnchantment(ench, level);
                    }
                }
            }

            item.setItemMeta(meta);
            items.put(key, item);
            weights.put(key, itemSection.getInt("weight", 1));
        }

        List<String> selectedKeys = selectWeightedItems(weights, 3);
        saveRotatedItems(selectedKeys, items);
    }

    private List<String> selectWeightedItems(Map<String, Integer> weights, int count) {
        List<String> pool = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pool.add(entry.getKey());
            }
        }

        Collections.shuffle(pool, random);
        Set<String> unique = new LinkedHashSet<>(pool);
        List<String> selected = new ArrayList<>();

        for (String key : unique) {
            if (selected.size() >= count) break;
            selected.add(key);
        }

        return selected;
    }

    private void saveRotatedItems(List<String> keys, Map<String, ItemStack> items) {
        File file = new File(plugin.getDataFolder(), "rotated_items.yml");
        YamlConfiguration config = new YamlConfiguration();

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            ItemStack item = items.get(key);
            ItemMeta meta = item.getItemMeta();

            String path = "rotated.item" + (i + 1);
            config.set(path + ".material", item.getType().name());
            config.set(path + ".amount", item.getAmount());

            if (meta != null) {
                if (meta.hasDisplayName()) config.set(path + ".name", meta.getDisplayName());
                if (meta.hasLore()) config.set(path + ".lore", meta.getLore());
                if (meta.hasCustomModelData()) config.set(path + ".customModelData", meta.getCustomModelData());

                if (!meta.getEnchants().isEmpty()) {
                    List<String> enchants = new ArrayList<>();
                    for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                        enchants.add(entry.getKey().getName() + ":" + entry.getValue());
                    }
                    config.set(path + ".enchantments", enchants);
                }
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

