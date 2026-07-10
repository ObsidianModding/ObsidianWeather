package net.obsidianmodding.obsidianweather.tornado.core;

import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.obsidianmodding.obsidianweather.config.WarningSettings;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class LocalWarningService {

    private final Supplier<WeatherConfig> configSupplier;

    public LocalWarningService(Supplier<WeatherConfig> configSupplier) {
        this.configSupplier = configSupplier;
    }

    public void notifySpawn(TornadoInstance tornado) {
        WarningSettings warnings = configSupplier.get().warnings();
        if (!warnings.enabled()) {
            return;
        }
        Location current = tornado.location();
        Location projected = current.clone().add(tornado.heading().multiply(
                tornado.stats().movementSpeed() * tornado.behavior().movementSpeedMultiplier() * 60.0));
        double radiusSquared = warnings.radius() * warnings.radius();
        Component message = Component.text(warnings.message(), NamedTextColor.DARK_GRAY);
        for (Player player : tornado.world().getPlayers()) {
            if (player.getLocation().distanceSquared(current) <= radiusSquared
                    || player.getLocation().distanceSquared(projected) <= radiusSquared) {
                player.sendMessage(message);
            }
        }
    }
}
