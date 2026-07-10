package net.obsidianmodding.obsidianweather.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.obsidianmodding.obsidianweather.tornado.tier.TornadoTier;

public record TypeSettings(boolean enabled, double spawnWeight, Map<TornadoTier, TierStats> tiers) {
    public TypeSettings {
        spawnWeight = Math.max(0.0, spawnWeight);
        EnumMap<TornadoTier, TierStats> copy = new EnumMap<>(TornadoTier.class);
        copy.putAll(tiers);
        for (TornadoTier tier : TornadoTier.values()) {
            copy.putIfAbsent(tier, TierStats.defaultsFor(tier));
        }
        tiers = Collections.unmodifiableMap(copy);
    }

    public TierStats stats(TornadoTier tier) {
        return tiers.get(tier);
    }
}
