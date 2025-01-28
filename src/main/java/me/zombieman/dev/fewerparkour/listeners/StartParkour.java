package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class StartParkour implements Listener {

    private FewerParkour plugin;

    public StartParkour(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, playerUUID);

        // Check if the player has an active parkour
        if (playerDataConfig.getString("activeParkour") != null) return;

        if (playerDataConfig.getString("parkourSelectionMode") != null) return;

        Location playerLocation = player.getLocation();

        // Get the player's current location
        int x = (int) player.getLocation().getX();
        int y = (int) player.getLocation().getY();
        int z = (int) player.getLocation().getZ();

        // Iterate through all parkours to check if the player is on a start checkpoint
        for (String parkourName : ParkourManager.getAllParkours(plugin)) {
            FileConfiguration config = ParkourManager.getParkourConfig(plugin, parkourName);
            if (config == null) continue;

//            System.out.println(parkourName);

            String start = config.getString("start");
//
//            System.out.println(start);
//
//            System.out.println(x + y + z);
//
//            System.out.println("Players location");
//            System.out.println((int) player.getLocation().getX() + "," + (int) player.getLocation().getY() + "," + (int) player.getLocation().getZ());

            if (start != null && isPlayerOnStart(x, y, z, start)) {

                Material type = playerLocation.getWorld().getBlockAt((int) playerLocation.getX(), playerLocation.getBlockY() - 1, playerLocation.getBlockZ()).getType();

                if (type.isAir() || type != ParkourManager.getCheckpointBlock(plugin, parkourName)) return;

                // Set the active parkour in player data


                playerDataConfig.set("activeParkour", parkourName);
                playerDataConfig.set("Start Level." + parkourName, player.getLevel());
                playerDataConfig.set("Level." + parkourName, 0);
                playerDataConfig.set("startTime", Instant.now().toEpochMilli());
                playerDataConfig.set("latestCheckpointLocation." + parkourName, PlayerData.locationToString(player.getLocation()));
                playerDataConfig.set("latestCheckpointLocationWhereYouCheckTheLocation." + parkourName, PlayerData.locationToString(playerLocation));
                PlayerData.savePlayerData(plugin, playerUUID);

                player.setLevel(0);

                List<String> checkpoints = config.getStringList("checkpoints");
                Material checkpointBlock = ParkourManager.getCheckpointBlock(plugin, parkourName);

                String worldStr = ParkourManager.getParkourConfig(plugin, parkourName).getString("world");

                if (worldStr == null) return;

                World world = Bukkit.getWorld(worldStr);

                if (world == null) return;

                int checkpointCount = 0;
                int totalCheckpoints = checkpoints.size();

                if (!player.hasPermission("group.prime")) {
                    for (String checkpoint : checkpoints) {
                        String[] coords = checkpoint.split(",");
                        int xC = Integer.parseInt(coords[0]);
                        int yC = Integer.parseInt(coords[1]);
                        int zC = Integer.parseInt(coords[2]);

                        Block block = world.getBlockAt(xC, yC, zC);
                        if (block.getType() == checkpointBlock) {
                            checkpointCount++;
                        }
                    }
                } else {
                    checkpointCount = totalCheckpoints;
                }

                Component message = MiniMessage.miniMessage().deserialize("""
                    <#00FF00><strikethrough>                                                           </strikethrough>
                    Successfully started the <bold><underlined>%s</underlined></bold> parkour!
                    There are <bold><underlined>%d</underlined></bold> checkpoints you need to complete!
                    <strikethrough>                                                           </strikethrough>""".formatted(parkourName, checkpointCount));
                player.sendMessage(message);

                player.playSound(playerLocation, Sound.ITEM_GOAT_HORN_SOUND_2, 1.0f, 1.0f);

//                startActionBarTimer(player, playerUUID);
                startElytraPreventionTask();
                break;
            }
        }
    }

    private boolean isPlayerOnStart(int x, int y, int z, String startLocation) {
        y = y - 1; // Adjust y coordinate to check the block directly below the player

        String[] parts = startLocation.split(",");
        int startX = Integer.parseInt(parts[0]);
        int startY = Integer.parseInt(parts[1]);
        int startZ = Integer.parseInt(parts[2]);

        // Check a 3x3 area around the start location
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (x == startX + xOffset && y == startY && z == startZ + zOffset) {
                    return true;
                }
            }
        }

        return false;
    }

    private void startActionBarTimer(Player player, UUID playerUUID) {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, playerUUID);

                String parkour = playerDataConfig.getString("activeParkour");
                if (parkour == null) {
                    this.cancel();
                    return;
                }

                if (!ParkourManager.parkourExists(plugin, parkour)) {
                    playerDataConfig.set("activeParkour", null);
                    playerDataConfig.set("startTime", null);
                    PlayerData.savePlayerData(plugin, playerUUID);
                    this.cancel();
                    return;
                }

                long startTime = playerDataConfig.getLong("startTime");
                long elapsedTime = Instant.now().toEpochMilli() - startTime;

                long minutes = (elapsedTime / 1000) / 60;
                long seconds = (elapsedTime / 1000) % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                int failedAmount = PlayerData.getPlayerDataConfig(plugin, playerUUID).getInt("Failed Amount." + parkour);

                player.sendActionBar(MiniMessage.miniMessage().deserialize("<#00FF00>Parkour Time: <underlined><bold>" + timeString + "</underlined><bold> Failed: <underlined><bold>" + failedAmount));
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startElytraPreventionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId());
                    if (playerDataConfig.getString("activeParkour") == null) {
                        this.cancel();
                        return;
                    }

                    player.setFlying(false);

                    if (player.isGliding()) {
                        player.setGliding(false);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
