package net.obsidianmodding.obsidianweather.tornado.type;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class DustDevilBehavior implements TornadoBehavior {

    @Override
    public TornadoType type() {
        return TornadoType.DUST_DEVIL;
    }

    @Override
    public boolean canSpawnNaturallyIn(World world) {
        return !world.hasStorm();
    }

    @Override
    public boolean canSpawnAt(Location location) {
        if (location.getWorld() == null || location.getBlock().getTemperature() < 0.8) {
            return false;
        }
        String biomeName = location.getBlock().getBiome().toString();
        return biomeName.contains("DESERT")
                || biomeName.contains("BADLANDS")
                || biomeName.contains("SAVANNA");
    }

    @Override
    public double movementSpeedMultiplier() {
        return 1.4;
    }

    @Override
    public Vector modifyEntityVelocity(Entity entity, Location center, Vector velocity, double strength) {
        velocity.setX(velocity.getX() * 1.45);
        velocity.setZ(velocity.getZ() * 1.45);
        velocity.setY(Math.min(0.38, 0.12 + 0.18 * strength));
        return velocity;
    }

    @Override
    public void affectEntity(Entity entity, double strength) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addPotionEffect(new PotionEffect(
                    PotionEffectType.BLINDNESS,
                    50,
                    0,
                    true,
                    false,
                    true
            ));
        }
    }

    @Override
    public Particle funnelParticle() {
        return Particle.DUST_PLUME;
    }

    @Override
    public Particle accentParticle() {
        return Particle.ASH;
    }

    @Override
    public String ambientSoundKey() {
        return "minecraft:block.sand.break";
    }
}
