package net.obsidianmodding.obsidianweather.tornado.type;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import org.bukkit.Location;

public final class BehaviorRegistry {

    private final Map<TornadoType, TornadoBehavior> behaviors;

    public BehaviorRegistry() {
        EnumMap<TornadoType, TornadoBehavior> registered = new EnumMap<>(TornadoType.class);
        register(registered, new StandardTornadoBehavior());
        register(registered, new FirenadoBehavior());
        register(registered, new IcenadoBehavior());
        register(registered, new WaterspoutBehavior());
        behaviors = Map.copyOf(registered);
    }

    public Optional<TornadoBehavior> get(TornadoType type) {
        return Optional.ofNullable(behaviors.get(type));
    }

    public List<TornadoBehavior> eligibleAt(Location location, WeatherConfig config) {
        return behaviors.values().stream()
                .filter(behavior -> config.type(behavior.type()).enabled())
                .filter(behavior -> behavior.canSpawnAt(location))
                .toList();
    }

    public Collection<TornadoBehavior> all() {
        return behaviors.values();
    }

    private void register(Map<TornadoType, TornadoBehavior> registered, TornadoBehavior behavior) {
        registered.put(behavior.type(), behavior);
    }
}
