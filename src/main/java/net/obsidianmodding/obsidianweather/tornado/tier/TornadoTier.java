package net.obsidianmodding.obsidianweather.tornado.tier;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum TornadoTier {
    WEAK("weak"),
    MODERATE("moderate"),
    STRONG("strong"),
    SEVERE("severe"),
    VIOLENT("violent");

    private final String configKey;

    TornadoTier(String configKey) {
        this.configKey = configKey;
    }

    public String configKey() {
        return configKey;
    }

    public static Optional<TornadoTier> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values()).filter(tier -> tier.name().equals(normalized)).findFirst();
    }
}
