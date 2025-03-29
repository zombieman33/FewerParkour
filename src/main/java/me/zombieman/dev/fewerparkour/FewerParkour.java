package me.zombieman.dev.fewerparkour;

import me.zombieman.dev.discordlinkingplus.DiscordLinkingPlus;
import me.zombieman.dev.fewerparkour.API.API;
import me.zombieman.dev.fewerparkour.commands.ParkourCmd;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.listeners.*;
import me.zombieman.dev.fewerparkour.manager.LeaderboardManager;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import me.zombieman.dev.fewerparkour.manager.TimeManager;
import me.zombieman.dev.fewerparkour.placeholders.LeaderboardPlaceholder;
import me.zombieman.dev.fewerparkour.placeholders.PlayerDataPlaceholder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FewerParkour extends JavaPlugin {

    private List<Listener> eventHandlers = new ArrayList<>();

    private ParkourCmd parkourCmd;

    private PlayerDataPlaceholder playerDataPlaceholder;
    private LeaderboardPlaceholder leaderboardPlaceholder;

    private API api;
    private static FewerParkour instance;

    public boolean DEBUG = false;
    public boolean LINKING = false;

    @Override
    public void onEnable() {
        // Plugin startup logic

        instance = this;

        playerDataPlaceholder = new PlayerDataPlaceholder(this);
        playerDataPlaceholder.register();

        leaderboardPlaceholder = new LeaderboardPlaceholder(this);
        leaderboardPlaceholder.register();

        PlayerData.initDataFolder(this);
        LeaderboardManager.initDataFolder(this);

        PluginCommand plParkourCmd = this.getCommand("parkour");
        this.parkourCmd = new ParkourCmd(this);
        if (plParkourCmd != null) plParkourCmd.setExecutor(this.parkourCmd);

        JoinQuitListener joinQuitListener = new JoinQuitListener(this);
        Bukkit.getPluginManager().registerEvents(joinQuitListener, this);
        eventHandlers.add(joinQuitListener);

        SelectorListener selectorListener = new SelectorListener(this);
        Bukkit.getPluginManager().registerEvents(selectorListener, this);
        eventHandlers.add(selectorListener);

        StartParkour startParkour = new StartParkour(this);
        Bukkit.getPluginManager().registerEvents(startParkour, this);
        eventHandlers.add(startParkour);

        CheckpointsParkour checkpointsParkour = new CheckpointsParkour(this);
        Bukkit.getPluginManager().registerEvents(checkpointsParkour, this);
        eventHandlers.add(checkpointsParkour);

        FailParkour failParkour = new FailParkour(this);
        Bukkit.getPluginManager().registerEvents(failParkour, this);
        eventHandlers.add(failParkour);

        InteractListener interactListener = new InteractListener(this);
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        eventHandlers.add(interactListener);

        XPPickUp xpPickUp = new XPPickUp(this);
        Bukkit.getPluginManager().registerEvents(xpPickUp, this);
        eventHandlers.add(xpPickUp);

        new TimeManager(this);
        new CommandListener(this);
        new DamageListener(this);

        api = new API(this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                checkParkour();
            }
        }, 0, 1);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                timer();
            }
        }, 0, 1);

        if (getServer().getPluginManager().getPlugin("DiscordLinkingPlus") != null) LINKING = true;

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        for (Listener handler : eventHandlers) {
            HandlerList.unregisterAll(handler);
        }
        eventHandlers.clear();

        if (playerDataPlaceholder != null) {
            playerDataPlaceholder.unregister();
        }

    }

    public API getAPI() {
        return api;
    }
    public static FewerParkour getInstance() {
        return instance;
    }

    private void checkParkour() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(FewerParkour.this, player.getUniqueId());

            if (playerDataConfig.getString("activeParkour") != null) {
                String latestCheckpointString = playerDataConfig.getString("latestCheckpointLocation." + playerDataConfig.getString("activeParkour"));

                if (latestCheckpointString != null) {

                    if (player.isFlying() || player.isGliding()) {
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

                            player.teleport(latestCheckpointLocation);
                            player.sendMessage(MiniMessage.miniMessage().deserialize("""
                                    <#FF0000><strikethrough>                                                  </strikethrough>
                                    You were teleported back to your
                                    checkpoint because you tried to fly!
                                    <strikethrough>                                                  </strikethrough>"""));
                        }
                    }
                }
                player.setFlying(false);
            } else {
                player.setWalkSpeed(0.2f);
            }
        }
    }

    private void timer() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            UUID playerUUID = player.getUniqueId();

            FileConfiguration playerDataConfig = PlayerData.getPlayerDataConfig(FewerParkour.this, playerUUID);

            String parkour = playerDataConfig.getString("activeParkour");
            if (parkour != null) {

                if (!ParkourManager.parkourExists(FewerParkour.this, parkour)) {
                    playerDataConfig.set("activeParkour", null);
                    playerDataConfig.set("startTime", null);
                    PlayerData.savePlayerData(FewerParkour.this, playerUUID);
                    return;
                }

                long startTime = playerDataConfig.getLong("startTime");
                long elapsedTime = Instant.now().toEpochMilli() - startTime;

                long minutes = (elapsedTime / 1000) / 60;
                long seconds = (elapsedTime / 1000) % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                int failedAmount = PlayerData.getPlayerDataConfig(FewerParkour.this, playerUUID).getInt("Failed Amount." + parkour);

                player.sendActionBar(MiniMessage.miniMessage().deserialize("<#00FF00>Parkour Time: <underlined><bold>" + timeString + "</underlined><bold> Failed: <underlined><bold>" + failedAmount));
            }
        }
    }
}
