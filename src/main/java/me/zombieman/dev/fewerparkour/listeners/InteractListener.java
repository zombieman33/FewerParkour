package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class InteractListener implements Listener {

    private FewerParkour plugin;

    public InteractListener(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, playerUUID);

        // Check if the player is in an active parkour
        String activeParkour = playerDataConfig.getString("activeParkour");
        if (activeParkour != null) {
            event.setCancelled(true);
            return;
        }

    }

}
