package me.zombieman.dev.fewerparkour.listeners;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class XPPickUp implements Listener {

    private FewerParkour plugin;

    public XPPickUp(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExPickUp(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();

        if (PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("activeParkour") == null) return;

        event.setCancelled(true);

    }

}
