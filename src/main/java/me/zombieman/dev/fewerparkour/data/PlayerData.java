package me.zombieman.dev.fewerparkour.data;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    public static final String DATA_FOLDER_NAME = "playerData";
    public static final ConcurrentHashMap<UUID, FileConfiguration> playerDataCache = new ConcurrentHashMap<>();

    public static void initDataFolder(FewerParkour plugin) {
        File playerDataFolder = new File(plugin.getDataFolder(), PlayerData.DATA_FOLDER_NAME);
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public static FileConfiguration getPlayerDataConfig(FewerParkour plugin, Player player) {
        return getPlayerDataConfig(plugin, player.getUniqueId());
    }

    public static FileConfiguration getPlayerDataConfig(FewerParkour plugin, UUID uuid) {
        FileConfiguration data = getCached(uuid);
        if (data != null) return data;

        File playerFile = getPlayerFile(plugin, uuid);
        if (!playerFile.exists()) {
            createFile(plugin, uuid);
        }

        data = YamlConfiguration.loadConfiguration(playerFile);
        cache(uuid, data);

        return data;
    }

    public static void createFile(FewerParkour plugin, Player player) {
        createFile(plugin, player.getUniqueId());
    }

    public static void createFile(FewerParkour plugin, UUID uuid) {
        File playerFile = getPlayerFile(plugin, uuid);

        if (!playerFile.exists()) {
            try {
                boolean fileCreated = playerFile.createNewFile();
                if (fileCreated) {
                    plugin.getLogger().info("Created player data file for UUID: " + uuid);
                } else {
                    plugin.getLogger().warning("Failed to create player data file for UUID: " + uuid);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating player data file for UUID: " + uuid);
                e.printStackTrace();
            }
        }
    }

    public static void savePlayerData(FewerParkour plugin, UUID playerUUID) {
        FileConfiguration data = getCached(playerUUID);
        File playerFile = getPlayerFile(plugin, playerUUID);

        try {
            if (data != null) data.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static File getPlayerFile(FewerParkour plugin, UUID playerUUID) {
        return new File(plugin.getDataFolder(), DATA_FOLDER_NAME + "/" + playerUUID + ".yml");
    }

    public static FileConfiguration getCached(UUID uuid) {
        if (uuid != null && playerDataCache.containsKey(uuid)) {
            return playerDataCache.get(uuid);
        }
        return null;
    }

    private static void cache(UUID uuid, FileConfiguration data) {
        playerDataCache.put(uuid, data);
    }

    public static void cleanupCache(Player player) {
        playerDataCache.remove(player.getUniqueId());
    }

    public static boolean hasCompletedCheckpoint(FewerParkour plugin, UUID playerUUID, Location checkpointLocation, String parkour) {
        String locationString = locationToString(checkpointLocation);
        List<String> checkpoints = getPlayerDataConfig(plugin, playerUUID).getStringList("completedCheckpoints." + parkour);
        return checkpoints.contains(locationString);
    }

    public static void markCheckpointCompleted(FewerParkour plugin, UUID playerUUID, Location checkpointLocation, String parkour) {
        String locationString = locationToString(checkpointLocation);
        List<String> checkpoints = getPlayerDataConfig(plugin, playerUUID).getStringList("completedCheckpoints." + parkour);
        if (!checkpoints.contains(locationString)) {
            checkpoints.add(locationString);
            getPlayerDataConfig(plugin, playerUUID).set("completedCheckpoints." + parkour, checkpoints);
            savePlayerData(plugin, playerUUID);
        }
    }

    public static void clearCompletedCheckpoints(FewerParkour plugin, UUID playerUUID, String parkour) {
        getPlayerDataConfig(plugin, playerUUID).set("completedCheckpoints." + parkour, new ArrayList<>());
        savePlayerData(plugin, playerUUID);
    }

    public static String locationToString(Location location) {
        return String.format("%s,%d,%d,%d", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static List<String> getAllPlayerDataFiles(FewerParkour plugin) {
        File playerDataFolder = new File(plugin.getDataFolder(), PlayerData.DATA_FOLDER_NAME);
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
            return Collections.emptyList();
        }

        File[] playerFiles = playerDataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (playerFiles == null) {
            return Collections.emptyList();
        }

        List<String> playerFileNames = new ArrayList<>();
        for (File file : playerFiles) {
            String fileName = file.getName();
            if (fileName.endsWith(".yml")) {
                playerFileNames.add(fileName.substring(0, fileName.length() - 4));
            }
        }
        return playerFileNames;
    }

    public static boolean playerDataFileExists(FewerParkour plugin, String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);
            File playerFile = getPlayerFile(plugin, uuid);
            return playerFile.exists();
        } catch (IllegalArgumentException e) {
            return false; // Invalid UUID format
        }
    }

    public static void reloadConfig(FewerParkour plugin, Player player, String uuidString) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<#FF0000>Invalid UUID format: " + uuidString));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        cleanupCache(player);
        File playerFile = getPlayerFile(plugin, uuid);
        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            cache(uuid, config);
        } else {
            player.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>ERROR</bold>
                    %s player data file doesn't exist.
                    <strikethrough>                                                      </strikethrough>""".formatted(uuid.toString())));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
    public static boolean resetPlayer(FewerParkour plugin, Player player, String parkour, String uuidString) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<#FF0000>Invalid UUID format: " + uuidString));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        File playerFile = getPlayerFile(plugin, uuid);
        if (playerFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

            // Check if player is currently on the specified parkour
            String activeParkour = config.getString("activeParkour");
            if (activeParkour != null && activeParkour.equalsIgnoreCase(parkour)) {
                List<String> locations = PlayerData.getPlayerDataConfig(plugin, uuid).getStringList("completedCheckpoints." + parkour);

                // Clear checkpoint data
                locations.clear();

                // Reset various data fields
                PlayerData.getPlayerDataConfig(plugin, uuid).set("completedCheckpoints." + parkour, locations);
                PlayerData.getPlayerDataConfig(plugin, uuid).set("amountOfCheckpointsPassed." + parkour, null);
                PlayerData.getPlayerDataConfig(plugin, uuid).set("latestCheckpointLocation." + parkour, null);
                PlayerData.getPlayerDataConfig(plugin, uuid).set("Failed Amount." + parkour, null);
                PlayerData.getPlayerDataConfig(plugin, uuid).set("startTime", null);
                PlayerData.getPlayerDataConfig(plugin, uuid).set("Start Level." + parkour, null);
                PlayerData.getPlayerDataConfig(plugin, uuid).set("Level." + parkour, null);
                PlayerData.getPlayerDataConfig(plugin, uuid).set("activeParkour", null);
                PlayerData.savePlayerData(plugin, uuid);

                PlayerData.clearCompletedCheckpoints(plugin, uuid, parkour);
            }

            // Teleport player only if necessary
            Location spawn = ParkourManager.getParkourConfig(plugin, parkour).getLocation("Spawn");
            if (spawn != null && (activeParkour != null && activeParkour.equalsIgnoreCase(parkour))) {
                Bukkit.getPlayer(uuid).teleport(spawn);
                Bukkit.getPlayer(uuid).playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return true;
            }

        }

        return false;
    }


    public static long getBestTime(FewerParkour plugin, UUID uuid) {
        return getPlayerDataConfig(plugin, uuid).getLong("stats.best_time");
    }
    public static void setBestTimeIfNeeded(FewerParkour plugin, UUID uuid, long timeInSeconds) {

        if (getBestTime(plugin, uuid) > timeInSeconds || getBestTime(plugin, uuid) == 0) {
            PlayerData.getPlayerDataConfig(plugin, uuid).set("stats.best_time", timeInSeconds);
            PlayerData.savePlayerData(plugin, uuid);
        }
    }

}