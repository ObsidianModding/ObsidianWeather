package net.obsidianmodding.obsidianweather.tornado.physics;

import org.bukkit.Material;

public final class MaterialWeight {

    private MaterialWeight() {
    }

    public static int of(Material material) {
        String name = material.name();
        if (material.isAir() || !material.isBlock() || material == Material.BEDROCK
                || name.contains("PORTAL") || name.contains("COMMAND_BLOCK")
                || material == Material.BARRIER || material == Material.STRUCTURE_BLOCK
                || material == Material.JIGSAW || material == Material.LIGHT) {
            return Integer.MAX_VALUE;
        }
        if (name.contains("LEAVES") || name.contains("FLOWER") || name.contains("CROP")
                || name.contains("GRASS") || name.contains("MUSHROOM") || name.contains("VINE")) {
            return 1;
        }
        if (name.contains("DIRT") || name.contains("SAND") || name.contains("GRAVEL")
                || name.contains("CLAY") || name.contains("SNOW")) {
            return 2;
        }
        if (name.contains("LOG") || name.contains("WOOD") || name.contains("PLANK")
                || name.contains("WOOL") || name.contains("TERRACOTTA")) {
            return 3;
        }
        if (name.contains("STONE") || name.contains("DEEPSLATE") || name.contains("BRICK")
                || name.contains("CONCRETE")) {
            return 4;
        }
        if (name.contains("IRON") || name.contains("GOLD") || name.contains("COPPER")
                || name.contains("DIAMOND") || name.contains("NETHERITE")) {
            return 6;
        }
        if (material == Material.OBSIDIAN || material == Material.CRYING_OBSIDIAN
                || material == Material.ANCIENT_DEBRIS) {
            return 99;
        }
        return 3;
    }
}
