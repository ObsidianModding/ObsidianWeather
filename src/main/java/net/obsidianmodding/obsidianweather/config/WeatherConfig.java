package net.obsidianmodding.obsidianweather.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import net.obsidianmodding.obsidianweather.tornado.tier.TornadoTier;
import net.obsidianmodding.obsidianweather.tornado.type.TornadoType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class WeatherConfig {

    private final int spawnCheckIntervalTicks;
    private final double spawnChancePerCheck;
    private final int maxConcurrentPerWorld;
    private final double minimumSpawnDistance;
    private final double maximumSpawnDistance;
    private final Set<String> affectedWorlds;
    private final boolean blockPickupEnabled;
    private final boolean entityPickupEnabled;
    private final boolean damageEnabled;
    private final double baseDamage;
    private final int damageIntervalTicks;
    private final int blocksPerTick;
    private final int variantBlocksPerTick;
    private final int entitiesPerTick;
    private final Set<Material> blockWhitelist;
    private final Set<Material> blockBlacklist;
    private final double effectsMaxDistance;
    private final int particleIntervalTicks;
    private final int soundIntervalTicks;
    private final WarningSettings warnings;
    private final TownySettings towny;
    private final Map<TornadoType, TypeSettings> types;

    private WeatherConfig(FileConfiguration config, Logger logger) {
        spawnCheckIntervalTicks = positive(config.getInt("spawn.check-interval-ticks", 100), 100);
        spawnChancePerCheck = clamp(config.getDouble("spawn.chance-per-check", 0.004), 0.0, 1.0);
        maxConcurrentPerWorld = positive(config.getInt("spawn.max-concurrent-per-world", 2), 2);
        minimumSpawnDistance = Math.max(0.0, config.getDouble("spawn.minimum-distance-from-player", 48.0));
        maximumSpawnDistance = Math.max(minimumSpawnDistance,
                config.getDouble("spawn.maximum-distance-from-player", 112.0));
        affectedWorlds = normalizedWorlds(config.getStringList("spawn.affected-worlds"));

        blockPickupEnabled = config.getBoolean("physics.block-pickup-enabled", true);
        entityPickupEnabled = config.getBoolean("physics.entity-pickup-enabled", true);
        damageEnabled = config.getBoolean("physics.damage-enabled", true);
        baseDamage = Math.max(0.0, config.getDouble("physics.base-damage", 1.5));
        damageIntervalTicks = positive(config.getInt("physics.damage-interval-ticks", 20), 20);
        blocksPerTick = Math.max(0, config.getInt("physics.blocks-per-tick", 12));
        variantBlocksPerTick = Math.max(0, config.getInt("physics.variant-blocks-per-tick", 2));
        entitiesPerTick = Math.max(0, config.getInt("physics.entities-per-tick", 48));
        blockWhitelist = materials(config, "physics.block-whitelist", logger);
        blockBlacklist = materials(config, "physics.block-blacklist", logger);

        effectsMaxDistance = Math.max(16.0, config.getDouble("effects.max-distance", 96.0));
        particleIntervalTicks = positive(config.getInt("effects.particle-interval-ticks", 2), 2);
        soundIntervalTicks = positive(config.getInt("effects.sound-interval-ticks", 40), 40);

        warnings = new WarningSettings(
                config.getBoolean("warnings.enabled", false),
                config.getDouble("warnings.radius", 64.0),
                config.getString("warnings.message", "A deep, distant rumble rolls through the storm.")
        );
        towny = new TownySettings(
                config.getBoolean("towny.destroy-towns", false),
                config.getBoolean("towny.damage-players-in-towns", true),
                config.getBoolean("towny.knockback-entities-in-towns", true)
        );

        EnumMap<TornadoType, TypeSettings> loadedTypes = new EnumMap<>(TornadoType.class);
        for (TornadoType type : TornadoType.values()) {
            String path = "types." + type.configKey();
            EnumMap<TornadoTier, TierStats> loadedTiers = new EnumMap<>(TornadoTier.class);
            for (TornadoTier tier : TornadoTier.values()) {
                ConfigurationSection section = config.getConfigurationSection(
                        path + ".tiers." + tier.configKey());
                loadedTiers.put(tier, TierStats.from(section, tier));
            }
            double defaultWeight = switch (type) {
                case STANDARD -> 60.0;
                case FIRENADO -> 15.0;
                case ICENADO -> 15.0;
                case WATERSPOUT -> 10.0;
            };
            loadedTypes.put(type, new TypeSettings(
                    config.getBoolean(path + ".enabled", true),
                    config.getDouble(path + ".spawn-weight", defaultWeight),
                    loadedTiers
            ));
        }
        types = Collections.unmodifiableMap(loadedTypes);
    }

    public static WeatherConfig load(FileConfiguration config, Logger logger) {
        return new WeatherConfig(config, logger);
    }

    public int spawnCheckIntervalTicks() {
        return spawnCheckIntervalTicks;
    }

    public double spawnChancePerCheck() {
        return spawnChancePerCheck;
    }

    public int maxConcurrentPerWorld() {
        return maxConcurrentPerWorld;
    }

    public double minimumSpawnDistance() {
        return minimumSpawnDistance;
    }

    public double maximumSpawnDistance() {
        return maximumSpawnDistance;
    }

    public boolean affects(World world) {
        return affectedWorlds.contains("*") || affectedWorlds.contains(world.getName().toLowerCase(Locale.ROOT));
    }

    public boolean blockPickupEnabled() {
        return blockPickupEnabled;
    }

    public boolean entityPickupEnabled() {
        return entityPickupEnabled;
    }

    public boolean damageEnabled() {
        return damageEnabled;
    }

    public double baseDamage() {
        return baseDamage;
    }

    public int damageIntervalTicks() {
        return damageIntervalTicks;
    }

    public int blocksPerTick() {
        return blocksPerTick;
    }

    public int variantBlocksPerTick() {
        return variantBlocksPerTick;
    }

    public int entitiesPerTick() {
        return entitiesPerTick;
    }

    public boolean allowsMaterial(Material material) {
        return (blockWhitelist.isEmpty() || blockWhitelist.contains(material)) && !blockBlacklist.contains(material);
    }

    public double effectsMaxDistance() {
        return effectsMaxDistance;
    }

    public int particleIntervalTicks() {
        return particleIntervalTicks;
    }

    public int soundIntervalTicks() {
        return soundIntervalTicks;
    }

    public WarningSettings warnings() {
        return warnings;
    }

    public TownySettings towny() {
        return towny;
    }

    public TypeSettings type(TornadoType type) {
        return types.get(type);
    }

    private static Set<String> normalizedWorlds(java.util.List<String> configured) {
        if (configured.isEmpty()) {
            return Set.of("*");
        }
        Set<String> worlds = new HashSet<>();
        configured.forEach(name -> worlds.add(name.toLowerCase(Locale.ROOT)));
        return Collections.unmodifiableSet(worlds);
    }

    private static Set<Material> materials(FileConfiguration config, String path, Logger logger) {
        Set<Material> materials = new HashSet<>();
        for (String configured : config.getStringList(path)) {
            Material material = Material.matchMaterial(configured);
            if (material == null) {
                logger.warning("Ignoring unknown material in " + path + ": " + configured);
            } else {
                materials.add(material);
            }
        }
        return Collections.unmodifiableSet(materials);
    }

    private static int positive(int value, int fallback) {
        return value > 0 ? value : fallback;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
