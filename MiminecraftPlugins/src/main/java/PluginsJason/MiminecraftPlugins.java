package PluginsJason;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class MiminecraftPlugins extends JavaPlugin implements Listener {

    // ================= MiniBan =================
    // Conjunto de jugadores baneados
    private HashSet<String> bannedPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        getLogger().info("✅ MiPluginJason activado!");
        // Registramos el listener de bienvenida y baneos
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("❌ MiPluginJason desactivado!");
    }

    // ================= WelcomeMessage =================
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Mensaje de bienvenida al jugador
        player.sendMessage("§aWelcome to the server, §e" + player.getName() + "§a!");
        // Mensaje que verá todo el servidor
        event.setJoinMessage("§6» §e" + player.getName() + " §ahas joined the server.");

        // ================= MiniBan =================
        // Expulsa al jugador si está baneado
        if (bannedPlayers.contains(player.getName())) {
            player.kickPlayer("§cYou are banned from this server.");
        }
    }

    // ================= Comandos =================
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return true;
        }

        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {

            // ================= MiniBan =================
            case "ban":
                if (!player.isOp()) {
                    player.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
                if (args.length == 1) {
                    String targetName = args[0];
                    bannedPlayers.add(targetName);
                    Player target = Bukkit.getPlayerExact(targetName);
                    if (target != null) {
                        target.kickPlayer("§cYou have been banned from the server.");
                    }
                    sender.sendMessage("§aYou have banned " + targetName);
                }
                return true;

            case "unban":
                if (!player.isOp()) {
                    player.sendMessage("§cYou don't have permission to use this command.");
                    return true;
                }
                if (args.length == 1) {
                    String targetName = args[0];
                    if (bannedPlayers.remove(targetName)) {
                        sender.sendMessage("§aYou have unbanned " + targetName);
                    } else {
                        sender.sendMessage("§cThe player " + targetName + " was not banned.");
                    }
                }
                return true;

            // ================= PlayerInfo =================
            case "miinfo":
                String info = "§aYour Info:\n" +
                        "§eName: §f" + player.getName() + "\n" +
                        "§eWorld: §f" + player.getWorld().getName() + "\n" +
                        "§eCoordinates: §fX=" + player.getLocation().getBlockX() +
                        " Y=" + player.getLocation().getBlockY() +
                        " Z=" + player.getLocation().getBlockZ();
                player.sendMessage(info);
                return true;

            // ================= RTP =================
            case "rtp":
                int radius = 1000; // rango de ±1000 bloques
                int x = (int) (Math.random() * (radius * 2)) - radius;
                int z = (int) (Math.random() * (radius * 2)) - radius;
                int y = player.getWorld().getHighestBlockYAt(x, z); // altura segura

                player.teleport(new org.bukkit.Location(player.getWorld(), x, y, z));
                player.sendMessage("§aTeleported to a random location! X=" + x + " Z=" + z);
                return true;
        }

        return false;
    }
}