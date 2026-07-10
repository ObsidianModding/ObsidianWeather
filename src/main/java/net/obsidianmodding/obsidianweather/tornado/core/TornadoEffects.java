package net.obsidianmodding.obsidianweather.tornado.core;

import java.util.function.Supplier;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public final class TornadoEffects {

    private final Supplier<WeatherConfig> configSupplier;

    public TornadoEffects(Supplier<WeatherConfig> configSupplier) {
        this.configSupplier = configSupplier;
    }

    public void tick(TornadoInstance tornado) {
        WeatherConfig config = configSupplier.get();
        if (tornado.ageTicks() % config.particleIntervalTicks() == 0) {
            renderParticles(tornado, config);
        }
        if (tornado.ageTicks() % config.soundIntervalTicks() == 0) {
            playAmbientSound(tornado, config);
        }
    }

    private void renderParticles(TornadoInstance tornado, WeatherConfig config) {
        Location center = tornado.location();
        double maxDistance = config.effectsMaxDistance();
        double maxDistanceSquared = maxDistance * maxDistance;
        double funnelHeight = Math.min(36.0, 10.0 + tornado.stats().radius() * 1.8);

        for (Player player : tornado.world().getPlayers()) {
            double distanceSquared = player.getLocation().distanceSquared(center);
            if (distanceSquared > maxDistanceSquared) {
                continue;
            }
            double falloff = 1.0 - Math.sqrt(distanceSquared) / maxDistance;
            int particleCount = Math.min(80,
                    Math.max(2, (int) Math.round(28.0 * tornado.stats().particleDensity() * falloff)));
            spawnFunnel(player, tornado, center, funnelHeight, particleCount, tornado.behavior().funnelParticle());
            int accentCount = Math.max(1, particleCount / 6);
            spawnFunnel(player, tornado, center, funnelHeight * 0.75, accentCount,
                    tornado.behavior().accentParticle());
        }
    }

    private void spawnFunnel(
            Player player,
            TornadoInstance tornado,
            Location center,
            double funnelHeight,
            int count,
            Particle particle
    ) {
        for (int i = 0; i < count; i++) {
            double height = tornado.random().nextDouble() * funnelHeight;
            double heightRatio = height / funnelHeight;
            double width = 0.6 + tornado.stats().radius() * (0.12 + 0.72 * heightRatio);
            double angle = tornado.random().nextDouble() * Math.PI * 2.0 + tornado.ageTicks() * 0.12;
            Location point = center.clone().add(
                    Math.cos(angle) * width,
                    height,
                    Math.sin(angle) * width
            );
            player.spawnParticle(particle, point, 1, 0.12, 0.12, 0.12, 0.01);
        }
    }

    private void playAmbientSound(TornadoInstance tornado, WeatherConfig config) {
        Location center = tornado.location();
        double maxDistance = config.effectsMaxDistance();
        double maxDistanceSquared = maxDistance * maxDistance;
        for (Player player : tornado.world().getPlayers()) {
            double distanceSquared = player.getLocation().distanceSquared(center);
            if (distanceSquared > maxDistanceSquared) {
                continue;
            }
            double falloff = 1.0 - Math.sqrt(distanceSquared) / maxDistance;
            float volume = (float) Math.max(0.05, Math.min(1.0, falloff));
            float pitch = (float) Math.max(0.55, 1.0 - tornado.stats().damageMultiplier() * 0.1);
            player.playSound(center, tornado.behavior().ambientSoundKey(), volume, pitch);
        }
    }
}
