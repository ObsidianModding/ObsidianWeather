package net.obsidianmodding.obsidianweather.tornado.movement;

import net.obsidianmodding.obsidianweather.tornado.core.TornadoInstance;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class PathMovement {

    private static final double MAX_TURN_RADIANS_PER_TICK = Math.toRadians(2.5);

    public boolean move(TornadoInstance tornado) {
        World world = tornado.world();
        Vector heading = tornado.heading();

        double turn = (tornado.random().nextDouble() * 2.0 - 1.0) * MAX_TURN_RADIANS_PER_TICK;
        heading.rotateAroundY(turn).setY(0.0);
        if (heading.lengthSquared() < 0.001) {
            heading.setX(1.0);
        }
        heading.normalize();

        double distance = tornado.stats().movementSpeed() * tornado.behavior().movementSpeedMultiplier();
        Location next = tornado.location().add(heading.clone().multiply(distance));
        int chunkX = next.getBlockX() >> 4;
        int chunkZ = next.getBlockZ() >> 4;
        if (!world.isChunkLoaded(chunkX, chunkZ) || !world.getWorldBorder().isInside(next)) {
            return false;
        }

        int surfaceY = world.getHighestBlockYAt(next.getBlockX(), next.getBlockZ(), HeightMap.WORLD_SURFACE);
        next.setY(Math.min(world.getMaxHeight() - 1, Math.max(world.getMinHeight() + 1, surfaceY + 1.0)));
        tornado.heading(heading);
        tornado.moveTo(next);
        return true;
    }
}
