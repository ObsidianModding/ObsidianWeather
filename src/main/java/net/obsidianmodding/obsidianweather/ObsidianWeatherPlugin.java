package net.obsidianmodding.obsidianweather;

import org.bukkit.plugin.java.JavaPlugin;

public final class ObsidianWeatherPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("ObsidianWeather enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("ObsidianWeather disabled.");
    }
}
