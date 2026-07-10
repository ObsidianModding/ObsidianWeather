package net.obsidianmodding.obsidianweather.tornado.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class IcenadoBehavior implements TornadoBehavior {

    @Override
    public TornadoType type() {
        return TornadoType.ICENADO;
    }

    @Override
    public boolean canSpawnAt(Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        String biomeName = location.getBlock().getBiome().toString();
        return location.getBlock().getTemperature() <= 0.25
                || biomeName.contains("SNOW")
                || biomeName.contains("FROZEN")
                || biomeName.contains("ICE");
    }

    @Override
    public void affectEntity(Entity entity, double strength) {
        int freezeIncrease = 20 + (int) Math.round(30.0 * strength);
        entity.setFreezeTicks(Math.min(entity.getMaxFreezeTicks(), entity.getFreezeTicks() + freezeIncrease));
        if (entity instanceof LivingEntity livingEntity) {
            int amplifier = strength >= 1.5 ? 2 : 1;
            livingEntity.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    60,
                    amplifier,
                    true,
                    false,
                    true
            ));
        }
    }

    @Override
    public void affectBlock(Block block) {
        if (block.getType() == Material.WATER) {
            block.setType(Material.FROSTED_ICE, false);
        } else if (block.getType() == Material.FIRE || block.getType() == Material.SOUL_FIRE) {
            block.setType(Material.AIR, false);
        }
    }

    @Override
    public Particle funnelParticle() {
        return Particle.SNOWFLAKE;
    }

    @Override
    public Particle accentParticle() {
        return Particle.CLOUD;
    }

    @Override
    public String ambientSoundKey() {
        return "minecraft:block.glass.break";
    }
}
