package PluginsJason.commands;

import PluginsJason.config.ItemManager;
import PluginsJason.config.ItemSaver;
import PluginsJason.config.ShopRotator;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class MainCommand implements CommandExecutor {

    private final ItemManager itemManager;
    private final JavaPlugin plugin;

    public MainCommand(ItemManager itemManager, JavaPlugin plugin) {
        this.itemManager = itemManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cEste comando solo puede ser usado en el juego.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§eUso: §f/jm <give|copy|rotate> <id>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 2) {
                    player.sendMessage("§eUso: §f/jm give <id>");
                    return true;
                }

                String id = args[1];
                ItemStack item = itemManager.getItem(id);
                if (item == null) {
                    player.sendMessage("❌ No se encontró el ítem con ID: §f" + id);
                    return true;
                }

                player.getInventory().addItem(item.clone());
                player.sendMessage("✅ Recibiste el ítem: §f" + item.getItemMeta().getDisplayName());
                return true;

            case "copy":
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem == null || handItem.getType().isAir()) {
                    player.sendMessage("❌ No tienes ningún ítem en la mano.");
                    return true;
                }

                ItemMeta meta = handItem.getItemMeta();
                if (meta == null || !meta.hasCustomModelData()) {
                    player.sendMessage("❌ El ítem necesita tener customModelData para extraer el precio.");
                    return true;
                }

                int modelData = meta.getCustomModelData();
                Integer price = itemManager.getPriceByModelData(modelData);

                if (price == null) {
                    player.sendMessage("⚠ El precio no está definido en config.yml para este ítem.");
                }

                String uniqueId = "item" + System.currentTimeMillis();
                ItemSaver.saveItem(plugin.getDataFolder(), handItem, uniqueId, price);
                player.sendMessage("✅ Ítem copiado como §e" + uniqueId + "§a en §fcopied_items.yml.");
                return true;

            case "rotate":
                ShopRotator rotator = new ShopRotator(plugin);
                rotator.rotateItems();
                player.sendMessage("🔄 Rotación completada. Revisa §frotated_items.yml.");
                return true;

            default:
                player.sendMessage("❌ Subcomando desconocido. Usa §f/jm give <id>§c, §f/jm copy§c o §f/jm rotate");
                return true;
        }
    }
}