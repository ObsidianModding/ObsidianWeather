package net.obsidianmodding.obsidianweather.tornado.movement;

import java.util.OptionalInt;
import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class GroundLocator {

    private static final int NETHER_VERTICAL_SEARCH = 48;

    private GroundLocator() {
    }

    public static OptionalInt findGroundY(World world, int x, int z, double referenceY) {
        if (world.getEnvironment() != World.Environment.NETHER) {
            return OptionalInt.of(world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE) + 1);
        }

        int minimum = world.getMinHeight() + 1;
        int maximum = world.getMaxHeight() - 2;
        int reference = Math.max(minimum, Math.min(maximum, (int) Math.floor(referenceY)));
        for (int offset = 0; offset <= NETHER_VERTICAL_SEARCH; offset++) {
            int below = reference - offset;
            if (below >= minimum && isUsableGround(world, x, below, z)) {
                return OptionalInt.of(below);
            }
            int above = reference + offset;
            if (offset > 0 && above <= maximum && isUsableGround(world, x, above, z)) {
                return OptionalInt.of(above);
            }
        }
        return OptionalInt.empty();
    }

    private static boolean isUsableGround(World world, int x, int y, int z) {
        Block support = world.getBlockAt(x, y - 1, z);
        Material supportType = support.getType();
        boolean supported = supportType.isSolid() || supportType == Material.LAVA;
        return supported
                && world.getBlockAt(x, y, z).getType().isAir()
                && world.getBlockAt(x, y + 1, z).getType().isAir();
    }
}
