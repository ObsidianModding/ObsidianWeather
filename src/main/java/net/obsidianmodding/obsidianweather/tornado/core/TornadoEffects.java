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
        if (tornado.stats().particleDensity() <= 0.0) {
            return;
        }
        Location center = tornado.location();
        double maxDistance = config.effectsMaxDistance();
        double maxDistanceSquared = maxDistance * maxDistance;
        double baseHeight = Math.min(42.0, 14.0 + tornado.stats().radius() * 2.0);
        double funnelHeight = baseHeight * tornado.funnelProfile().heightMultiplier();

        for (Player player : tornado.world().getPlayers()) {
            double distanceSquared = player.getLocation().distanceSquared(center);
            if (distanceSquared > maxDistanceSquared) {
                continue;
            }
            double falloff = 1.0 - Math.sqrt(distanceSquared) / maxDistance;
            double visibility = 0.3 + falloff * 0.7;
            int shellCount = Math.min(160, Math.max(18,
                    (int) Math.round(60.0 * tornado.stats().particleDensity() * visibility)));
            spawnFunnelShell(player, tornado, center, funnelHeight, shellCount,
                    tornado.behavior().funnelParticle());
            spawnInnerColumn(player, tornado, center, funnelHeight, Math.max(6, shellCount / 4),
                    tornado.behavior().accentParticle());
            spawnGroundRing(player, tornado, center, Math.max(10, shellCount / 3),
                    tornado.behavior().funnelParticle());
        }
    }

    private void spawnFunnelShell(
            Player player,
            TornadoInstance tornado,
            Location center,
            double funnelHeight,
            int count,
            Particle particle
    ) {
        FunnelProfile profile = tornado.funnelProfile();
        int strands = profile.strands();
        for (int i = 0; i < count; i++) {
            int strand = i % strands;
            double height = ((double) i / count * funnelHeight
                    + tornado.random().nextDouble() * 0.8) % funnelHeight;
            double heightRatio = height / funnelHeight;
            double width = profile.shellWidth(tornado.stats().radius(), heightRatio, tornado.ageTicks());
            double angle = tornado.ageTicks() * 0.18
                    + heightRatio * Math.PI * 8.0
                    + strand * Math.PI * 2.0 / strands;
            Location point = center.clone().add(
                    profile.centerOffsetX(tornado.stats().radius(), heightRatio, tornado.ageTicks())
                            + Math.cos(angle) * width,
                    height,
                    profile.centerOffsetZ(tornado.stats().radius(), heightRatio, tornado.ageTicks())
                            + Math.sin(angle) * width
            );
            player.spawnParticle(particle, point, 1, 0.12, 0.12, 0.12, 0.01);
        }
    }

    private void spawnInnerColumn(
            Player player,
            TornadoInstance tornado,
            Location center,
            double funnelHeight,
            int count,
            Particle particle
    ) {
        FunnelProfile profile = tornado.funnelProfile();
        for (int i = 0; i < count; i++) {
            double height = ((double) i / count * funnelHeight
                    + tornado.random().nextDouble() * 1.2) % funnelHeight;
            double heightRatio = height / funnelHeight;
            double width = 0.25 + tornado.stats().radius() * 0.08 * heightRatio;
            double angle = tornado.ageTicks() * -0.24 + heightRatio * Math.PI * 10.0;
            Location point = center.clone().add(
                    profile.centerOffsetX(tornado.stats().radius(), heightRatio, tornado.ageTicks())
                            + Math.cos(angle) * width,
                    height,
                    profile.centerOffsetZ(tornado.stats().radius(), heightRatio, tornado.ageTicks())
                            + Math.sin(angle) * width
            );
            player.spawnParticle(particle, point, 1, 0.18, 0.18, 0.18, 0.01);
        }
    }

    private void spawnGroundRing(
            Player player,
            TornadoInstance tornado,
            Location center,
            int count,
            Particle particle
    ) {
        double radius = tornado.stats().radius();
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0 * i / count + tornado.ageTicks() * 0.2;
            double distance = tornado.funnelProfile().groundDistance(radius, tornado.random().nextDouble());
            Location point = center.clone().add(
                    Math.cos(angle) * distance,
                    0.15 + tornado.random().nextDouble() * 1.35,
                    Math.sin(angle) * distance
            );
            player.spawnParticle(particle, point, 1, 0.28, 0.1, 0.28, 0.02);
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
