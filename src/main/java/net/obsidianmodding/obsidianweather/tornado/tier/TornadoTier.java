package net.obsidianmodding.obsidianweather.tornado.tier;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum TornadoTier {
    F0("f0", "weak"),
    F1("f1", "moderate"),
    F2("f2", "strong"),
    F3("f3", "severe"),
    F4("f4", "violent"),
    F5("f5", null);

    private final String configKey;
    private final String legacyConfigKey;

    TornadoTier(String configKey, String legacyConfigKey) {
        this.configKey = configKey;
        this.legacyConfigKey = legacyConfigKey;
    }

    public String configKey() {
        return configKey;
    }

    public String displayName() {
        return name();
    }

    public String legacyConfigKey() {
        return legacyConfigKey;
    }

    public static Optional<TornadoTier> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        Optional<TornadoTier> direct = Arrays.stream(values())
                .filter(tier -> tier.name().equals(normalized))
                .findFirst();
        if (direct.isPresent()) {
            return direct;
        }
        return Arrays.stream(values())
                .filter(tier -> tier.legacyConfigKey != null)
                .filter(tier -> tier.legacyConfigKey.toUpperCase(Locale.ROOT).equals(normalized))
                .findFirst();
    }
}
