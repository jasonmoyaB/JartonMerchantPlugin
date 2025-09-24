package PluginsJason.commands;

import PluginsJason.config.ItemManager;
import PluginsJason.config.ItemSaver;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CopyCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ItemManager itemManager;

    public CopyCommand(JavaPlugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by players.");
            return true;
        }

        if (!command.getName().equalsIgnoreCase("jm") || args.length != 1 || !args[0].equalsIgnoreCase("copy")) {
            player.sendMessage("§e Correct usage: /jm copy");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cYou have no item in your hand.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        int price;

        if (meta != null && meta.hasCustomModelData()) {
            int modelData = meta.getCustomModelData();
            Integer configPrice = itemManager.getPriceByModelData(modelData);
            price = (configPrice != null) ? configPrice : new Random().nextInt(1901) + 100;
        } else {
            //Price 3000 to up
            price = new Random().nextInt(1001) + 3000;
        }

        String id = "item" + System.currentTimeMillis();
        ItemSaver.saveItem(plugin.getDataFolder(), item, id, price);
        player.sendMessage("§aItem successfully copied as §e" + id + "§a in copied_items.yml with price §e$" + price);


        File rotatedFile = new File(plugin.getDataFolder(), "rotated_items.yml");
        YamlConfiguration rotatedConfig = YamlConfiguration.loadConfiguration(rotatedFile);

        for (int i = 1; i <= 3; i++) {
            String slot = "rotated.item" + i;
            if (!rotatedConfig.contains(slot + ".material")) {
                rotatedConfig.set(slot + ".material", item.getType().name());
                rotatedConfig.set(slot + ".amount", item.getAmount());
                rotatedConfig.set(slot + ".price", price);
                rotatedConfig.set(slot + ".name", meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "Unnamed Item");
                rotatedConfig.set(slot + ".lore", meta != null && meta.hasLore() ? meta.getLore() : Collections.singletonList(""));
                if (meta != null && meta.hasCustomModelData()) {
                    rotatedConfig.set(slot + ".customModelData", meta.getCustomModelData());
                }
                try {
                    rotatedConfig.save(rotatedFile);
                    player.sendMessage("§e✔ Also add to rotated_items.yml if there's space §fen §b" + slot);
                } catch (IOException e) {
                    player.sendMessage("§cError saving to rotated_items.yml.");
                    e.printStackTrace();
                }
                break;
            }
        }

        return true;
    }
}
