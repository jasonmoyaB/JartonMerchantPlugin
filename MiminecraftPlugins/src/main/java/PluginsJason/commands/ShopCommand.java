package PluginsJason.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.*;

public class ShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        File file = new File(Bukkit.getPluginManager().getPlugin("NpcComerse").getDataFolder(), "rotated_items.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<MerchantRecipe> recipes = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String path = "rotated.item" + i;
            if (!config.contains(path + ".material")) continue;

            Material material = Material.matchMaterial(config.getString(path + ".material"));
            if (material == null) continue;

            ItemStack result = new ItemStack(material, config.getInt(path + ".amount", 1));
            ItemMeta meta = result.getItemMeta();

            if (meta != null) {
                if (config.contains(path + ".name")) {
                    meta.setDisplayName(config.getString(path + ".name"));
                }

                if (config.contains(path + ".lore")) {
                    meta.setLore(config.getStringList(path + ".lore"));
                }

                if (config.contains(path + ".customModelData")) {
                    meta.setCustomModelData(config.getInt(path + ".customModelData"));
                }

                result.addUnsafeEnchantment(Enchantment.LURE, 1); // efecto brillante
                result.setItemMeta(meta);
            }

            // Precio en esmeraldas (por defecto 1)
            int price = config.getInt(path + ".price", 1);
            ItemStack cost = new ItemStack(Material.EMERALD, price);

            MerchantRecipe recipe = new MerchantRecipe(result, 9999);
            recipe.addIngredient(cost);
            recipes.add(recipe);
        }

        Merchant merchant = Bukkit.createMerchant("§x§F§F§C§0§0§0Ancient Traveler");
        merchant.setRecipes(recipes);

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1f, 1f);
        player.openMerchant(merchant, true);
        return true;
    }
}