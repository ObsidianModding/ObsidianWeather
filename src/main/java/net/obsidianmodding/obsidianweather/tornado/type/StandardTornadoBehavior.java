package net.obsidianmodding.obsidianweather.tornado.type;

import org.bukkit.Location;
import org.bukkit.Particle;

public final class StandardTornadoBehavior implements TornadoBehavior {

    @Override
    public TornadoType type() {
        return TornadoType.STANDARD;
    }

    @Override
    public boolean canSpawnAt(Location location) {
        return location.getWorld() != null;
    }

    @Override
    public Particle funnelParticle() {
        return Particle.CLOUD;
    }

    @Override
    public Particle accentParticle() {
        return Particle.WHITE_SMOKE;
    }

    @Override
    public String ambientSoundKey() {
        return "minecraft:entity.ender_dragon.flap";
    }
}
