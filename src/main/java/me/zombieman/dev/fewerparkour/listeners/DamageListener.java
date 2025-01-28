package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class DamageListener implements Listener {

    private final FewerParkour plugin;

    public DamageListener(FewerParkour plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player player) {

            UUID playerUUID = player.getUniqueId();
            FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, playerUUID);

            String activeParkour = playerDataConfig.getString("activeParkour");
            if (activeParkour == null) return;

            event.setCancelled(true);

        }

    }
}
