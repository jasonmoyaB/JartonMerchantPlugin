package PluginsJason.commands;

import PluginsJason.NpcComerse;
import PluginsJason.utils.ItemUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CopyCommand implements CommandExecutor {

    private final NpcComerse plugin;

    public CopyCommand(NpcComerse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        String yaml = ItemUtils.convertItemToYaml(player.getInventory().getItemInMainHand());
        player.sendMessage("§aItem data copied:");
        player.sendMessage("§7" + yaml);
        return true;
    }
}
