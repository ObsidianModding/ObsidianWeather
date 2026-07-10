package net.obsidianmodding.obsidianweather.tornado.core;

import java.util.Random;

public enum FunnelProfile {
    CLASSIC_CONE("classic-cone", 3, 1.0),
    ROPE("rope", 2, 1.25),
    STOVEPIPE("stovepipe", 3, 1.0),
    WEDGE("wedge", 4, 0.78),
    MULTI_VORTEX("multi-vortex", 6, 0.9);

    private final String configKey;
    private final int strands;
    private final double heightMultiplier;

    FunnelProfile(String configKey, int strands, double heightMultiplier) {
        this.configKey = configKey;
        this.strands = strands;
        this.heightMultiplier = heightMultiplier;
    }

    public String configKey() {
        return configKey;
    }

    public int strands() {
        return strands;
    }

    public double heightMultiplier() {
        return heightMultiplier;
    }

    public double shellWidth(double radius, double heightRatio, long ageTicks) {
        return switch (this) {
            case CLASSIC_CONE -> 0.7 + radius * (0.1 + 0.58 * heightRatio);
            case ROPE -> 0.3 + radius * (0.07 + 0.07 * heightRatio);
            case STOVEPIPE -> 0.7 + radius * (0.34 + 0.035 * Math.sin(heightRatio * Math.PI * 4.0));
            case WEDGE -> 0.8 + radius * (0.62 - 0.18 * heightRatio);
            case MULTI_VORTEX -> 0.5 + radius * (0.2 + 0.3 * heightRatio);
        };
    }

    public double centerOffsetX(double radius, double heightRatio, long ageTicks) {
        if (this != ROPE) {
            return 0.0;
        }
        return Math.sin(heightRatio * Math.PI * 5.0 + ageTicks * 0.045) * radius * 0.18;
    }

    public double centerOffsetZ(double radius, double heightRatio, long ageTicks) {
        if (this != ROPE) {
            return 0.0;
        }
        return Math.cos(heightRatio * Math.PI * 4.0 + ageTicks * 0.04) * radius * 0.18;
    }

    public double groundDistance(double radius, double randomValue) {
        return switch (this) {
            case CLASSIC_CONE -> radius * (0.45 + randomValue * 0.5);
            case ROPE -> radius * (0.2 + randomValue * 0.35);
            case STOVEPIPE -> radius * (0.35 + randomValue * 0.4);
            case WEDGE -> radius * (0.65 + randomValue * 0.35);
            case MULTI_VORTEX -> radius * (0.4 + randomValue * 0.6);
        };
    }

    public static FunnelProfile random(Random random) {
        FunnelProfile[] profiles = values();
        return profiles[random.nextInt(profiles.length)];
    }
}
