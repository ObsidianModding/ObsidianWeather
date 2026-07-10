package net.obsidianmodding.obsidianweather.tornado.type;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public final class FirenadoBehavior implements TornadoBehavior {

    private static final Set<Material> HEAT_SOURCES = EnumSet.of(
            Material.FIRE, Material.SOUL_FIRE, Material.LAVA, Material.CAMPFIRE, Material.SOUL_CAMPFIRE);

    @Override
    public TornadoType type() {
        return TornadoType.FIRENADO;
    }

    @Override
    public boolean canSpawnNaturallyIn(World world) {
        return world.getEnvironment() == World.Environment.NETHER
                || TornadoBehavior.super.canSpawnNaturallyIn(world);
    }

    @Override
    public boolean canSpawnAt(Location location) {
        if (location.getWorld() == null) {
            return false;
        }
        return location.getWorld().getEnvironment() == World.Environment.NETHER
                || location.getBlock().getTemperature() >= 0.8
                || hasNearbyHeatSource(location);
    }

    @Override
    public void affectEntity(Entity entity, double strength) {
        int burnTicks = Math.max(entity.getFireTicks(), 40 + (int) Math.round(40.0 * strength));
        entity.setFireTicks(burnTicks);
    }

    @Override
    public void affectBlock(Block block) {
        if (block.getType().isAir() && block.getRelative(BlockFace.DOWN).getType().isSolid()) {
            block.setType(Material.FIRE, false);
            return;
        }
        Block above = block.getRelative(BlockFace.UP);
        if (block.getType().isFlammable() && above.getType().isAir()) {
            above.setType(Material.FIRE, false);
        }
    }

    @Override
    public Particle funnelParticle() {
        return Particle.FLAME;
    }

    @Override
    public Particle accentParticle() {
        return Particle.SMALL_FLAME;
    }

    @Override
    public String ambientSoundKey() {
        return "minecraft:block.fire.ambient";
    }

    private boolean hasNearbyHeatSource(Location location) {
        World world = location.getWorld();
        int centerX = location.getBlockX();
        int centerY = location.getBlockY();
        int centerZ = location.getBlockZ();
        for (int x = centerX - 6; x <= centerX + 6; x += 2) {
            for (int z = centerZ - 6; z <= centerZ + 6; z += 2) {
                if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = centerY - 3; y <= centerY + 3; y++) {
                    if (HEAT_SOURCES.contains(world.getBlockAt(x, y, z).getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
