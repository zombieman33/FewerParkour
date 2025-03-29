package me.zombieman.dev.fewerparkour.manager;

import me.zombieman.dev.fewerparkour.FewerParkour;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardManager {

    public static final String DATA_FOLDER_NAME = "leaderboards";

    public static final ConcurrentHashMap<String, FileConfiguration> leaderboardCache = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Long> lastUpdated = new ConcurrentHashMap<>();

    // Initialize the leaderboard folder
    public static void initDataFolder(FewerParkour plugin) {
        File leaderboardFolder = new File(plugin.getDataFolder(), LeaderboardManager.DATA_FOLDER_NAME);
        if (!leaderboardFolder.exists()) {
            leaderboardFolder.mkdirs();
        }
    }

    public static FileConfiguration getLeaderboardDataConfig(FewerParkour plugin, String parkourName) {
        FileConfiguration data = getCached(parkourName);
        long currentTime = System.currentTimeMillis();
        if (data != null && currentTime - lastUpdated.getOrDefault(parkourName, 0L) < 60000) {
            return data;
        }

        File leaderboardFile = getLeaderboardFile(plugin, parkourName);
        if (!leaderboardFile.exists()) {
            createFile(plugin, parkourName);
        }

        data = YamlConfiguration.loadConfiguration(leaderboardFile);
        cache(parkourName, data);
        lastUpdated.put(parkourName, currentTime);

        return data;
    }

    // Create leaderboard file for a specific parkour
    public static void createFile(FewerParkour plugin, String parkourName) {
        File leaderboardFile = getLeaderboardFile(plugin, parkourName);

        if (!leaderboardFile.exists()) {
            try {
                leaderboardFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Save leaderboard data to file
    public static void saveLeaderboardData(FewerParkour plugin, String parkourName) {
        FileConfiguration data = getCached(parkourName);
        File leaderboardFile = getLeaderboardFile(plugin, parkourName);

        try {
            if (data != null) data.save(leaderboardFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Get file for a specific parkour leaderboard
    @NotNull
    private static File getLeaderboardFile(FewerParkour plugin, String parkourName) {
        return new File(plugin.getDataFolder(), DATA_FOLDER_NAME + "/" + parkourName + ".yml");
    }

    // Get cached leaderboard data
    public static FileConfiguration getCached(String parkourName) {
        if (parkourName != null && leaderboardCache.containsKey(parkourName)) {
            return leaderboardCache.get(parkourName);
        }
        return null;
    }

    // Cache leaderboard data
    private static void cache(String parkourName, FileConfiguration data) {
        leaderboardCache.put(parkourName, data);
    }

    public static void cleanupCache(String parkourName) {
        leaderboardCache.remove(parkourName);
    }

    public static void savePlayerTime(FewerParkour plugin, String parkourName, Player player, int time) {
        FileConfiguration data = getLeaderboardDataConfig(plugin, parkourName);
        String playerName = player.getName();

        data.set(playerName, time);

        saveLeaderboardData(plugin, parkourName);
    }

    public static String getTopPlayer(FewerParkour plugin, String parkourName, int position) {
        FileConfiguration data = getLeaderboardDataConfig(plugin, parkourName);
        if (data == null) {
            return "No data available";
        }

        // Sort players by time
        List<Map.Entry<String, Integer>> leaderboardList = new ArrayList<>();
        for (String key : data.getKeys(false)) {
            int time = data.getInt(key);
            leaderboardList.add(new AbstractMap.SimpleEntry<>(key, time));
        }

        leaderboardList.sort(Comparator.comparingInt(Map.Entry::getValue));

        if (position <= 0 || position > leaderboardList.size()) {
            return "Invalid position";
        }

        return leaderboardList.get(position - 1).getKey();
    }

    public static String getPlayerFormattedTime(FewerParkour plugin, String parkourName, String playerName) {
        FileConfiguration data = getLeaderboardDataConfig(plugin, parkourName);

        if (data != null && data.contains(playerName)) {
            int timeInSeconds = data.getInt(playerName);
            return formatTime(timeInSeconds);
        }

        return "n/a";
    }
    private static String formatTime(long elapsedTimeMillis) {
        long elapsedTimeSecs = elapsedTimeMillis / 1000;
        long hours = elapsedTimeSecs / 3600;
        long minutes = (elapsedTimeSecs % 3600) / 60;
        long seconds = elapsedTimeSecs % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d", seconds);
        }
    }


}
