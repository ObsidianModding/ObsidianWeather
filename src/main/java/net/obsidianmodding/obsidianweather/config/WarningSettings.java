package net.obsidianmodding.obsidianweather.config;

public record WarningSettings(boolean enabled, double radius, String message) {
    public WarningSettings {
        radius = Math.max(1.0, radius);
        if (message == null || message.isBlank()) {
            message = "A deep, distant rumble rolls through the storm.";
        }
    }
}
