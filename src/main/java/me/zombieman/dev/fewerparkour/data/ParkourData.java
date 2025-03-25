package me.zombieman.dev.fewerparkour.data;

import com.destroystokyo.paper.ParticleBuilder;
import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.commands.ParkourCmd;
import me.zombieman.dev.fewerparkour.listeners.CheckpointsParkour;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class ParkourData {

    public static void createParkour(FewerParkour plugin, String name, String materialStr, Player player) {

        if (Material.getMaterial(materialStr.toUpperCase()) == null) {
            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                </strikethrough>
                    <bold>ERROR</bold>
                    %s is not a valid block!
                    <strikethrough>                                                </strikethrough>""".formatted(materialStr));

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (ParkourManager.parkourExists(plugin, name)) {
            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                   </strikethrough>
                    <#FF0000>The parkour %s already exists!
                    '/parkour list' to view all valid parkours!
                    <strikethrough>                                                   </strikethrough>""".formatted(name));
            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        ParkourManager.createFile(plugin, name);

        Component message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                         </strikethrough>
                The parkour <bold><underlined>%s</underlined></bold> was successfully created!
                You are now in selecting mode!
                <strikethrough>                                                          </strikethrough>
                                    
                <gray><bold><underlined>Info: </underlined></bold>
                 <bold>|</bold> Selecting mode means that you can select checkpoints
                 <bold>|</bold> by breaking the blocks you want, the player has to
                 <bold>|</bold> complete all checkpoints to be able to win the parkour.
                </gray>
                                    
                <#FF0000><strikethrough>                                                         </strikethrough>
                 <bold>|</bold> The last checkpoint you set will be the finish.
                 <bold>|</bold> Checkpoints will works in <bold>2x2</bold>.
                                    
                <bold><underlined>How To Exit Selection Mode: </underlined></bold>
                 <bold>|</bold> You exit selection mode with <bold><underlined>/parkour exit.</underlined></bold>
                <strikethrough>                                                         </strikethrough>""".formatted(name));

        PlayerData.createFile(plugin, player.getUniqueId());

        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("parkourSelectionMode", name);
        PlayerData.savePlayerData(plugin, player.getUniqueId());


        ItemStack material = new ItemStack(Material.getMaterial(materialStr.toUpperCase()));

        ParkourManager.getParkourConfig(plugin, name).set("Checkpoint Block", material.getType().toString());
        ParkourManager.saveParkourConfig(plugin, name, ParkourManager.getParkourConfig(plugin, name));

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    public static void setParkourSpawn(FewerParkour plugin, String name, Player player) {

        if (!ParkourManager.parkourExists(plugin, name)) {
            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                   </strikethrough>
                    <#FF0000>The parkour %s doesn't exist!
                    '/parkour list' to view all valid parkours!
                    <strikethrough>                                                   </strikethrough>""".formatted(name));
            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        ParkourManager.createFile(plugin, name);

        String loc = (int) player.getLocation().getX() + ", " + (int) player.getLocation().getY() + ", " + (int) player.getLocation().getZ();

        Component message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                                </strikethrough>
                You successfully added a spawn to the <bold><underlined>%s</underlined></bold> parkour!
                Location: %s
                <strikethrough>                                                                </strikethrough>""".formatted(name, loc));

        ParkourManager.getParkourConfig(plugin, name).set("Spawn", player.getLocation());
        ParkourManager.saveParkourConfig(plugin, name, ParkourManager.getParkourConfig(plugin, name));

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    public static void deleteParkour(FewerParkour plugin, String name, Player player) {

        if (!ParkourManager.parkourExists(plugin, name)) {
            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    The parkour %s doesn't exist!
                    '/parkour list' to view all valid parkours!
                    <strikethrough>                                                      </strikethrough>""".formatted(name));

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }


        FileConfiguration config = ParkourManager.getParkourConfig(plugin, name);
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }
        ParkourManager.saveParkourConfig(plugin, name, config);

        ParkourManager.deleteFile(plugin, name);

        Component message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                      </strikethrough>
                The parkour %s was successfully deleted!
                <strikethrough>                                                      </strikethrough>""".formatted(name));

        PlayerData.createFile(plugin, player.getUniqueId());

        if (PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("parkourSelectionMode") != null) {
            exitParkour(plugin, player, true);
        }

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerData.getPlayerDataConfig(plugin, onlinePlayer).set("completedCheckpoints." + name, null);
            PlayerData.getPlayerDataConfig(plugin, onlinePlayer).set("latestCheckpointLocation." + name, null);
            PlayerData.savePlayerData(plugin, onlinePlayer.getUniqueId());
        }

    }

    public static void listParkour(FewerParkour plugin, Player player) {
        List<String> parkours = ParkourManager.getAllParkours(plugin);

        Component message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                      </strikethrough>
                <underlined>Parkour List:</underlined>
                <bold>%s
                <strikethrough>                                                      </strikethrough>""".formatted(String.join(", ", parkours)));

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    public static void exitParkour(FewerParkour plugin, Player player, boolean isFromDelete) {
        Component message;

        if (PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("parkourSelectionMode") == null) {
            if (isFromDelete) return;

            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>FAIL
                    </bold>You aren't in the selection mode!
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        String parkour = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("parkourSelectionMode");

        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("parkourSelectionMode", null);
        PlayerData.savePlayerData(plugin, player.getUniqueId());

        if (isFromDelete) return;

        message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                      </strikethrough>
                Successfully exited %s parkour selection mode!
                <strikethrough>                                                      </strikethrough>""".formatted(parkour));


        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
    }

    public static void leaveParkour(FewerParkour plugin, Player player) {
        Component message;

        String parkour = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("activeParkour");
        List<String> locations = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getStringList("completedCheckpoints." + parkour);

        if (parkour == null) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>FAIL</bold>
                    You aren't in a parkour.
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }


        Location spawn = ParkourManager.getParkourConfig(plugin, parkour).getLocation("Spawn");

        if (spawn == null) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>ERROR</bold>
                    There isn't a spawn set for this parkour!
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        locations.clear();

        player.setLevel(PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getInt("Start Level." + parkour));

        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("completedCheckpoints." + parkour, locations);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("amountOfCheckpointsPassed." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("latestCheckpointLocation." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("latestCheckpointLocationWhereYouCheckTheLocation." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("Failed Amount." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("startTime", null);

        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("Start Level." + parkour, null);
        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("Level." + parkour, null);

        PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).set("activeParkour", null);
        PlayerData.savePlayerData(plugin, player.getUniqueId());

        PlayerData.clearCompletedCheckpoints(plugin, player.getUniqueId(), parkour);

        message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                      </strikethrough>
                Successfully left %s parkour!
                <strikethrough>                                                      </strikethrough>""".formatted(parkour));

//        String start = ParkourManager.getParkourConfig(plugin, parkour).getString("start");
//        if (start != null) {
//            String[] coords = start.split(",");
//            int startX = Integer.parseInt(coords[0]);
//            int startY = Integer.parseInt(coords[1]);
//            int startZ = Integer.parseInt(coords[2]);
//            String string = ParkourManager.getParkourConfig(plugin, parkour).getString("world");
//
//            if (string != null) {
//                player.teleport(new Location(Bukkit.getWorld(string), startX, startY, startZ));
//            }
//        }

        player.sendMessage(message);
        player.teleport(spawn);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }
    public static void respawn(FewerParkour plugin, Player player) {
        Component message;

        String activeParkour = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("activeParkour");

        if (activeParkour == null) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>FAIL</bold>
                    You aren't in a parkour.
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        UUID playerUUID = player.getUniqueId();

        // Retrieve the latest checkpoint location string
        String latestCheckpointString = PlayerData.getPlayerDataConfig(plugin, playerUUID).getString("latestCheckpointLocation." + activeParkour);

        if (latestCheckpointString != null) {
            // Parse the checkpoint string
            String[] parts = latestCheckpointString.split(",");
            if (parts.length == 4) {
                String worldName = parts[0];
                double checkpointX = Double.parseDouble(parts[1]);
                double checkpointY = Double.parseDouble(parts[2]);
                double checkpointZ = Double.parseDouble(parts[3]);

                Location latestCheckpointLocation = new Location(Bukkit.getWorld(worldName), checkpointX, checkpointY, checkpointZ);

                latestCheckpointLocation.setPitch(player.getLocation().getPitch());
                latestCheckpointLocation.setYaw(player.getLocation().getYaw());
                latestCheckpointLocation.setDirection(player.getLocation().getDirection());

                int failedAmount = PlayerData.getPlayerDataConfig(plugin, playerUUID).getInt("Failed Amount." + activeParkour);

                PlayerData.getPlayerDataConfig(plugin, playerUUID).set("Failed Amount." + activeParkour, failedAmount + 1);
                PlayerData.savePlayerData(plugin, playerUUID);

                player.sendMessage(MiniMessage.miniMessage().deserialize("""
                            <#FF0000><strikethrough>                                                      </strikethrough>
                            Click Here To Exit The Parkour.
                            <strikethrough>                                                      </strikethrough>""").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/parkour leave")).hoverEvent(HoverEvent.showText(MiniMessage.miniMessage().deserialize("<#FF0000>/parkour leave"))));

                player.teleport(latestCheckpointLocation);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

            }
        }
    }
    public static void addReward(FewerParkour plugin, Player player, String parkour, String command) {

        if (!ParkourManager.parkourExists(plugin, parkour)) {
            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    The parkour %s doesn't exist!
                    '/parkour list' to view all valid parkours!
                    <strikethrough>                                                      </strikethrough>""".formatted(parkour));

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        FileConfiguration parkourConfig = ParkourManager.getParkourConfig(plugin, parkour);
        List<String> winCommands = parkourConfig.getStringList("Win Commands");

        if (winCommands.contains(command)) {

            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    The %s command already exist in:
                    %s list of win commands!
                    <strikethrough>                                                      </strikethrough>""".formatted(command, parkour));


            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        winCommands.add(command);

        Component message = MiniMessage.miniMessage().deserialize("""
                    <#00FF00><strikethrough>                                                      </strikethrough>
                    %s command was added to the rewards for:
                    %s parkour!
                    <strikethrough>                                                      </strikethrough>""".formatted(command, parkour));


        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);

        parkourConfig.set("Win Commands", winCommands);
        ParkourManager.saveParkourConfig(plugin, parkour, parkourConfig);

    }
    public static void removeReward(FewerParkour plugin, Player player, String parkour, String command) {

        if (!ParkourManager.parkourExists(plugin, parkour)) {
            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    The parkour %s doesn't exist!
                    '/parkour list' to view all valid parkours!
                    <strikethrough>                                                      </strikethrough>""".formatted(parkour));

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        FileConfiguration parkourConfig = ParkourManager.getParkourConfig(plugin, parkour);
        List<String> winCommands = parkourConfig.getStringList("Win Commands");

        if (!winCommands.contains(command)) {

            Component message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    %s is not a valid command in the:
                    %s list of win commands!
                    <strikethrough>                                                      </strikethrough>""".formatted(command, parkour));


            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
            return;
        }

        winCommands.remove(command);

        Component message = MiniMessage.miniMessage().deserialize("""
                    <#00FF00><strikethrough>                                                      </strikethrough>
                    %s command was removed from the rewards for:
                    %s parkour!
                    <strikethrough>                                                      </strikethrough>""".formatted(command, parkour));


        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

        parkourConfig.set("Win Commands", winCommands);
        ParkourManager.saveParkourConfig(plugin, parkour, parkourConfig);

    }

    public static void spawnParkour(FewerParkour plugin, Player player, String parkour) {

        Component message;

        Location spawn = ParkourManager.getParkourConfig(plugin, parkour).getLocation("Spawn");

        if (spawn == null) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>ERROR</bold>
                    There isn't a spawn set for this parkour!
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                      </strikethrough>
                Successfully teleported back to spawn!
                <strikethrough>                                                      </strikethrough>""");

        player.sendMessage(message);
        player.teleport(spawn);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    public static void replaceCheckpointBlocks(FewerParkour plugin, String parkourName, String newBlockStr, String primeCheckpoint, Player player) {

        Component message;

        if (!ParkourManager.parkourExists(plugin, parkourName)) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    The parkour %s doesn't exist!
                    '/parkour list' to view all valid parkours!
                    <strikethrough>                                                      </strikethrough>""".formatted(parkourName));

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        FileConfiguration config = ParkourManager.getParkourConfig(plugin, parkourName);
        if (config == null) return;

        String worldStr = config.getString("world");

        if (worldStr == null) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>ERROR</bold>
                    There isn't a world set for this parkour!
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        World world = plugin.getServer().getWorld(worldStr);
        if (world == null) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>ERROR</bold>
                    The world set for this parkour is null!
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        Material newBlock = Material.getMaterial(newBlockStr);
        if (newBlock == null) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FF0000><strikethrough>                                                      </strikethrough>
                    <bold>ERROR</bold>
                    %s is not a valid block!
                    <strikethrough>                                                      </strikethrough>""".formatted(newBlockStr));

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        Material checkpointBlock = ParkourManager.getCheckpointBlock(plugin, parkourName);
        Material primeCheckpointBlock = ParkourManager.getPrimeCheckpointBlock(plugin, parkourName);

        if (checkpointBlock == newBlock) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#FFFF00><strikethrough>                                                      </strikethrough>
                    <bold>INFO</bold>
                    Wasn't able to replace the checkpoint blocks since
                    they are the same as the current blocks.
                    
                    <bold><underlined>Solution:</underlined></bold>
                     <bold>|</bold> This could because the replaced block
                     <bold>|</bold> is the same as the current blocks.
                    <strikethrough>                                                      </strikethrough>""");

            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        List<String> checkpoints = config.getStringList("checkpoints");

        String start = config.getString("start");

        if (!primeCheckpoint.equalsIgnoreCase("prime")) {
            if (start != null) {
                String[] coords = start.split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                int z = Integer.parseInt(coords[2]);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                            if (block.getType() == checkpointBlock) {
                                block.setType(newBlock);
                                world.spawnParticle(Particle.LAVA, block.getLocation(), 10);
                                world.playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                            }
                        }
                    }
                }
            }
        }

        if (primeCheckpoint.equalsIgnoreCase("prime")) {
            checkpointBlock = primeCheckpointBlock;
        }


        for (String checkpoint : checkpoints) {
            String[] coords = checkpoint.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                        if (block.getType() == checkpointBlock) {
                            world.spawnParticle(Particle.LAVA, block.getLocation(), 10);
                            world.playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
                            block.setType(newBlock);
                        }
                    }
                }
            }
        }

        message = MiniMessage.miniMessage().deserialize("""
                <#00FF00><strikethrough>                                                                   </strikethrough>
                Successfully replaced all blocks in the %s parkour!
                <strikethrough>                                                                   </strikethrough>""".formatted(parkourName));

        player.sendMessage(message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

        ParkourManager.setCheckpointBlock(plugin, primeCheckpoint, parkourName, newBlock);
    }
}