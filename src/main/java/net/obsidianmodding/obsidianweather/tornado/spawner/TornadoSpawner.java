package net.obsidianmodding.obsidianweather.tornado.spawner;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.tornado.core.TornadoManager;
import org.bukkit.HeightMap;
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
        if (!config.affects(world) || !world.hasStorm() || !world.isThundering()
                || tornadoManager.count(world) >= config.maxConcurrentPerWorld()
                || random.nextDouble() >= config.spawnChancePerCheck()) {
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
        int y = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE) + 1;
        Location spawn = new Location(world, x + 0.5, y, z + 0.5);
        if (world.getWorldBorder().isInside(spawn)) {
            tornadoManager.spawn(spawn, null, null, true);
        }
    }
}
