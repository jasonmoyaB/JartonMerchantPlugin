package PluginsJason.commands;

import PluginsJason.config.ItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
            sender.sendMessage("Only in game you can use the command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("use: /jm <give|copy> <id>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 2) {
                    player.sendMessage("Use: /jm give <id>");
                    return true;
                }
                String id = args[1];
                ItemStack item = itemManager.getItem(id);
                if (item == null) {
                    player.sendMessage("❌ We could not find the item: " + id);
                    return true;
                }
                player.getInventory().addItem(item.clone());
                player.sendMessage("✅ You got the item: " + item.getItemMeta().getDisplayName());
                return true;

            case "copy":
                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem == null || handItem.getType().isAir()) {
                    player.sendMessage("❌ You don´t have any item in hand.");
                    return true;
                }

                // Aquí puedes agregar la lógica para guardar el ítem en archivo
                player.sendMessage("📋 Item data copied. Review the file to paste it into config.yml.");
                return true;

            default:
                player.sendMessage("Unknown subcommand. Use /jm give <id> or /jm copy");
                return true;
        }
    }
}