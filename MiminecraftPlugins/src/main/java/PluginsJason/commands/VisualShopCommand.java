package PluginsJason.commands;

import PluginsJason.gui.AncientTravelerGUI;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class VisualShopCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public VisualShopCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        new AncientTravelerGUI(plugin).openFor(player);
        return true;
    }
}
