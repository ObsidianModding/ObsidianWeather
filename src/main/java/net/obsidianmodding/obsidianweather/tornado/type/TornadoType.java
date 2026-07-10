package net.obsidianmodding.obsidianweather.tornado.type;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum TornadoType {
    STANDARD("standard"),
    FIRENADO("firenado"),
    ICENADO("icenado"),
    WATERSPOUT("waterspout"),
    DUST_DEVIL("dust-devil");

    private final String configKey;

    TornadoType(String configKey) {
        this.configKey = configKey;
    }

    public String configKey() {
        return configKey;
    }

    public static Optional<TornadoType> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return Arrays.stream(values()).filter(type -> type.name().equals(normalized)).findFirst();
    }
}
