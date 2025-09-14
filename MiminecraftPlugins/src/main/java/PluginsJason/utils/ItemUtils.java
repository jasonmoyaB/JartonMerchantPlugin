package PluginsJason.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemUtils {

    public static String convertItemToYaml(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return "There is no item in hand.";

        ItemMeta meta = item.getItemMeta();
        StringBuilder yaml = new StringBuilder();

        yaml.append("material: ").append(item.getType()).append("\n");

        if (meta.hasDisplayName()) {
            yaml.append("name: '").append(meta.getDisplayName()).append("'\n");
        }

        if (meta.hasLore()) {
            yaml.append("lore:\n");
            for (String line : meta.getLore()) {
                yaml.append("  - '").append(line).append("'\n");
            }
        }

        if (meta.hasEnchants()) {
            yaml.append("enchanted:\n");
            for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                yaml.append("  - ").append(entry.getKey().getKey().getKey().toUpperCase())
                        .append(" ").append(entry.getValue()).append("\n");
            }
        }

        if (meta.hasCustomModelData()) {
            yaml.append("customModelData: ").append(meta.getCustomModelData()).append("\n");
        }

        return yaml.toString();
    }
}

