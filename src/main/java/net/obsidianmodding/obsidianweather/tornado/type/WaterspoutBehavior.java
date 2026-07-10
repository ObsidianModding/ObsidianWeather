package net.obsidianmodding.obsidianweather.tornado.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public final class WaterspoutBehavior implements TornadoBehavior {

    @Override
    public TornadoType type() {
        return TornadoType.WATERSPOUT;
    }

    @Override
    public boolean canSpawnAt(Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        Material at = location.getBlock().getType();
        Material below = location.getBlock().getRelative(BlockFace.DOWN).getType();
        String biomeName = location.getBlock().getBiome().toString();
        return (at == Material.WATER || below == Material.WATER)
                && (biomeName.contains("OCEAN") || biomeName.contains("RIVER"));
    }

    @Override
    public boolean canPickupBlocks() {
        return false;
    }

    @Override
    public double movementSpeedMultiplier() {
        return 1.25;
    }

    @Override
    public Vector modifyEntityVelocity(Entity entity, Location center, Vector velocity, double strength) {
        velocity.setX(velocity.getX() * 1.35);
        velocity.setZ(velocity.getZ() * 1.35);
        velocity.setY(Math.min(0.05, velocity.getY() * 0.25 - (0.08 * strength)));
        return velocity;
    }

    @Override
    public Particle funnelParticle() {
        return Particle.SPLASH;
    }

    @Override
    public Particle accentParticle() {
        return Particle.BUBBLE;
    }

    @Override
    public String ambientSoundKey() {
        return "minecraft:weather.rain.above";
    }
}
