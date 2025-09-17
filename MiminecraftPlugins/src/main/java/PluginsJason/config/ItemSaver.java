package PluginsJason.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemSaver {

    public static void saveItem(File pluginFolder, ItemStack item, String id, Integer price) {
        File file = new File(pluginFolder, "copied_items.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, Object> itemData = new LinkedHashMap<>();
        itemData.put("material", item.getType().name());
        itemData.put("amount", item.getAmount());

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) itemData.put("name", meta.getDisplayName());
            if (meta.hasLore()) itemData.put("lore", meta.getLore());
            if (meta.hasCustomModelData()) itemData.put("customModelData", meta.getCustomModelData());

            if (!meta.getEnchants().isEmpty()) {
                List<String> enchants = new ArrayList<>();
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    enchants.add(entry.getKey().getName() + ":" + entry.getValue());
                }
                itemData.put("enchantments", enchants);
            }
        }

        if (price != null && price > 0) {
            itemData.put("price", price);
        }

        config.set("copied_items." + id, itemData);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
