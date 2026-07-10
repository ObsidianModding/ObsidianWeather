package net.obsidianmodding.obsidianweather.integration;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;
import java.util.logging.Level;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class IntegrationBootstrap {

    private IntegrationBootstrap() {
    }

    public static void registerAvailable(
            JavaPlugin plugin,
            ProtectionManager protectionManager,
            Supplier<WeatherConfig> configSupplier
    ) {
        load(plugin, protectionManager, configSupplier, "WorldGuard",
                "net.obsidianmodding.obsidianweather.integration.worldguard.WorldGuardIntegration");
        load(plugin, protectionManager, configSupplier, "Towny",
                "net.obsidianmodding.obsidianweather.integration.towny.TownyIntegration");
    }

    private static void load(
            JavaPlugin plugin,
            ProtectionManager protectionManager,
            Supplier<WeatherConfig> configSupplier,
            String pluginName,
            String adapterClassName
    ) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled(pluginName)) {
            return;
        }
        try {
            Class<?> adapterClass = Class.forName(adapterClassName, true, plugin.getClass().getClassLoader());
            Object adapter = adapterClass.getConstructor(Supplier.class).newInstance(configSupplier);
            protectionManager.register((ProtectionIntegration) adapter);
        } catch (ClassNotFoundException | NoClassDefFoundError exception) {
            plugin.getLogger().warning(pluginName + " is installed but its API classes are unavailable; integration disabled.");
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException | ClassCastException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not enable " + pluginName + " integration.", exception);
        }
    }
}
