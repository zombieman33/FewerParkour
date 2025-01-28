package me.zombieman.dev.fewerparkour.manager;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParkourManager {

    public static final String DATA_FOLDER_NAME = "parkourData";

    public static final ConcurrentHashMap<String, FileConfiguration> parkourDataCache = new ConcurrentHashMap<>();

    public static void initDataFolder(FewerParkour plugin) {
        File parkourDataFolder = new File(plugin.getDataFolder(), ParkourManager.DATA_FOLDER_NAME);
        if (!parkourDataFolder.exists()) {
            parkourDataFolder.mkdirs();
        }
    }

    public static FileConfiguration getParkourConfig(FewerParkour plugin, String parkourName) {
        FileConfiguration data = getCached(parkourName);
        if (data != null) return data;

        File parkourFile = getParkourFile(plugin, parkourName);
        if (!parkourFile.exists()) {
            createFile(plugin, parkourName);
        }

        data = YamlConfiguration.loadConfiguration(parkourFile);
        cache(parkourName, data);

        return data;
    }

    public static void createFile(FewerParkour plugin, String parkourName) {
        File parkourFile = getParkourFile(plugin, parkourName);

        if (!parkourFile.exists()) {
            try {
                parkourFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveParkourConfig(FewerParkour plugin, String parkourName, FileConfiguration config) {
        File parkourFile = getParkourFile(plugin, parkourName);

        try {
            config.save(parkourFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getParkourFile(FewerParkour plugin, String parkourName) {
        return new File(plugin.getDataFolder(), DATA_FOLDER_NAME + "/" + parkourName + ".yml");
    }

    public static FileConfiguration getCached(String parkourName) {
        if (parkourName != null && parkourDataCache.containsKey(parkourName)) {
            return parkourDataCache.get(parkourName);
        }
        return null;
    }

    private static void cache(String parkourName, FileConfiguration data) {
        parkourDataCache.put(parkourName, data);
    }

    public static void cleanupCache(String parkourName) {
        parkourDataCache.remove(parkourName);
    }

    public static boolean deleteFile(FewerParkour plugin, String parkourName) {
        File parkourFile = getParkourFile(plugin, parkourName);
        if (parkourFile.exists()) {
            return deleteDirectory(parkourFile);
        }
        return false;
    }

    private static boolean deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        return false; // Failed to delete file
                    }
                }
            }
        }
        return directory.delete(); // Delete the directory itself
    }

    public static boolean parkourExists(FewerParkour plugin, String parkourName) {
        File parkourFile = getParkourFile(plugin, parkourName);
        return parkourFile.exists();
    }

    public static List<String> getAllParkours(FewerParkour plugin) {
        List<String> parkours = new ArrayList<>();
        File parkourDataFolder = new File(plugin.getDataFolder(), DATA_FOLDER_NAME);
        if (parkourDataFolder.exists()) {
            File[] files = parkourDataFolder.listFiles((dir, filename) -> filename.endsWith(".yml"));
            if (files != null) {
                for (File file : files) {
                    parkours.add(file.getName().replace(".yml", ""));
                }
            }
        }
        return parkours;
    }

//    public static boolean isStart(FewerParkour plugin, String parkourName, int x, int y, int z) {
//        FileConfiguration config = getParkourConfig(plugin, parkourName);
//        if (config == null) return false;
//
//        String start = config.getString("start");
//        if (start != null) {
//            String[] startCoords = start.split(",");
//            int startX = Integer.parseInt(startCoords[0]);
//            int startY = Integer.parseInt(startCoords[1]);
//            int startZ = Integer.parseInt(startCoords[2]);
//            if (isWithin2x2Area(x, y, z, startX, startY, startZ)) {
//                return true;
//            }
//        } else {
//            return false;
//        }
//        return false;
//    }
//
//    public static boolean isCheckpoint(FewerParkour plugin, String parkourName, int x, int y, int z) {
//        FileConfiguration config = getParkourConfig(plugin, parkourName);
//        if (config == null) return false;
//
//        List<String> checkpoints = config.getStringList("checkpoints");
//        for (String checkpoint : checkpoints) {
//            String[] checkpointCoords = checkpoint.split(",");
//            int checkpointX = Integer.parseInt(checkpointCoords[0]);
//            int checkpointY = Integer.parseInt(checkpointCoords[1]);
//            int checkpointZ = Integer.parseInt(checkpointCoords[2]);
//            if (isWithin2x2Area(x, y, z, checkpointX, checkpointY, checkpointZ)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    private static boolean isWithin2x2Area(int x, int y, int z, int refX, int refY, int refZ) {
//        return x >= refX && x <= refX + 2 &&
//                y >= refY && y <= refY + 2 &&
//                z >= refZ && z <= refZ + 2;
//    }

    public static Material getParkourBlock(FewerParkour plugin, String parkour) {
        String materialStr = getParkourConfig(plugin, parkour).getString("Checkpoint Block");
        Material material = Material.GOLD_BLOCK;
        if (Material.getMaterial(materialStr) == null) {
            return material;
        }
        return Material.getMaterial(materialStr);
    }

    public static void addCheckpoint(FewerParkour plugin, String name, String type, int x, int y, int z, World world) {
        FileConfiguration config = getParkourConfig(plugin, name);
        if (config == null) return;

        String key = switch (type.toLowerCase()) {
            case "start" -> "start";
            default -> "checkpoints";
        };

        if (key.equals("checkpoints")) {
            List<String> checkpoints = config.getStringList(key);
            checkpoints.add(x + "," + y + "," + z);
            config.set(key, checkpoints);
        } else {
            config.set("world", world.getName());
            config.set(key, x + "," + y + "," + z);
        }

        saveParkourConfig(plugin, name, config);
    }

    public static Material getCheckpointBlock(FewerParkour plugin, String parkour) {
        FileConfiguration config = getParkourConfig(plugin, parkour);
        String blockTypeName = config.getString("Checkpoint Block", "GOLD_BLOCK");
        return Material.matchMaterial(blockTypeName.toUpperCase());
    }
    public static Material getPrimeCheckpointBlock(FewerParkour plugin, String parkour) {
        FileConfiguration config = getParkourConfig(plugin, parkour);
        String blockTypeName = config.getString("Prime Checkpoint Block", "AMETHYST_BLOCK");
        return Material.matchMaterial(blockTypeName.toUpperCase());
    }
    public static void setCheckpointBlock(FewerParkour plugin, String prime, String parkour, Material material) {
        FileConfiguration config = getParkourConfig(plugin, parkour);

        ItemStack itemStack = new ItemStack(material);

        if (prime.equalsIgnoreCase("prime")) {
            config.set("Prime Checkpoint Block", itemStack.getType().toString());
        } else {
            config.set("Checkpoint Block", itemStack.getType().toString());
        }
        saveParkourConfig(plugin, parkour, config);
    }

    public static void reloadConfig(FewerParkour plugin, Player player, String parkourName) {

        if (!parkourExists(plugin, parkourName)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>ERROR</bold>
                    %s parkour doesn't exist.
                    <strikethrough>                                                      </strikethrough>""".formatted(parkourName)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        cleanupCache(parkourName);
        File parkourFile = getParkourFile(plugin, parkourName);
        if (parkourFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(parkourFile);
            cache(parkourName, config);
        }
    }
}
