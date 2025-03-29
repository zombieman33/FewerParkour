package me.zombieman.dev.fewerparkour.commands;

import me.zombieman.dev.fewerparkour.FewerParkour;
import me.zombieman.dev.fewerparkour.data.ParkourData;
import me.zombieman.dev.fewerparkour.data.PlayerData;
import me.zombieman.dev.fewerparkour.listeners.SelectorListener;
import me.zombieman.dev.fewerparkour.manager.ParkourManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParkourCmd implements CommandExecutor, TabCompleter {

    private FewerParkour plugin;

    public ParkourCmd(FewerParkour plugin) {
        this.plugin = plugin;
    }

    String adminPermission = "fewerparkour.command.parkour.admin";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if (args.length >= 1) {

            if (args[0].equalsIgnoreCase("create")) {
                if (!player.hasPermission("fewerparkour.command.parkour.create") || !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                if (args[1].isEmpty() || args[1].isBlank() || args[2] == null || args[2].isBlank()) {
                    player.sendMessage(ChatColor.RED + "Usage: /parkour create <parkour> <checkpoint material>");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                ParkourData.createParkour(plugin, args[1], args[2], player);

            } else if (args[0].equalsIgnoreCase("delete")) {
                if (!player.hasPermission("fewerparkour.command.parkour.delete") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                if (args[1].isEmpty() || args[1].isBlank()) {

                    player.sendMessage(ChatColor.RED + "Usage: /parkour delete <parkour>");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                ParkourData.deleteParkour(plugin, args[1], player);

            } else if (args[0].equalsIgnoreCase("list")) {
                if (!player.hasPermission("fewerparkour.command.parkour.list") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                ParkourData.listParkour(plugin, player);

            } else if (args[0].equalsIgnoreCase("leave")) {
                if (!player.hasPermission("fewerparkour.command.parkour.leave") && player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                ParkourData.leaveParkour(plugin, player);
            } else if (args[0].equalsIgnoreCase("exit")) {
                if (!player.hasPermission("fewerparkour.command.parkour.exit") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                ParkourData.exitParkour(plugin, player, false);
            } else if (args[0].equalsIgnoreCase("setspawn")) {
                if (!player.hasPermission("fewerparkour.command.parkour.setspawn") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                if (args[1].isEmpty() || args[1].isBlank()) {
                    player.sendMessage(ChatColor.RED + "Usage: /parkour setspawn <parkour>");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                ParkourData.setParkourSpawn(plugin, args[1], player);

            } else if (args[0].equalsIgnoreCase("replace")) {
                if (!player.hasPermission("fewerparkour.command.parkour.replace") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                if (args[1].isEmpty() || args[1].isBlank() || args[2] == null || args[2].isBlank() || args[3] == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /parkour replace <parkour> <block> <prime or normal>");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                if (args[3].equalsIgnoreCase("prime") || args[3].equalsIgnoreCase("normal")) {
                    ParkourData.replaceCheckpointBlocks(plugin, args[1], args[2], args[3], player);
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /parkour replace <parkour> <block> <prime or normal>");
                }

            } else if (args[0].equalsIgnoreCase("respawn")) {
                if (!player.hasPermission("fewerparkour.command.parkour.respawn") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                ParkourData.respawn(plugin, player);

            } else if (args[0].equalsIgnoreCase("addreward")) {
                if (!player.hasPermission("fewerparkour.command.parkour.addreward") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                if (args[1] == null || args[2] == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /parkour addreward <parkour> <command>");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                String rewardCommand = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                ParkourData.addReward(plugin, player, args[1], rewardCommand);

            } else if (args[0].equalsIgnoreCase("removereward")) {
                if (!player.hasPermission("fewerparkour.command.parkour.removereward") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                if (args[1] == null || args[2] == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /parkour removereward <parkour> <command>");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                String rewardCommand = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                ParkourData.removeReward(plugin, player, args[1], rewardCommand);

            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!player.hasPermission("fewerparkour.command.parkour.reload") && !player.hasPermission(adminPermission)) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }
                if (args[1] == null || args[2] == null) {
                    player.sendMessage(ChatColor.RED + "Usage: /parkour reload <folder> <parkour, all>");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }

                long startTime = System.currentTimeMillis();

                if (args[1].equalsIgnoreCase("ParkourData")) {
                    if (args[2].equalsIgnoreCase("all")) {
                        for (String parkourName : ParkourManager.getAllParkours(plugin)) {
                            ParkourManager.reloadConfig(plugin, player, parkourName);
                        }
                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;

                        player.sendMessage(MiniMessage.miniMessage().deserialize("""
                                <#00FF00><strikethrough>                                                      </strikethrough>
                                Successfully reloaded all parkour files in %s ms!
                                <strikethrough>                                                      </strikethrough>""".formatted(elapsedTime)));

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    } else {
                        ParkourManager.reloadConfig(plugin, player, args[2]);

                        if (!ParkourManager.parkourExists(plugin, args[2])) return false;

                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;

                        player.sendMessage(MiniMessage.miniMessage().deserialize("""
                                <#00FF00><strikethrough>                                                      </strikethrough>
                                Successfully reloaded %s parkour in %s ms!
                                <strikethrough>                                                      </strikethrough>""".formatted(args[1], elapsedTime)));

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                } else if (args[1].equalsIgnoreCase("PlayerData")) {
                    if (args[2].equalsIgnoreCase("all")) {
                        for (String playerDataFiles : PlayerData.getAllPlayerDataFiles(plugin)) {
                            PlayerData.reloadConfig(plugin, player, playerDataFiles);
                        }
                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;

                        player.sendMessage(MiniMessage.miniMessage().deserialize("""
                                <#00FF00><strikethrough>                                                      </strikethrough>
                                Successfully reloaded all player data files in %s ms!
                                <strikethrough>                                                      </strikethrough>""".formatted(elapsedTime)));

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                    } else {
                        PlayerData.reloadConfig(plugin, player, args[2]);

                        if (!PlayerData.playerDataFileExists(plugin, args[2])) return false;

                        long endTime = System.currentTimeMillis();
                        long elapsedTime = endTime - startTime;

                        player.sendMessage(MiniMessage.miniMessage().deserialize("""
                                <#00FF00><strikethrough>                                                      </strikethrough>
                                Successfully reloaded %s player files in %s ms!
                                <strikethrough>                                                      </strikethrough>""".formatted(args[1], elapsedTime)));

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (!player.hasPermission("fewerparkour.command.parkour.reset")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return false;
                }
                long startTime = System.currentTimeMillis();

                int amountOfPlayers = 0;

                for (String playerDataFiles : PlayerData.getAllPlayerDataFiles(plugin)) {
                    if (PlayerData.resetPlayer(plugin, player, args[1], playerDataFiles)) {
                        amountOfPlayers++;
                    }
                }

                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;

                player.sendMessage(MiniMessage.miniMessage().deserialize("""
                        <#00FF00><strikethrough>                                                      </strikethrough>
                        Successfully reset %d players
                        from the %s parkour in %s ms!
                        <strikethrough>                                                      </strikethrough>""".formatted(amountOfPlayers, args[1], elapsedTime)));
                return true;
            }

        } else {

            String usage = "";
            String coma = ", ";

            if (player.hasPermission("fewerparkour.command.parkour.leave") || player.hasPermission(adminPermission)) usage = "leave";

            if (player.hasPermission("fewerparkour.command.parkour.list") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "list";
            }
            if (player.hasPermission("fewerparkour.command.parkour.delete") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "delete";
            }
            if (player.hasPermission("fewerparkour.command.parkour.create") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "create";
            }
            if (player.hasPermission("fewerparkour.command.parkour.respawn") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "respawn";
            }
            if (player.hasPermission("fewerparkour.command.parkour.addreward") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "addreward";
            }
            if (player.hasPermission("fewerparkour.command.parkour.removereward") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "removereward";
            }
            if (player.hasPermission("fewerparkour.command.parkour.exit") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "exit";
            }
            if (player.hasPermission("fewerparkour.command.parkour.reload") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "reload";
            }
            if (player.hasPermission("fewerparkour.command.parkour.reset") || player.hasPermission(adminPermission)) {
                if (usage.equals("")) {
                    coma = "";
                }
                usage = usage + coma + "reset";
            }

            player.sendMessage(ChatColor.YELLOW + "Usage: /parkour <%s>".formatted(usage));
        }
        return false;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        Player player = (Player) sender;


        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) {
                if (player.hasPermission("fewerparkour.command.parkour.create") || player.hasPermission(adminPermission)) {
                    completions.add("<parkour>");
                }
            }
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("setspawn") || args[0].equalsIgnoreCase("replace") || args[0].equalsIgnoreCase("addreward") || args[0].equalsIgnoreCase("removereward") || args[0].equalsIgnoreCase("reset")) {
                if (player.hasPermission("fewerparkour.command.parkour.delete") || player.hasPermission(adminPermission) || player.hasPermission("fewerparkour.command.parkour.setspawn") || player.hasPermission("fewerparkour.command.parkour.replace") || player.hasPermission("fewerparkour.command.parkour.addreward") || player.hasPermission("fewerparkour.command.parkour.removereward") || player.hasPermission("fewerparkour.command.parkour.reset")) {
                    completions.addAll(ParkourManager.getAllParkours(plugin));
                }
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("fewerparkour.command.parkour.reload")) {
                    completions.add("PlayerData");
                    completions.add("ParkourData");
                }
            }
        }


        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("addreward")) {
                if (player.hasPermission("fewerparkour.command.parkour.addreward") || player.hasPermission(adminPermission)) {
                    completions.add("<console command to run>");
                }
            }
            if (args[0].equalsIgnoreCase("removereward")) {
                if (player.hasPermission("fewerparkour.command.parkour.removereward") || player.hasPermission(adminPermission)) {
                    if (args[1] != null) {
                        String parkour = args[1];
                        completions.addAll(ParkourManager.getParkourConfig(plugin, parkour).getStringList("Win Commands"));
                    }
                }
            }
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("replace")) {
                if (player.hasPermission("fewerparkour.command.parkour.create") || player.hasPermission(adminPermission) || player.hasPermission("fewerparkour.command.parkour.replace")) {
                    for (Material blocks : Material.values()) {
                        if (blocks.isBlock()) {
                            completions.add(blocks.name());
                        }
                    }
                }
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("fewerparkour.command.parkour.reload") || player.hasPermission(adminPermission)) {
                    if (args[1].equalsIgnoreCase("ParkourData")) {
                        completions.addAll(ParkourManager.getAllParkours(plugin));
                        completions.add("all");
                    } else if (args[1].equalsIgnoreCase("PlayerData")) {
                        completions.add("all");
                        completions.addAll(PlayerData.getAllPlayerDataFiles(plugin));
                    }
                }
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("replace")) {
                if (player.hasPermission("fewerparkour.command.parkour.replace") || player.hasPermission(adminPermission)) {
                    completions.add("normal");
                    completions.add("prime");
                }
            }
        }

        if (args.length == 1) {
            if (player.hasPermission("fewerparkour.command.parkour.create") || player.hasPermission(adminPermission)) {
                completions.add("create");
            }
            if (player.hasPermission("fewerparkour.command.parkour.delete") || player.hasPermission(adminPermission)) {
                completions.add("delete");
            }
            if (player.hasPermission("fewerparkour.command.parkour.list") || player.hasPermission(adminPermission)) {
                completions.add("list");
            }
            if (player.hasPermission("fewerparkour.command.parkour.exit") || player.hasPermission(adminPermission)) {
                completions.add("exit");
            }
            if (player.hasPermission("fewerparkour.command.parkour.leave") || player.hasPermission(adminPermission)) {
                completions.add("leave");
            }
            if (player.hasPermission("fewerparkour.command.parkour.setspawn") || player.hasPermission(adminPermission)) {
                completions.add("setspawn");
            }
            if (player.hasPermission("fewerparkour.command.parkour.replace") || player.hasPermission(adminPermission)) {
                completions.add("replace");
            }
            if (player.hasPermission("fewerparkour.command.parkour.respawn") || player.hasPermission(adminPermission)) {
                completions.add("respawn");
            }
            if (player.hasPermission("fewerparkour.command.parkour.addreward") || player.hasPermission(adminPermission)) {
                completions.add("addreward");
            }
            if (player.hasPermission("fewerparkour.command.parkour.removereward") || player.hasPermission(adminPermission)) {
                completions.add("removereward");
            }
            if (player.hasPermission("fewerparkour.command.parkour.reload") || player.hasPermission(adminPermission)) {
                completions.add("reload");
            }
            if (player.hasPermission("fewerparkour.command.parkour.reset") || player.hasPermission(adminPermission)) {
                completions.add("reset");
            }
        }

        String lastArg = args[args.length - 1].toUpperCase();
        return completions.stream().filter(s -> s.toUpperCase().startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
    }
}