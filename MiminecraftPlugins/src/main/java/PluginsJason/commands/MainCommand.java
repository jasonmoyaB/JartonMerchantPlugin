package PluginsJason.commands;

import PluginsJason.rotation.ShopRotator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class MainCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public MainCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUsa §a/jm rotate §epara rotar la tienda manualmente.");
            return true;
        }

        if (args[0].equalsIgnoreCase("rotate")) {
            new ShopRotator(plugin).rotateItems();
            sender.sendMessage("§aThe store has been manually rotated..");
            return true;
        }

        sender.sendMessage("§cComando desconocido.");
        return true;
    }
}
