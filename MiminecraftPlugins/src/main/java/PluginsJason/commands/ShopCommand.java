package PluginsJason.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.Inventory;
import org.bukkit.configuration.file.YamlConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.\n.");
            return true;
        }

        Player player = (Player) sender;

        // Cargar ítems desde rotated_items.yml
        File file = new File(Bukkit.getPluginManager().getPlugin("NpcComerse").getDataFolder(), "rotated_items.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        List<ItemStack> items = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String path = "rotated.item" + i;
            if (!config.contains(path + ".material")) continue;

            ItemStack item = new ItemStack(
                    org.bukkit.Material.matchMaterial(config.getString(path + ".material")),
                    config.getInt(path + ".amount", 1)
            );

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (config.contains(path + ".name")) meta.setDisplayName(config.getString(path + ".name"));
                if (config.contains(path + ".lore")) meta.setLore(config.getStringList(path + ".lore"));
                if (config.contains(path + ".customModelData")) meta.setCustomModelData(config.getInt(path + ".customModelData"));
                item.setItemMeta(meta);
            }

            items.add(item);
        }

        if (items.isEmpty()) {
            player.sendMessage("§cNo rotated items available.");
            return true;
        }

        Inventory gui = Bukkit.createInventory(null, 9, "§x§F§F§C§0§0§0Shop of the Day");

        for (int i = 0; i < items.size(); i++) {
            gui.setItem(i + 3, items.get(i)); // slots 3, 4, 5
        }

        player.openInventory(gui);
        return true;
    }
}
