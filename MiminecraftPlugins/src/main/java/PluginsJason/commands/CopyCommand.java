package PluginsJason.commands;

import PluginsJason.config.ItemManager;
import PluginsJason.config.ItemSaver;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

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
        if (meta == null || !meta.hasCustomModelData()) {
            player.sendMessage("§cEl ítem necesita tener customModelData para extraer el precio.");
            return true;
        }

        int modelData = meta.getCustomModelData();
        Integer price = itemManager.getPriceByModelData(modelData);

        String id = "item" + System.currentTimeMillis();
        ItemSaver.saveItem(plugin.getDataFolder(), item, id, price);

        player.sendMessage("§aItem copiado exitosamente como §e" + id + "§a en §fcopied_items.yml.");
        return true;
    }
}
