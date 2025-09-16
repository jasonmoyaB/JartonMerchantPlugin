package PluginsJason;

import PluginsJason.commands.MainCommand;
import PluginsJason.commands.ShopCommand;
import PluginsJason.commands.VisualShopCommand;
import PluginsJason.config.ItemManager;
import PluginsJason.config.ShopRotator;
import PluginsJason.gui.AncientTravelerGUI;
import PluginsJason.listeners.ShopListener;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcComerse extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 🧠 Inicializar sistema de ítems
        ItemManager itemManager = new ItemManager(getConfig());

        // 🔄 Iniciar rotación automática de ítems
        new ShopRotator(this).startRotationTask();

        // 🎨 Registrar GUI visual personalizada
        new AncientTravelerGUI(this); // Solo se registra, no se abre aquí

        // 🧩 Registrar eventos y comandos
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getCommand("jmshop").setExecutor(new ShopCommand(this));        // Abre comerciante visual
        getCommand("jmshopgui").setExecutor(new VisualShopCommand(this));    // Abre GUI visual tipo panel
        getCommand("jm").setExecutor(new MainCommand(itemManager, this));

        getLogger().info("✅ NpcComerse activado sin dependencia de CommandPanels.");
    }

    @Override
    public void onDisable() {
        getLogger().info("🛑 NpcComerse desactivado.");
    }
}