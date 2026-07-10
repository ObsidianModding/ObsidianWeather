package net.obsidianmodding.obsidianweather.tornado.spawner;

import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Supplier;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.tornado.core.TornadoManager;
import net.obsidianmodding.obsidianweather.tornado.movement.GroundLocator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TornadoSpawner {

    private final JavaPlugin plugin;
    private final Supplier<WeatherConfig> configSupplier;
    private final TornadoManager tornadoManager;
    private final Random random = new Random();
    private int ticksUntilCheck;

    public TornadoSpawner(
            JavaPlugin plugin,
            Supplier<WeatherConfig> configSupplier,
            TornadoManager tornadoManager
    ) {
        this.plugin = plugin;
        this.configSupplier = configSupplier;
        this.tornadoManager = tornadoManager;
    }

    public void tick() {
        if (--ticksUntilCheck > 0) {
            return;
        }
        WeatherConfig config = configSupplier.get();
        ticksUntilCheck = config.spawnCheckIntervalTicks();
        for (World world : plugin.getServer().getWorlds()) {
            attemptWorldSpawn(world, config);
        }
    }

    private void attemptWorldSpawn(World world, WeatherConfig config) {
        if (!config.affects(world)
                || tornadoManager.count(world) >= config.maxConcurrentPerWorld()
                || random.nextDouble() >= spawnChance(world, config)) {
            return;
        }
        List<Player> players = world.getPlayers().stream().filter(Player::isValid).toList();
        if (players.isEmpty()) {
            return;
        }
        Player anchor = players.get(random.nextInt(players.size()));
        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = config.minimumSpawnDistance()
                + random.nextDouble() * (config.maximumSpawnDistance() - config.minimumSpawnDistance());
        int x = (int) Math.floor(anchor.getLocation().getX() + Math.cos(angle) * distance);
        int z = (int) Math.floor(anchor.getLocation().getZ() + Math.sin(angle) * distance);
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }
        OptionalInt groundY = GroundLocator.findGroundY(world, x, z, anchor.getLocation().getY());
        if (groundY.isEmpty()) {
            return;
        }
        Location spawn = new Location(world, x + 0.5, groundY.getAsInt(), z + 0.5);
        if (world.getWorldBorder().isInside(spawn)) {
            tornadoManager.spawn(spawn, null, null, true);
        }
    }

    private double spawnChance(World world, WeatherConfig config) {
        double chance = config.spawnChancePerCheck();
        if (world.getEnvironment() == World.Environment.NETHER) {
            chance *= config.netherFirenadoChanceMultiplier();
        }
        return Math.min(1.0, chance);
    }
}
