package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

    private final FewerParkour plugin;
    public CommandListener(FewerParkour plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        String activeParkour = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("activeParkour");
        if (activeParkour == null) return;

        if (!command.startsWith("/parkour")) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>FAIL</bold>
                    You cannot run any other commands then
                    /parkour while in a parkour!
                    <strikethrough>                                                      </strikethrough>"""));
            event.setCancelled(true);
        }
    }
}
