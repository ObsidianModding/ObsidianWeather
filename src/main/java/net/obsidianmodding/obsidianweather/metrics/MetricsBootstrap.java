package net.obsidianmodding.obsidianweather.metrics;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class MetricsBootstrap {

    private static final int PLUGIN_ID = 32529;

    private MetricsBootstrap() {
    }

    public static void start(JavaPlugin plugin) {
        new Metrics(plugin, PLUGIN_ID);
    }
}
