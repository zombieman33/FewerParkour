package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class JoinQuitListener implements Listener {
    private FewerParkour plugin;

    public JoinQuitListener(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> PlayerData.cleanupCache(event.getPlayer()));
    }
    @EventHandler
    public void onQuitNormal(PlayerQuitEvent event) {

        Component message;

        Player player = event.getPlayer();

        String parkour = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("activeParkour");
        List<String> locations = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getStringList("completedCheckpoints." + parkour);

        if (parkour == null) return;

        locations.clear();

        player.setLevel(PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getInt("Start Level." + parkour));

        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("Start Level." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("Level." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("completedCheckpoints." + parkour, locations);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("amountOfCheckpointsPassed." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("latestCheckpointLocation." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("latestCheckpointLocationWhereYouCheckTheLocation." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("Failed Amount." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("startTime", null);

        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("activeParkour", null);
        PlayerData.savePlayerData(plugin, player.getUniqueId());

        PlayerData.clearCompletedCheckpoints(plugin, player.getUniqueId(), parkour);

    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerData.createFile(plugin, event.getPlayer().getUniqueId());
    }
}
