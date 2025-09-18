package PluginsJason;

import PluginsJason.commands.MainCommand;
import PluginsJason.commands.ShopCommand;
import PluginsJason.config.ItemManager;
import PluginsJason.gui.AncientTravelerGUI;
import PluginsJason.listeners.ShopListener;
import PluginsJason.economy.EconomyManager;
import PluginsJason.rotation.ShopRotator;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcComerse extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // 💰 Inicializar Vault Economy
        if (!EconomyManager.setupEconomy(this)) {
            getLogger().severe("Vault is unavailable. Plugin disabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 🧠 Inicializar sistema de ítems
        ItemManager itemManager = new ItemManager(getConfig());

        // 🔄 Iniciar rotación automática de ítems
        new ShopRotator(this).startRotationTask();

        // 🎨 Registrar GUI visual personalizada
        new AncientTravelerGUI(this); // Solo se registra, no se abre aquí

        // 🧩 Registrar eventos y comandos
        getServer().getPluginManager().registerEvents(new ShopListener(), this);
        getServer().getPluginManager().registerEvents(new ShopCommand(this), this); // Eventos de compra
        getCommand("jmshop").setExecutor(new ShopCommand(this));        // Abre comerciante visual
//        getCommand("jmshopgui").setExecutor(new VisualShopCommand(this));    // Abre GUI visual tipo panel
        getCommand("jm").setExecutor(new MainCommand(this, itemManager));


        getLogger().info("✅ NpcComerse now is actived!!!");
    }

    @Override
    public void onDisable() {
        getLogger().info("🛑 NpcComerse disable.");
    }
}