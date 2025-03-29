package me.zombieman.dev.fewerparkour.API;

import me.zombieman.dev.discordlinkingplus.DiscordLinkingPlus;
import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.ParkourData;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;

import java.sql.SQLException;
import java.util.UUID;

public class API {
    private final FewerParkour plugin;

    public API(FewerParkour plugin) {
        this.plugin = plugin;
    }

    public boolean isInParkour(UUID uuid) throws SQLException {
        return ParkourData.isInParkour(plugin, uuid);
    }
    public String getParkour(UUID uuid) throws SQLException {
        return ParkourData.getParkour(plugin, uuid);
    }

}
