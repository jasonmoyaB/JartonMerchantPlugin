package PluginsJason;

import PluginsJason.commands.MainCommand;
import PluginsJason.commands.ShopCommand;
import PluginsJason.config.ItemManager;
import PluginsJason.commands.GiveCommand;
import PluginsJason.commands.CopyCommand;
import PluginsJason.config.ShopRotator;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcComerse extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ItemManager itemManager = new ItemManager(getConfig());
        new ShopRotator(this).startRotationTask();
        getCommand("jmshop").setExecutor(new ShopCommand());
        getCommand("jm").setExecutor(new MainCommand(itemManager, this));
        getLogger().info("NpcComerse is now actived.");



        // Cargar configuración
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("NpcComerse disable.");
    }
}