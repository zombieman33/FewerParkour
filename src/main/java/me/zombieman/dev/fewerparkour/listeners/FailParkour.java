package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.MusicInstrument;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public class FailParkour implements Listener {

    private final FewerParkour plugin;

    public FailParkour(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, playerUUID);

        String activeParkour = playerDataConfig.getString("activeParkour");
        if (activeParkour == null) return;

        if (playerDataConfig.getString("parkourSelectionMode") != null) return;

        if (!ParkourManager.parkourExists(plugin, activeParkour)) return;

        Location playerLocation = player.getLocation();

        // Store player's current direction
        Vector playerDirection = player.getLocation().getDirection();

        // Retrieve the latest checkpoint location string
        String latestCheckpointString = playerDataConfig.getString("latestCheckpointLocation." + activeParkour);
        String latestCheckpointLocationWhereYouCheckTheLocation = playerDataConfig.getString("latestCheckpointLocationWhereYouCheckTheLocation." + activeParkour);

        if (latestCheckpointString != null && latestCheckpointLocationWhereYouCheckTheLocation != null) {
            // Parse the checkpoint string
            String[] parts = latestCheckpointString.split(",");
            String[] parts2 = latestCheckpointLocationWhereYouCheckTheLocation.split(",");
            if (parts.length == 4 && parts2.length == 4) {
                String worldName = parts[0];
                double checkpointX = Double.parseDouble(parts[1]);
                double checkpointY = Double.parseDouble(parts[2]);
                double checkpointZ = Double.parseDouble(parts[3]);

                double checkpointYWhereYouCheckLocation = Double.parseDouble(parts2[2]);

                if (plugin.DEBUG) {
                    System.out.println("PlayerLocation: " + playerLocation.getX() + ", " + playerLocation.getY() + ", " + playerLocation.getZ());
                    System.out.println("LastCheckpoint: " + latestCheckpointLocationWhereYouCheckTheLocation);
                    System.out.println("Y location to be to be tpd: " + checkpointYWhereYouCheckLocation);
                }

                if (playerLocation.getY() < checkpointYWhereYouCheckLocation) {
                    Location latestCheckpointLocation = new Location(Bukkit.getWorld(worldName), checkpointX, checkpointY, checkpointZ);

                    latestCheckpointLocation.setPitch(player.getLocation().getPitch());
                    latestCheckpointLocation.setYaw(player.getLocation().getYaw());
                    latestCheckpointLocation.setDirection(player.getLocation().getDirection());

                    player.teleport(latestCheckpointLocation);

                    int failedAmount = PlayerData.getPlayerDataConfig(plugin, playerUUID).getInt("Failed Amount." + activeParkour);

                    player.sendMessage(MiniMessage.miniMessage().deserialize("""
                            <#FF0000><strikethrough>                                                      </strikethrough>
                            Click Here To Exit The Parkour.
                            <strikethrough>                                                      </strikethrough>""").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/parkour leave")).hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize("<#FF0000>/parkour leave"))));

                    PlayerData.getPlayerDataConfig(plugin, playerUUID).set("Failed Amount." + activeParkour, failedAmount + 1);
                    PlayerData.getPlayerDataConfig(plugin, playerUUID).set("latestCheckpointLocationWhereYouCheckTheLocation." + activeParkour, PlayerData.locationToString(player.getLocation()));
                    PlayerData.savePlayerData(plugin, playerUUID);

                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

//                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
//                        PlayerData.savePlayerData(plugin, playerUUID);
//                    }, 2);
                }
            }
        }
    }
}