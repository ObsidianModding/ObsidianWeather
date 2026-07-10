package net.obsidianmodding.obsidianweather.tornado.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;
import net.obsidianmodding.obsidianweather.config.TierStats;
import net.obsidianmodding.obsidianweather.config.TypeSettings;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.integration.ProtectionManager;
import net.obsidianmodding.obsidianweather.tornado.movement.PathMovement;
import net.obsidianmodding.obsidianweather.tornado.physics.TornadoPhysics;
import net.obsidianmodding.obsidianweather.tornado.tier.TornadoTier;
import net.obsidianmodding.obsidianweather.tornado.type.BehaviorRegistry;
import net.obsidianmodding.obsidianweather.tornado.type.TornadoBehavior;
import net.obsidianmodding.obsidianweather.tornado.type.TornadoType;
import org.bukkit.Location;
import org.bukkit.World;

public final class TornadoManager {

    private final Logger logger;
    private final Supplier<WeatherConfig> configSupplier;
    private final BehaviorRegistry behaviorRegistry;
    private final PathMovement pathMovement = new PathMovement();
    private final TornadoPhysics physics;
    private final TornadoEffects effects;
    private final LocalWarningService warningService;
    private final Random random = new Random();
    private final Map<String, TornadoInstance> active = new LinkedHashMap<>();

    public TornadoManager(
            Logger logger,
            Supplier<WeatherConfig> configSupplier,
            BehaviorRegistry behaviorRegistry,
            ProtectionManager protectionManager
    ) {
        this.logger = logger;
        this.configSupplier = configSupplier;
        this.behaviorRegistry = behaviorRegistry;
        this.physics = new TornadoPhysics(configSupplier, protectionManager);
        this.effects = new TornadoEffects(configSupplier);
        this.warningService = new LocalWarningService(configSupplier);
    }

    public void tick() {
        Iterator<TornadoInstance> iterator = active.values().iterator();
        while (iterator.hasNext()) {
            TornadoInstance tornado = iterator.next();
            if (!tornado.tickLifetime() || !pathMovement.move(tornado)) {
                iterator.remove();
                logger.info("Tornado " + tornado.id() + " dissipated.");
                continue;
            }
            physics.tick(tornado);
            effects.tick(tornado);
        }
    }

    public Optional<TornadoInstance> spawn(
            Location location,
            TornadoType requestedType,
            TornadoTier requestedTier,
            boolean enforceTypeConstraints
    ) {
        if (location.getWorld() == null || count(location.getWorld()) >= configSupplier.get().maxConcurrentPerWorld()) {
            return Optional.empty();
        }
        WeatherConfig config = configSupplier.get();
        TornadoBehavior behavior;
        if (requestedType != null) {
            behavior = behaviorRegistry.get(requestedType).orElse(null);
            if (behavior == null || !config.type(requestedType).enabled()
                    || (enforceTypeConstraints && !behavior.canSpawnAt(location))) {
                return Optional.empty();
            }
        } else {
            List<TornadoBehavior> eligible = behaviorRegistry.eligibleAt(location, config);
            behavior = weightedBehavior(eligible, config);
            if (behavior == null) {
                return Optional.empty();
            }
        }

        TypeSettings typeSettings = config.type(behavior.type());
        TornadoTier tier = requestedTier != null ? requestedTier : weightedTier(typeSettings);
        TierStats stats = typeSettings.stats(tier);
        TornadoInstance tornado = new TornadoInstance(location, behavior, tier, stats);
        active.put(tornado.id(), tornado);
        warningService.notifySpawn(tornado);
        logger.info("Spawned " + tier.configKey() + " " + behavior.type().configKey()
                + " tornado " + tornado.id() + " in " + location.getWorld().getName() + ".");
        return Optional.of(tornado);
    }

    public List<TornadoInstance> active() {
        return List.copyOf(active.values());
    }

    public int count(World world) {
        int count = 0;
        for (TornadoInstance tornado : active.values()) {
            if (tornado.world().equals(world)) {
                count++;
            }
        }
        return count;
    }

    public int stop(String idOrAll) {
        if (idOrAll.equalsIgnoreCase("all")) {
            int stopped = active.size();
            active.values().forEach(TornadoInstance::stop);
            active.clear();
            return stopped;
        }
        String normalized = idOrAll.toLowerCase(java.util.Locale.ROOT);
        List<String> matches = active.keySet().stream().filter(id -> id.startsWith(normalized)).toList();
        for (String id : matches) {
            active.remove(id).stop();
        }
        return matches.size();
    }

    public void stopAll() {
        stop("all");
    }

    private TornadoBehavior weightedBehavior(List<TornadoBehavior> eligible, WeatherConfig config) {
        double total = eligible.stream().mapToDouble(behavior -> config.type(behavior.type()).spawnWeight()).sum();
        if (total <= 0.0) {
            return null;
        }
        double roll = random.nextDouble() * total;
        for (TornadoBehavior behavior : eligible) {
            roll -= config.type(behavior.type()).spawnWeight();
            if (roll <= 0.0) {
                return behavior;
            }
        }
        return eligible.getLast();
    }

    private TornadoTier weightedTier(TypeSettings settings) {
        List<TornadoTier> tiers = new ArrayList<>(List.of(TornadoTier.values()));
        double total = tiers.stream().mapToDouble(tier -> settings.stats(tier).spawnWeight()).sum();
        if (total <= 0.0) {
            return TornadoTier.WEAK;
        }
        double roll = random.nextDouble() * total;
        for (TornadoTier tier : tiers) {
            roll -= settings.stats(tier).spawnWeight();
            if (roll <= 0.0) {
                return tier;
            }
        }
        return TornadoTier.WEAK;
    }
}
