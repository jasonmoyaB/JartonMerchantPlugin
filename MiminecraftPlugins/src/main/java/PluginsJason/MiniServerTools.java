package PluginsJason;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MiniServerTools es un plugin simple que demuestra cómo banear
 * y desbanear jugadores usando el BanList integrado de Bukkit.
 */
public class MiniServerTools extends JavaPlugin {

    @Override
    public void onEnable() {
        // Se ejecuta cuando el plugin se habilita
        getLogger().info("MiniServerTools habilitado!");
    }

    @Override
    public void onDisable() {
        // Se ejecuta cuando el plugin se deshabilita
        getLogger().info("MiniServerTools deshabilitado!");
    }

    /**
     * Método que maneja comandos del plugin.
     * @param sender quien ejecuta el comando
     * @param cmd comando ejecutado
     * @param label alias del comando
     * @param args argumentos del comando
     * @return true si el comando se procesó correctamente
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // ------------------ COMANDO /ban ------------------
        if (cmd.getName().equalsIgnoreCase("ban")) {

            // Verificar permisos
            if (!sender.hasPermission("miniservertools.ban")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
                return true;
            }

            // Validar argumentos
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Uso: /ban <jugador> [razón]");
                return true;
            }

            String targetName = args[0]; // Nombre del jugador a banear
            // Si hay razón, la usamos; si no, usamos un mensaje por defecto
            String reason = (args.length > 1) ? String.join(" ", args).substring(targetName.length()).trim()
                    : "Baneado por un administrador";

            // OfflinePlayer funciona aunque el jugador no esté conectado
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            // Banear usando BanList integrado de Bukkit
            // addBan(nombre, razón, expiración (null = permanente), fuente del baneo)
            Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, null, sender.getName());

            // Si el jugador está online, lo expulsamos inmediatamente
            if (target.isOnline()) {
                Player onlinePlayer = (Player) target;
                onlinePlayer.kickPlayer(ChatColor.RED + "Has sido baneado: " + reason);
            }

            // Mensaje de confirmación al ejecutor del comando
            sender.sendMessage(ChatColor.GREEN + "El jugador " + targetName + " ha sido baneado.");
            return true;
        }

        // ------------------ COMANDO /unban ------------------
        if (cmd.getName().equalsIgnoreCase("unban")) {

            // Verificar permisos
            if (!sender.hasPermission("miniservertools.unban")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
                return true;
            }

            // Validar argumentos
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Uso: /unban <jugador>");
                return true;
            }

            String targetName = args[0];

            // Quitar baneo usando BanList integrado
            Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);

            // Mensaje de confirmación
            sender.sendMessage(ChatColor.GREEN + "El jugador " + targetName + " ha sido desbaneado.");
            return true;
        }

        return false; // Comando no reconocido
    }
}
