package net.obsidianmodding.obsidianweather.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import net.obsidianmodding.obsidianweather.ObsidianWeatherPlugin;
import net.obsidianmodding.obsidianweather.tornado.core.TornadoInstance;
import net.obsidianmodding.obsidianweather.tornado.core.TornadoManager;
import net.obsidianmodding.obsidianweather.tornado.movement.GroundLocator;
import net.obsidianmodding.obsidianweather.tornado.tier.TornadoTier;
import net.obsidianmodding.obsidianweather.tornado.type.TornadoType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ObsidianWeatherCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "obsidianweather.admin";

    private final JavaPlugin plugin;
    private final TornadoManager tornadoManager;

    public ObsidianWeatherCommand(JavaPlugin plugin, TornadoManager tornadoManager) {
        this.plugin = plugin;
        this.tornadoManager = tornadoManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("You do not have permission to manage ObsidianWeather.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "spawn" -> spawn(sender, label, args);
            case "stop" -> stop(sender, label, args);
            case "list" -> list(sender);
            case "reload" -> reload(sender);
            default -> {
                sendUsage(sender, label);
                yield true;
            }
        };
    }

    private boolean spawn(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /" + label + " spawn <world> [x z] [type] [rating]");
            return true;
        }
        World world = plugin.getServer().getWorld(args[1]);
        if (world == null) {
            sender.sendMessage("Unknown world: " + args[1]);
            return true;
        }

        int optionIndex = 2;
        double x;
        double z;
        if (args.length >= 4 && isDouble(args[2]) && isDouble(args[3])) {
            x = Double.parseDouble(args[2]);
            z = Double.parseDouble(args[3]);
            optionIndex = 4;
        } else if (sender instanceof Player player) {
            x = player.getLocation().getX();
            z = player.getLocation().getZ();
        } else {
            sender.sendMessage("Console must supply x and z coordinates.");
            return true;
        }

        TornadoType type = null;
        if (args.length > optionIndex) {
            Optional<TornadoType> parsed = TornadoType.parse(args[optionIndex]);
            if (parsed.isEmpty()) {
                sender.sendMessage("Unknown tornado type: " + args[optionIndex]);
                return true;
            }
            type = parsed.get();
        }
        TornadoTier tier = null;
        if (args.length > optionIndex + 1) {
            Optional<TornadoTier> parsed = TornadoTier.parse(args[optionIndex + 1]);
            if (parsed.isEmpty()) {
                sender.sendMessage("Unknown Fujita rating: " + args[optionIndex + 1]);
                return true;
            }
            tier = parsed.get();
        }

        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        if (!world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
            sender.sendMessage("That chunk is not loaded; move closer before spawning a tornado.");
            return true;
        }
        double referenceY = world.getSpawnLocation().getY();
        if (sender instanceof Player player && player.getWorld().equals(world)) {
            referenceY = player.getLocation().getY();
        }
        OptionalInt groundY = GroundLocator.findGroundY(world, blockX, blockZ, referenceY);
        if (groundY.isEmpty()) {
            sender.sendMessage("Could not find open ground near those coordinates.");
            return true;
        }
        Location location = new Location(world, x, groundY.getAsInt(), z);
        Optional<TornadoInstance> spawned = tornadoManager.spawn(location, type, tier, false);
        if (spawned.isEmpty()) {
            sender.sendMessage("Could not spawn a tornado (type disabled or per-world cap reached).");
            return true;
        }
        TornadoInstance tornado = spawned.get();
        sender.sendMessage("Spawned " + tornado.tier().displayName() + " " + tornado.type().configKey()
                + profileSuffix(tornado) + " tornado with id " + tornado.id() + ".");
        return true;
    }

    private boolean stop(CommandSender sender, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage("Usage: /" + label + " stop <id|all>");
            return true;
        }
        int stopped = tornadoManager.stop(args[1]);
        sender.sendMessage(stopped == 0 ? "No matching active tornadoes." : "Stopped " + stopped + " tornado(es)." );
        return true;
    }

    private boolean list(CommandSender sender) {
        List<TornadoInstance> active = tornadoManager.active();
        if (active.isEmpty()) {
            sender.sendMessage("There are no active tornadoes.");
            return true;
        }
        sender.sendMessage("Active tornadoes (" + active.size() + "):");
        for (TornadoInstance tornado : active) {
            Location location = tornado.location();
            sender.sendMessage("- " + tornado.id() + " " + tornado.type().configKey() + "/"
                    + tornado.tier().displayName() + profileSuffix(tornado) + " in " + tornado.world().getName() + " at "
                    + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()
                    + " (" + tornado.remainingSeconds() + "s remaining)");
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (plugin instanceof ObsidianWeatherPlugin obsidianWeather) {
            obsidianWeather.reloadWeatherConfig();
            sender.sendMessage("ObsidianWeather configuration reloaded.");
        } else {
            sender.sendMessage("Configuration reload is unavailable.");
        }
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("/" + label + " spawn <world> [x z] [type] [rating]");
        sender.sendMessage("/" + label + " stop <id|all>");
        sender.sendMessage("/" + label + " list");
        sender.sendMessage("/" + label + " reload");
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!sender.hasPermission(PERMISSION)) {
            return List.of();
        }
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.addAll(List.of("spawn", "stop", "list", "reload"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            plugin.getServer().getWorlds().forEach(world -> suggestions.add(world.getName()));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("stop")) {
            suggestions.add("all");
            tornadoManager.active().forEach(tornado -> suggestions.add(tornado.id()));
        } else if (args[0].equalsIgnoreCase("spawn")) {
            if (args.length == 3 || args.length == 5) {
                Arrays.stream(TornadoType.values()).map(TornadoType::configKey).forEach(suggestions::add);
            } else if (args.length == 4 || args.length == 6) {
                Arrays.stream(TornadoTier.values()).map(TornadoTier::configKey).forEach(suggestions::add);
            }
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return suggestions.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }

    private String profileSuffix(TornadoInstance tornado) {
        if (tornado.type() != TornadoType.STANDARD) {
            return "";
        }
        return " " + tornado.funnelProfile().configKey();
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
