package me.zombieman.dev.fewerparkour.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.manager.LeaderboardManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LeaderboardPlaceholder extends PlaceholderExpansion {

    private final FewerParkour plugin;

    public LeaderboardPlaceholder(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "fewerparkourleaderboard";
    }

    @Override
    public String getAuthor() {
        return "zombieman";
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (identifier.startsWith("name_")) {
            String[] parts = identifier.split("_");

            if (parts.length == 3) {
                String parkourName = parts[1];
                String placeString = parts[2];

                try {
                    int place = Integer.parseInt(placeString);
                    String playerAtPosition = LeaderboardManager.getTopPlayer(plugin, parkourName, place);

                    return playerAtPosition != null && !playerAtPosition.equals("Invalid position") ? playerAtPosition : "n/a";

                } catch (NumberFormatException e) {
                    return "n/a";
                }
            }
        }

        if (identifier.startsWith("time_")) {
            String[] parts = identifier.split("_");

            if (parts.length == 3) {
                String parkourName = parts[1];
                String placeString = parts[2];

                try {
                    int place = Integer.parseInt(placeString);
                    String playerAtPosition = LeaderboardManager.getTopPlayer(plugin, parkourName, place);

                    if (!playerAtPosition.equals("n/a") && !playerAtPosition.equals("Invalid position")) {
                        return LeaderboardManager.getPlayerFormattedTime(plugin, parkourName, playerAtPosition);
                    } else {
                        return "n/a";
                    }

                } catch (NumberFormatException e) {
                    return "n/a";
                }
            }
        }

        return "n/a";
    }
}
