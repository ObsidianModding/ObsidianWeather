package net.obsidianmodding.obsidianweather.tornado.type;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public interface TornadoBehavior {

    TornadoType type();

    boolean canSpawnAt(Location location);

    default boolean canPickupBlocks() {
        return true;
    }

    default double movementSpeedMultiplier() {
        return 1.0;
    }

    default Vector modifyEntityVelocity(Entity entity, Location center, Vector velocity, double strength) {
        return velocity;
    }

    default void affectEntity(Entity entity, double strength) {
    }

    default void affectBlock(Block block) {
    }

    Particle funnelParticle();

    Particle accentParticle();

    String ambientSoundKey();
}
