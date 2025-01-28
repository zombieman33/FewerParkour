package me.zombieman.dev.fewerparkour.listeners;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SelectorListener implements Listener {

    private FewerParkour plugin;

    public SelectorListener(FewerParkour plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();

        if (PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("parkourSelectionMode") == null) return;

        String parkour = PlayerData.getPlayerDataConfig(plugin, player.getUniqueId()).getString("parkourSelectionMode");

        String checkpointType = "checkpoints";
        FileConfiguration config = ParkourManager.getParkourConfig(plugin, parkour);

        if (config.getString("start") == null) {
            checkpointType = "start";
        }

        int x = event.getBlock().getX();
        int y = event.getBlock().getY();
        int z = event.getBlock().getZ();


        setCheckpointBlocks(event.getBlock(), ParkourManager.getParkourBlock(plugin, parkour));

        ParkourManager.addCheckpoint(plugin, parkour, checkpointType, x, y, z, player.getWorld());

        Component message;
        if (checkpointType.equals("start")) {
            message = MiniMessage.miniMessage().deserialize("""
                    <#00FF00><strikethrough>                                                      </strikethrough>
                    Successfully added a start checkpoint!
                    Continue to add checkpoints around the %s parkour!
                    <strikethrough>                                                      </strikethrough>""".formatted(parkour));
        } else {
            message = MiniMessage.miniMessage().deserialize("""
                    <#00FF00><strikethrough>                                                      </strikethrough>
                    Successfully added a checkpoint!
                    Continue to add checkpoints around the %s parkour!
                    <strikethrough>                                                      </strikethrough>""".formatted(parkour));
        }

        event.setCancelled(true);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        player.sendMessage(message);
    }
    private void setCheckpointBlocks(Block block, Material material) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        for (int xOffset = 0; xOffset < 2; xOffset++) {
            for (int zOffset = 0; zOffset < 2; zOffset++) {
                block.getWorld().getBlockAt(x + xOffset, y, z + zOffset).setType(material);
            }
        }
    }
}