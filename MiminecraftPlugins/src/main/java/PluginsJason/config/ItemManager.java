package PluginsJason.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemManager {

    private final FileConfiguration config;
    private final Map<String, ItemStack> loadedItems = new HashMap<>();
    private final Map<String, Integer> itemWeights = new HashMap<>();

    public ItemManager(FileConfiguration config) {
        this.config = config;
        loadItems();
    }

    public void loadItems() {
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) continue;

            Material material = Material.matchMaterial(itemSection.getString("material", "STONE"));
            if (material == null) continue;

            ItemStack item = new ItemStack(material, itemSection.getInt("amount", 1));
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            // Nombre con hex
            String name = itemSection.getString("name");
            if (name != null) meta.setDisplayName(applyHexColor(name));

            // Lore
            List<String> lore = itemSection.getStringList("lore");
            if (!lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(applyHexColor(line));
                }
                meta.setLore(coloredLore);
            }

            // CustomModelData
            if (itemSection.contains("customModelData")) {
                meta.setCustomModelData(itemSection.getInt("customModelData"));
            }

            // Encantamientos
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
            loadedItems.put(key, item);

            // Peso
            int weight = itemSection.getInt("weight", 1);
            itemWeights.put(key, weight);
        }
    }

    public ItemStack getItem(String id) {
        return loadedItems.get(id);
    }

    public Map<String, ItemStack> getAllItems() {
        return loadedItems;
    }

    public Map<String, Integer> getItemWeights() {
        return itemWeights;
    }

    private String applyHexColor(String input) {
        Pattern pattern = Pattern.compile("#([A-Fa-f0-9]{6})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder builder = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                builder.append("§").append(c);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(builder.toString()));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
