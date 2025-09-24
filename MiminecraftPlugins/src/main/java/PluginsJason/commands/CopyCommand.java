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
            sender.sendMessage("§cEste comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        if (!command.getName().equalsIgnoreCase("jm") || args.length != 1 || !args[0].equalsIgnoreCase("copy")) {
            player.sendMessage("§eUso correcto: §f/jm copy");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cNo tienes ningún ítem en la mano.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        int price;

        if (meta != null && meta.hasCustomModelData()) {
            int modelData = meta.getCustomModelData();
            Integer configPrice = itemManager.getPriceByModelData(modelData);
            price = (configPrice != null) ? configPrice : new Random().nextInt(1901) + 100;
        } else {
            price = new Random().nextInt(1901) + 100;
        }

        String id = "item" + System.currentTimeMillis();
        ItemSaver.saveItem(plugin.getDataFolder(), item, id, price);
        player.sendMessage("§aItem copiado exitosamente como §e" + id + "§a en §fcopied_items.yml con precio §e$" + price);

        // También agregar a rotated_items.yml si hay espacio
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
                    player.sendMessage("§e✔ También agregado a §arotated_items.yml §fen §b" + slot);
                } catch (IOException e) {
                    player.sendMessage("§cError al guardar en rotated_items.yml.");
                    e.printStackTrace();
                }
                break;
            }
        }

        return true;
    }
}
