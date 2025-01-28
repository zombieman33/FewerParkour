package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.ParkourData;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CheckpointsParkour implements Listener {

    private final FewerParkour plugin;

    public CheckpointsParkour(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCheckPoint(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(plugin, playerUUID);

        // Check if the player is in an active parkour
        String activeParkour = playerDataConfig.getString("activeParkour");
        if (activeParkour == null) return;

        if (PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("parkourSelectionMode") != null)
            return;

        if (!ParkourManager.parkourExists(plugin, activeParkour)) return;

        // Get the player's current location
        Location playerLocation = player.getLocation();

        int xLoc = (int) playerLocation.getBlockX();
        int yLoc = playerLocation.getBlockY() - 1;
        int zLoc = playerLocation.getBlockZ();

        if (plugin.DEBUG) {
            System.out.println("Real Loc X " + playerLocation.getBlockX());

            System.out.println(xLoc + ", " + yLoc + ", " + zLoc);
        }

        Material type = playerLocation.getWorld().getBlockAt(xLoc, yLoc, zLoc).getType();

        if (plugin.DEBUG) System.out.println("Type " + type);

        if (plugin.DEBUG) {
            System.out.println("Prime Block Type " + ParkourManager.getPrimeCheckpointBlock(plugin, activeParkour));
            System.out.println("Normal Block Type " + ParkourManager.getCheckpointBlock(plugin, activeParkour));
        }

        if (type.isAir() || type != ParkourManager.getCheckpointBlock(plugin, activeParkour) && type != ParkourManager.getPrimeCheckpointBlock(plugin, activeParkour))
            return;

        if (plugin.DEBUG) System.out.println("Type is not air");
        if (plugin.DEBUG)
            System.out.println("Or type is not " + ParkourManager.getCheckpointBlock(plugin, activeParkour));

        // Check if player is standing on a checkpoint (2x2 area) and it's not completed

        playerLocation = playerLocation.set(playerLocation.getX(), playerLocation.getY() - 1, playerLocation.getZ());

        int x = (int) player.getLocation().getX();
        int y = (int) player.getLocation().getY();
        int z = (int) player.getLocation().getZ();

        if (!isPlayerOnCheckpoint(player, playerLocation, activeParkour)) return;

        PlayerData.getPlayerDataConfig(plugin, playerUUID).set("latestCheckpointLocationWhereYouCheckTheLocation." + activeParkour, PlayerData.locationToString(player.getLocation()));
        PlayerData.savePlayerData(plugin, playerUUID);

        String color = "<#00FF00>";

        if (!PlayerData.hasCompletedCheckpoint(plugin, playerUUID, playerLocation, activeParkour)) {
            // Mark checkpoint as completed
            if (isPlayerOnPrimeCheckpoint(player, playerLocation, activeParkour)) {
                if (!player.hasPermission("group.prime")) {

                    player.sendMessage(MiniMessage.miniMessage().deserialize("""
                                    <#B05CFF><strikethrough>                                                      </strikethrough>
                                    You aren't able to claim this checkpoint,
                                    without the <bold>Prime</bold> Rank! <underlined><Click To Buy></underlined>
                                    <strikethrough>                                                      </strikethrough>""")
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://store.fewer.live/category/prime-rank"))
                            .hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize("<#B05CFF>Buy Prime"))));

                    player.sendTitle(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Buy Prime", ChatColor.LIGHT_PURPLE + "To unlock this checkpoint!");
                    player.playSound(playerLocation, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f,1.0f);

                    mark3x3CheckpointCompleted(player, playerLocation.set(playerLocation.getX(), playerLocation.getY() + 1, playerLocation.getZ()), activeParkour);
                    return;
                }
                color = "<#B05CFF>";
            }

            mark3x3CheckpointCompleted(player, playerLocation.set(playerLocation.getX(), playerLocation.getY() + 1, playerLocation.getZ()), activeParkour);

            int checkpointsPassed = playerDataConfig.getInt("amountOfCheckpointsPassed." + activeParkour, 0) + 1;

            // Check if all checkpoints are completed
            FileConfiguration parkourConfig = ParkourManager.getParkourConfig(plugin, activeParkour);
            List<String> checkpoints = parkourConfig.getStringList("checkpoints");
//
//            System.out.println(checkpointsPassed >= checkpoints.size());
//            System.out.println(checkpointsPassed + ">=" + checkpoints.size());


            Material checkpointBlock = ParkourManager.getCheckpointBlock(plugin, activeParkour);

            String worldStr = ParkourManager.getParkourConfig(plugin, activeParkour).getString("world");

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

            if (checkpointsPassed >= checkpointCount) {
//                System.out.println(checkpoints.size());

                long startTime = playerDataConfig.getLong("startTime");
                long elapsedTime = Instant.now().toEpochMilli() - startTime;
                long minutes = (elapsedTime / 1000) / 60;
                long seconds = (elapsedTime / 1000) % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);

                int failedAmount = PlayerData.getPlayerDataConfig(plugin, playerUUID).getInt("Failed Amount." + activeParkour);

                Component completeMessage = MiniMessage.miniMessage().deserialize(String.format("""
                <#00FF00><strikethrough>                                                                                </strikethrough>
                ✨ <bold>Congratulations!</bold> ✨
                You completed the <bold>%s</bold> parkour in <bold><underlined>%s</underlined></bold>!
                You failed a jump: <bold><underlined>%s</underlined></bold> times!
                <strikethrough>                                                                                </strikethrough>""", activeParkour, timeString, failedAmount));

                Component message = MiniMessage.miniMessage().deserialize(String.format("""
                    <#00FF00><strikethrough>                                                                                </strikethrough>
                    ✨ <bold>Congratulations!</bold> ✨
                    <bold>%s</bold> completed the <bold>%s</bold> parkour in <bold><underlined>%s</underlined></bold>!
                    <bold>%s</bold> failed a jump: <bold><underlined>%s</underlined></bold> times!
                    <strikethrough>                                                                                </strikethrough>""", player.getName(), activeParkour, timeString, player.getName(), failedAmount));

                for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayers != player) {
                        onlinePlayers.sendMessage(message);
                        onlinePlayers.playSound(onlinePlayers.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
                    }
                }

                List<String> winCommands = ParkourManager.getParkourConfig(plugin, activeParkour).getStringList("Win Commands");

                for (String command : winCommands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }

                if (!winCommands.isEmpty()) {

                    completeMessage = MiniMessage.miniMessage().deserialize(String.format("""
                        <#00FF00><strikethrough>                                                                                </strikethrough>
                        ✨ <bold>Congratulations!</bold> ✨
                        You completed the <bold>%s</bold> parkour in <bold><underlined>%s</underlined></bold>!
                        You failed a jump: <bold><underlined>%s</underlined></bold> times!
                        You also got a reward for completing the parkour!
                        <strikethrough>                                                                                </strikethrough>""", activeParkour, timeString, failedAmount));

                }

                player.setLevel(PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getInt("Start Level." + activeParkour));
                player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.sendMessage(completeMessage);

                startCountdown(plugin, player, activeParkour);

                // Clear active parkour and checkpoints passed
                playerDataConfig.set("activeParkour", null);
                playerDataConfig.set("startTime", null);
                playerDataConfig.set("Level." + activeParkour, null);
                playerDataConfig.set("Start Level." + activeParkour, null);
                playerDataConfig.set("amountOfCheckpointsPassed." + activeParkour, null);
                playerDataConfig.set("Failed Amount." + activeParkour, null);
                playerDataConfig.set("latestCheckpointLocation." + activeParkour, null);
                playerDataConfig.set("latestCheckpointLocationWhereYouCheckTheLocation." + activeParkour, null);
                PlayerData.savePlayerData(plugin, playerUUID);
                PlayerData.clearCompletedCheckpoints(plugin, playerUUID, activeParkour);

                long timeInSeconds = elapsedTime / 1000;

                System.out.println(timeInSeconds);

                PlayerData.setBestTimeIfNeeded(plugin, playerUUID, timeInSeconds);
            } else {

                // Send a message using MiniMessage
                Component message = MiniMessage.miniMessage().deserialize("""
                        %s<strikethrough>                         </strikethrough>
                        Checkpoint reached!
                        <strikethrough>                         </strikethrough>""".formatted(color)
                );


                Location finalPlayerLocation = playerLocation;

                player.sendMessage(message);

                int level = playerDataConfig.getInt("Level." + activeParkour);

                level = level + 1;

                player.setLevel(level);

                // Increment checkpoint count
                playerDataConfig.set("amountOfCheckpointsPassed." + activeParkour, checkpointsPassed);
                playerDataConfig.set("Level." + activeParkour, level);
                playerDataConfig.set("latestCheckpointLocation." + activeParkour, PlayerData.locationToString(playerLocation));
                PlayerData.savePlayerData(plugin, playerUUID);


                player.playSound(playerLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.playSound(finalPlayerLocation, Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0f, 1.0f);
                }, 2L);
            }
        }
    }
    private boolean isPlayerOnCheckpoint(Player player, Location playerLocation, String parkour) {
        int playerX = playerLocation.getBlockX();
        int playerY = playerLocation.getBlockY();
        int playerZ = playerLocation.getBlockZ();

        // Get the checkpoint block type
        Material checkpointBlockType = ParkourManager.getCheckpointBlock(plugin, parkour);
        Material primeCheckpointBlockType = ParkourManager.getPrimeCheckpointBlock(plugin, parkour);

        // Get the list of checkpoint locations from the parkour configuration
        FileConfiguration config = ParkourManager.getParkourConfig(plugin, parkour);
        if (config == null) return false;

        List<String> checkpoints = config.getStringList("checkpoints");
        if (checkpoints.isEmpty()) return false;

        for (String checkpointStr : checkpoints) {
            String[] parts = checkpointStr.split(",");
            if (parts.length != 3) continue;

            int checkpointX = Integer.parseInt(parts[0].trim());
            int checkpointY = Integer.parseInt(parts[1].trim());
            int checkpointZ = Integer.parseInt(parts[2].trim());

            // Check a 3x3 area around the checkpoint location
            for (int xOffset = -1; xOffset <= 1; xOffset++) {;
                for (int zOffset = -1; zOffset <= 1; zOffset++) {

                    if (plugin.DEBUG) {
                        System.out.println("xOffset " + xOffset);
                        System.out.println("zOffset " + zOffset);

                        System.out.println("PlayerX " + playerX);
                        System.out.println("PlayerY " + playerY);
                        System.out.println("PlayerZ " + playerZ);

                        System.out.println("CheckpointX " + checkpointX);
                        System.out.println("CheckpointY " + checkpointY);
                        System.out.println("CheckpointZ " + checkpointZ);
                    }

                    if (playerX == checkpointX + xOffset && playerY == checkpointY && playerZ == checkpointZ + zOffset) {
                        // Check if the player is standing on the checkpoint block type
                        if (playerLocation.getBlock().getType() == checkpointBlockType || playerLocation.getBlock().getType() == primeCheckpointBlockType) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    private boolean isPlayerOnPrimeCheckpoint(Player player, Location playerLocation, String parkour) {
        int playerX = playerLocation.getBlockX();
        int playerY = playerLocation.getBlockY();
        int playerZ = playerLocation.getBlockZ();

        // Get the checkpoint block type
        Material checkpointBlockType = ParkourManager.getCheckpointBlock(plugin, parkour);
        Material primeCheckpointBlockType = ParkourManager.getPrimeCheckpointBlock(plugin, parkour);

        // Get the list of checkpoint locations from the parkour configuration
        FileConfiguration config = ParkourManager.getParkourConfig(plugin, parkour);
        if (config == null) return false;

        List<String> checkpoints = config.getStringList("checkpoints");
        if (checkpoints.isEmpty()) return false;

        for (String checkpointStr : checkpoints) {
            String[] parts = checkpointStr.split(",");
            if (parts.length != 3) continue;

            int checkpointX = Integer.parseInt(parts[0].trim());
            int checkpointY = Integer.parseInt(parts[1].trim());
            int checkpointZ = Integer.parseInt(parts[2].trim());

            // Check a 3x3 area around the checkpoint location
            for (int xOffset = -1; xOffset <= 1; xOffset++) {;
                for (int zOffset = -1; zOffset <= 1; zOffset++) {

                    if (plugin.DEBUG) {
                        System.out.println("xOffset " + xOffset);
                        System.out.println("zOffset " + zOffset);

                        System.out.println("PlayerX " + playerX);
                        System.out.println("PlayerY " + playerY);
                        System.out.println("PlayerZ " + playerZ);

                        System.out.println("CheckpointX " + checkpointX);
                        System.out.println("CheckpointY " + checkpointY);
                        System.out.println("CheckpointZ " + checkpointZ);
                    }

                    if (playerX == checkpointX + xOffset && playerY == checkpointY && playerZ == checkpointZ + zOffset) {
                        // Check if the player is standing on the checkpoint block type
                        if (playerLocation.getBlock().getType() == primeCheckpointBlockType) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    private void mark3x3CheckpointCompleted(Player player, Location checkpointLocation, String parkour) {
        int baseX = checkpointLocation.getBlockX();
        int baseY = checkpointLocation.getBlockY();
        int baseZ = checkpointLocation.getBlockZ();

        Material checkpointBlock = ParkourManager.getCheckpointBlock(plugin, parkour);
        Material primeCheckpointBlock = ParkourManager.getPrimeCheckpointBlock(plugin, parkour);

        // Iterate through all blocks in the 3x3 area and mark only checkpoint blocks as completed
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                Location blockLocation = new Location(checkpointLocation.getWorld(), baseX + xOffset, baseY - 1, baseZ + zOffset);
                Material blockType = blockLocation.getBlock().getType();
                if (blockType == checkpointBlock || blockType == primeCheckpointBlock) {
                    PlayerData.markCheckpointCompleted(plugin, player.getUniqueId(), blockLocation, parkour);
                }
            }
        }
    }

    private void startCountdown(FewerParkour plugin, Player player, String parkour) {
        for (int i = 3; i >= -1; i--) {
            final int count = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (count > -1) {
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<#00FF00>Teleport To Spawn: <bold><underlined>" + count));
                } else {
                    ParkourData.spawnParkour(plugin, player, parkour);
                }
            }, (3 - i) * 20L);
        }
    }
}
