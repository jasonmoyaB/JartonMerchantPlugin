package PluginsJason;

import PluginsJason.commands.CopyCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcComerse extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("NpcComerse is now actived.");

        // Registrar comando /jm
        getCommand("jm").setExecutor((CommandExecutor) new CopyCommand(this));

        // Cargar configuración
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("NpcComerse disable.");
    }
}