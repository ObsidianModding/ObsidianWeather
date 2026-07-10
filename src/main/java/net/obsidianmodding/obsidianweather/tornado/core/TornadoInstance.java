package net.obsidianmodding.obsidianweather.tornado.core;

import java.util.Random;
import java.util.UUID;
import net.obsidianmodding.obsidianweather.config.TierStats;
import net.obsidianmodding.obsidianweather.tornado.tier.TornadoTier;
import net.obsidianmodding.obsidianweather.tornado.type.TornadoBehavior;
import net.obsidianmodding.obsidianweather.tornado.type.TornadoType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class TornadoInstance {

    private final String id;
    private final TornadoBehavior behavior;
    private final TornadoTier tier;
    private final TierStats stats;
    private final Random random;
    private final long totalLifespanTicks;
    private Location location;
    private Vector heading;
    private long remainingTicks;
    private long ageTicks;
    private boolean stopped;

    public TornadoInstance(Location location, TornadoBehavior behavior, TornadoTier tier, TierStats stats) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.location = location.clone();
        this.behavior = behavior;
        this.tier = tier;
        this.stats = stats;
        this.random = new Random(UUID.randomUUID().getLeastSignificantBits());
        double angle = random.nextDouble() * Math.PI * 2.0;
        this.heading = new Vector(Math.cos(angle), 0.0, Math.sin(angle));
        this.totalLifespanTicks = stats.lifespanTicks();
        this.remainingTicks = totalLifespanTicks;
    }

    public String id() {
        return id;
    }

    public World world() {
        return location.getWorld();
    }

    public Location location() {
        return location.clone();
    }

    public void moveTo(Location location) {
        this.location = location.clone();
    }

    public Vector heading() {
        return heading.clone();
    }

    public void heading(Vector heading) {
        this.heading = heading.clone();
    }

    public TornadoBehavior behavior() {
        return behavior;
    }

    public TornadoType type() {
        return behavior.type();
    }

    public TornadoTier tier() {
        return tier;
    }

    public TierStats stats() {
        return stats;
    }

    public Random random() {
        return random;
    }

    public long remainingTicks() {
        return remainingTicks;
    }

    public long remainingSeconds() {
        return (long) Math.ceil(remainingTicks / 20.0);
    }

    public long ageTicks() {
        return ageTicks;
    }

    public double progress() {
        return 1.0 - (remainingTicks / (double) totalLifespanTicks);
    }

    public boolean tickLifetime() {
        ageTicks++;
        remainingTicks--;
        return !stopped && remainingTicks > 0;
    }

    public void stop() {
        stopped = true;
    }
}
