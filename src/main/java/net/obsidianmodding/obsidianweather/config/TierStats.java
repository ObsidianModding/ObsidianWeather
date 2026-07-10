package net.obsidianmodding.obsidianweather.config;

import net.obsidianmodding.obsidianweather.tornado.tier.TornadoTier;
import org.bukkit.configuration.ConfigurationSection;

public record TierStats(
        double radius,
        double movementSpeed,
        int lifespanTicks,
        int pickupWeightLimit,
        double damageMultiplier,
        double particleDensity,
        double spawnWeight
) {
    public TierStats {
        radius = Math.max(1.0, radius);
        movementSpeed = Math.max(0.01, movementSpeed);
        lifespanTicks = Math.max(20, lifespanTicks);
        pickupWeightLimit = Math.max(0, pickupWeightLimit);
        damageMultiplier = Math.max(0.0, damageMultiplier);
        particleDensity = Math.max(0.0, particleDensity);
        spawnWeight = Math.max(0.0, spawnWeight);
    }

    public static TierStats from(ConfigurationSection section, TornadoTier tier) {
        TierStats defaults = defaultsFor(tier);
        if (section == null) {
            return defaults;
        }
        return new TierStats(
                section.getDouble("radius", defaults.radius()),
                section.getDouble("movement-speed", defaults.movementSpeed()),
                secondsToTicks(section.getDouble("lifespan-seconds", defaults.lifespanTicks() / 20.0)),
                section.getInt("pickup-weight-limit", defaults.pickupWeightLimit()),
                section.getDouble("damage-multiplier", defaults.damageMultiplier()),
                section.getDouble("particle-density", defaults.particleDensity()),
                section.getDouble("spawn-weight", defaults.spawnWeight())
        );
    }

    public static TierStats defaultsFor(TornadoTier tier) {
        return switch (tier) {
            case F0 -> new TierStats(6.0, 0.12, secondsToTicks(90), 2, 0.65, 0.65, 45);
            case F1 -> new TierStats(8.0, 0.15, secondsToTicks(120), 3, 0.9, 0.85, 30);
            case F2 -> new TierStats(11.0, 0.18, secondsToTicks(150), 4, 1.2, 1.0, 16);
            case F3 -> new TierStats(14.0, 0.21, secondsToTicks(180), 6, 1.6, 1.25, 7);
            case F4 -> new TierStats(18.0, 0.24, secondsToTicks(210), 8, 2.2, 1.5, 2);
            case F5 -> new TierStats(22.0, 0.27, secondsToTicks(240), 10, 3.0, 1.8, 0.5);
        };
    }

    private static int secondsToTicks(double seconds) {
        return Math.max(20, (int) Math.round(seconds * 20.0));
    }
}
