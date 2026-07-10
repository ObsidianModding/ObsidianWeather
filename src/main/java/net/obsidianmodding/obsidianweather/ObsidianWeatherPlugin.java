package net.obsidianmodding.obsidianweather;

import net.obsidianmodding.obsidianweather.commands.CommandBootstrap;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.integration.IntegrationBootstrap;
import net.obsidianmodding.obsidianweather.integration.ProtectionManager;
import net.obsidianmodding.obsidianweather.tornado.core.TornadoManager;
import net.obsidianmodding.obsidianweather.tornado.spawner.TornadoSpawner;
import net.obsidianmodding.obsidianweather.tornado.type.BehaviorRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class ObsidianWeatherPlugin extends JavaPlugin {

    private volatile WeatherConfig weatherConfig;
    private TornadoManager tornadoManager;
    private BukkitTask tickTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadWeatherConfig();

        ProtectionManager protectionManager = new ProtectionManager(getLogger());
        IntegrationBootstrap.registerAvailable(this, protectionManager, this::weatherConfig);

        BehaviorRegistry behaviorRegistry = new BehaviorRegistry();
        tornadoManager = new TornadoManager(getLogger(), this::weatherConfig, behaviorRegistry, protectionManager);
        TornadoSpawner tornadoSpawner = new TornadoSpawner(this, this::weatherConfig, tornadoManager);
        CommandBootstrap.register(this, tornadoManager);

        tickTask = getServer().getScheduler().runTaskTimer(this, () -> {
            tornadoSpawner.tick();
            tornadoManager.tick();
        }, 1L, 1L);
        getLogger().info("ObsidianWeather enabled with integrations: "
                + String.join(", ", protectionManager.activeIntegrations()));
    }

    @Override
    public void onDisable() {
        if (tickTask != null) {
            tickTask.cancel();
        }
        if (tornadoManager != null) {
            tornadoManager.stopAll();
        }
        getLogger().info("ObsidianWeather disabled.");
    }

    public void reloadWeatherConfig() {
        reloadConfig();
        weatherConfig = WeatherConfig.load(getConfig(), getLogger());
    }

    public WeatherConfig weatherConfig() {
        return weatherConfig;
    }

    public TornadoManager tornadoManager() {
        return tornadoManager;
    }
}
