package net.obsidianmodding.obsidianweather.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import net.obsidianmodding.obsidianweather.tornado.core.TornadoManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandBootstrap {

    private static final String COMMAND_CLASS =
            "net.obsidianmodding.obsidianweather.commands.ObsidianWeatherCommand";

    private CommandBootstrap() {
    }

    public static void register(JavaPlugin plugin, TornadoManager tornadoManager) {
        PluginCommand command = plugin.getCommand("obsidianweather");
        if (command == null) {
            plugin.getLogger().severe("Command obsidianweather is missing from plugin.yml.");
            return;
        }
        try {
            Class<?> commandClass = Class.forName(COMMAND_CLASS, true, plugin.getClass().getClassLoader());
            Object handler = commandClass.getConstructor(JavaPlugin.class, TornadoManager.class)
                    .newInstance(plugin, tornadoManager);
            command.setExecutor((CommandExecutor) handler);
            command.setTabCompleter((TabCompleter) handler);
        } catch (ClassNotFoundException exception) {
            plugin.getLogger().warning("ObsidianWeather command handler is not available.");
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                 | InvocationTargetException | ClassCastException exception) {
            plugin.getLogger().log(Level.SEVERE, "Could not register ObsidianWeather commands.", exception);
        }
    }
}
