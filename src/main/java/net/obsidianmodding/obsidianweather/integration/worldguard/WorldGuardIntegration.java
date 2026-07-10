package net.obsidianmodding.obsidianweather.integration.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import java.util.function.Supplier;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.integration.ProtectionAction;
import net.obsidianmodding.obsidianweather.integration.ProtectionIntegration;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class WorldGuardIntegration implements ProtectionIntegration {

    private static final RegionAssociable WEATHER_ACTOR = Associables.constant(Association.NON_MEMBER);

    public WorldGuardIntegration(Supplier<WeatherConfig> ignoredConfigSupplier) {
        // The common reflective loader uses one constructor shape for all optional integrations.
    }

    @Override
    public String name() {
        return "WorldGuard";
    }

    @Override
    public boolean allows(Location location, ProtectionAction action, Entity affectedEntity) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return query.testState(BukkitAdapter.adapt(location), WEATHER_ACTOR, Flags.BUILD);
    }
}
