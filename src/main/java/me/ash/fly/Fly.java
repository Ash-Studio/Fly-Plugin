package me.ash.fly;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Fly extends JavaPlugin implements TabCompleter {

    private final Set<String> flyingPlayers = new HashSet<>();
    private final Map<String, Integer> playerFlightTimes = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("FlyPlugin has been enabled!");

        // Register the command and set the TabCompleter
        PluginCommand flyCommand = this.getCommand("fly");
        if (flyCommand != null) {
            flyCommand.setTabCompleter(this);
        } else {
            getLogger().severe("Fly command not found in plugin.yml!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("FlyPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fly")) {
            if (!sender.hasPermission("FlyPlugin.use")) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage("Usage: /fly [add|remove|list|speed|toggle|time|status|increase|decrease] [player|speed|time]");
                return true;
            }

            String subCommand = args[0];

            if (subCommand.equalsIgnoreCase("add")) {
                if (args.length < 2) {
                    sender.sendMessage("Please specify a player to add.");
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                flyingPlayers.add(player.getName());
                player.setAllowFlight(true);
                sender.sendMessage("Fly mode enabled for " + player.getName());
            } else if (subCommand.equalsIgnoreCase("remove")) {
                if (args.length < 2) {
                    sender.sendMessage("Please specify a player to remove.");
                    return true;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                flyingPlayers.remove(player.getName());
                player.setAllowFlight(false);
                sender.sendMessage("Fly mode disabled for " + player.getName());
            } else if (subCommand.equalsIgnoreCase("list")) {
                sender.sendMessage("Flying players: " + flyingPlayers);
            } else if (subCommand.equalsIgnoreCase("speed")) {
                if (args.length < 2) {
                    sender.sendMessage("Please specify a speed between 1 and 10.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
                Player player = (Player) sender;
                try {
                    int speed = Integer.parseInt(args[1]);
                    if (speed < 1 || speed > 10) {
                        sender.sendMessage("Speed must be between 1 and 10.");
                        return true;
                    }
                    player.setFlySpeed(speed / 10.0f);
                    sender.sendMessage("Fly speed set to " + speed);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid speed. Please specify a number between 1 and 10.");
                }
            } else if (subCommand.equalsIgnoreCase("toggle")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
                Player player = (Player) sender;
                if (flyingPlayers.contains(player.getName())) {
                    flyingPlayers.remove(player.getName());
                    player.setAllowFlight(false);
                    sender.sendMessage("Fly mode disabled.");
                } else {
                    flyingPlayers.add(player.getName());
                    player.setAllowFlight(true);
                    sender.sendMessage("Fly mode enabled.");
                }
            } else if (subCommand.equalsIgnoreCase("time")) {
                if (args.length < 2) {
                    sender.sendMessage("Please specify a time in minutes.");
                    return true;
                }
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
                Player player = (Player) sender;
                try {
                    int time = Integer.parseInt(args[1]);
                    playerFlightTimes.put(player.getName(), time);
                    sender.sendMessage("Fly time set to " + time + " minutes.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid time. Please specify a number in minutes.");
                }
            } else if (subCommand.equalsIgnoreCase("status")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
                Player player = (Player) sender;
                int speed = (int) (player.getFlySpeed() * 10);
                int time = playerFlightTimes.getOrDefault(player.getName(), 0);
                sender.sendMessage("Fly status: \nSpeed: " + speed + "\nRemaining Time: " + time + " minutes\nFlying players: " + flyingPlayers);
            } else if (subCommand.equalsIgnoreCase("increase")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
                Player player = (Player) sender;
                int speed = (int) (player.getFlySpeed() * 10);
                if (speed < 10) {
                    player.setFlySpeed((speed + 1) / 10.0f);
                    sender.sendMessage("Fly speed increased to " + (speed + 1));
                } else {
                    sender.sendMessage("Fly speed is already at maximum.");
                }
            } else if (subCommand.equalsIgnoreCase("decrease")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("This command can only be run by a player.");
                    return true;
                }
                Player player = (Player) sender;
                int speed = (int) (player.getFlySpeed() * 10);
                if (speed > 1) {
                    player.setFlySpeed((speed - 1) / 10.0f);
                    sender.sendMessage("Fly speed decreased to " + (speed - 1));
                } else {
                    sender.sendMessage("Fly speed is already at minimum.");
                }
            } else {
                sender.sendMessage("Unknown sub-command.");
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("fly")) {
            if (args.length == 1) {
                return Arrays.asList("add", "remove", "list", "speed", "toggle", "time", "status", "increase", "decrease");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                } else if (args[0].equalsIgnoreCase("speed") || args[0].equalsIgnoreCase("time")) {
                    return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
                }
            }
        }
        return null;
    }
}
