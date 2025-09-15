package PluginsJason.commands;

import PluginsJason.config.ItemSaver;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CopyCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public CopyCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Validación: solo jugadores pueden ejecutar el comando
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cEste comando solo puede ser ejecutado por jugadores.");
            return true;
        }

        // Validación: subcomando correcto
        if (!command.getName().equalsIgnoreCase("jm") || args.length != 1 || !args[0].equalsIgnoreCase("copy")) {
            player.sendMessage("§eUso correcto: §f/jm copy");
            return true;
        }

        // Validación: ítem en mano
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§cNo tienes ningún ítem en la mano.");
            return true;
        }

        // Guardar ítem en copied_items.yml
        String id = "item" + System.currentTimeMillis(); // ID único por timestamp
        ItemSaver.saveItem(plugin.getDataFolder(), item, id);
        player.sendMessage("§aItem copiado exitosamente como §e" + id + "§a en §fcopied_items.yml.");

        return true;
    }
}
