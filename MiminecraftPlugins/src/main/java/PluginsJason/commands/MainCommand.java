package PluginsJason.commands;

import PluginsJason.config.ItemManager;
import PluginsJason.config.ItemSaver;
import PluginsJason.rotation.ShopRotator;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class MainCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ItemManager itemManager;

    public MainCommand(JavaPlugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUsa §a/jm <rotate|copy|give>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "rotate":
                return handleRotate(sender);

            case "copy":
                return new CopyCommand(plugin, itemManager).onCommand(sender, command, label, args);

            case "give":
                return new GiveCommand(itemManager).onCommand(sender, command, label, args);

            default:
                sender.sendMessage("§cSubcomando desconocido: §f" + sub);
                return true;
        }
    }

    private boolean handleRotate(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        new ShopRotator(plugin).rotateItems();
        sender.sendMessage("§aLa tienda ha sido rotada manualmente.");
        return true;
    }
}
