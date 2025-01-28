package me.zombieman.dev.fewerparkour.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.enums.Times;
import me.zombieman.dev.fewerparkour.manager.TimeManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerDataPlaceholder extends PlaceholderExpansion {

    private final FewerParkour plugin;

    public PlayerDataPlaceholder(FewerParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "fewerparkour";
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
        if (identifier.startsWith("playerdata_besttime_")) {
            String timeType = identifier.replace("playerdata_besttime_", "").toUpperCase();

            UUID playerUUID = player.getUniqueId();

            long bestTime = PlayerData.getBestTime(plugin, playerUUID);

//            System.out.println("Best Time: " + bestTime);
//            System.out.println("Time Type: " + timeType);
//            System.out.println("Times Type Value Of: " + Times.valueOf(timeType));
//            System.out.println("Time: " + TimeManager.convert(bestTime, Times.valueOf(timeType)));

            switch (Times.valueOf(timeType)) {
                case SECONDS:
                    return TimeManager.convert(bestTime, Times.SECONDS);
                case MINUTES:
                    return TimeManager.convert(bestTime, Times.MINUTES);
                case HOURS:
                    return TimeManager.convert(bestTime, Times.HOURS);

                case DAYS:
                    return TimeManager.convert(bestTime, Times.DAYS);

                case WEEKS:
                    return TimeManager.convert(bestTime, Times.WEEKS);

                case MONTHS:
                    return TimeManager.convert(bestTime, Times.MONTHS);

                case YEARS:
                    return TimeManager.convert(bestTime, Times.YEARS);

                case FORMATTED:
                    return TimeManager.convert(bestTime, Times.FORMATTED);

                default:
                    return String.valueOf(bestTime);
            }
        }

        return "n/a";
    }

}
