package PluginsJason.commands;

import PluginsJason.config.ItemManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand implements CommandExecutor {

    private final ItemManager itemManager;

    public GiveCommand(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse en el juego.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("give")) {
            player.sendMessage("Uso: /jm give <id>");
            return true;
        }

        String id = args[1];
        ItemStack item = itemManager.getItem(id);

        if (item == null) {
            player.sendMessage("❌ No se encontró el ítem con ID: " + id);
            return true;
        }

        player.getInventory().addItem(item.clone());
        player.sendMessage("✅ Recibiste el ítem: " + item.getItemMeta().getDisplayName());
        return true;
    }
}

