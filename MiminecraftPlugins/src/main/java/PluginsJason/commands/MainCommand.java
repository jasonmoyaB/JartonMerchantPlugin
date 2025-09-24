package PluginsJason.commands;

import PluginsJason.config.ItemManager;
import PluginsJason.rotation.ShopRotator;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c⛔ Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§eUsage: §a/jm <copy|rotate|give>");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "copy":
                if (!player.hasPermission("jartonmerchant.copy")) {
                    player.sendMessage("§c⛔ You don't have permission to use /jm copy.");
                    return true;
                }
                return new CopyCommand(plugin, itemManager).onCommand(player, command, label, args);

            case "rotate":
                if (!player.hasPermission("jartonmerchant.rotate")) {
                    player.sendMessage("§c⛔ You don't have permission to use /jm rotate.");
                    return true;
                }
                return handleRotate(player);

            case "give":
                if (!player.hasPermission("jartonmerchant.give")) {
                    player.sendMessage("§c⛔ You don't have permission to use /jm give.");
                    return true;
                }
                return new GiveCommand(itemManager).onCommand(player, command, label, args);

            default:
                player.sendMessage("§c⛔ Unknown subcommand: §f" + sub);
                return true;
        }
    }

    private boolean handleRotate(Player player) {
        new ShopRotator(plugin).rotateItems();
        player.sendMessage("§aＴʜᴇ ѕᴛᴏʀᴇ ʜᴀѕ ʙᴇᴇɴ ᴍᴀɴᴜᴀʟʟʏ ʀᴏᴛᴀᴛᴇᴅ.");
        return true;
    }
}
