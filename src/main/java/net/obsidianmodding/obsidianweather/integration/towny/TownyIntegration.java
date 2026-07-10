package net.obsidianmodding.obsidianweather.integration.towny;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import java.util.function.Supplier;
import net.obsidianmodding.obsidianweather.config.TownySettings;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.integration.ProtectionAction;
import net.obsidianmodding.obsidianweather.integration.ProtectionIntegration;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class TownyIntegration implements ProtectionIntegration {

    private final Supplier<WeatherConfig> configSupplier;

    public TownyIntegration(Supplier<WeatherConfig> configSupplier) {
        this.configSupplier = configSupplier;
    }

    @Override
    public String name() {
        return "Towny";
    }

    @Override
    public boolean allows(Location location, ProtectionAction action, Entity affectedEntity) {
        TownyAPI towny = TownyAPI.getInstance();
        if (!towny.isTownyWorld(location.getWorld()) || towny.isWilderness(location)) {
            return true;
        }

        TownySettings settings = configSupplier.get().towny();
        return switch (action) {
            case BLOCK_PICKUP, VARIANT_BLOCK_CHANGE -> settings.destroyTowns();
            case FIRE_SPREAD -> settings.destroyTowns() && townAllowsFire(towny, location);
            case ENTITY_KNOCKBACK -> settings.knockbackEntitiesInTowns();
            case ENTITY_DAMAGE -> allowsDamage(towny, settings, location, affectedEntity);
        };
    }

    private boolean allowsDamage(
            TownyAPI towny,
            TownySettings settings,
            Location location,
            Entity affectedEntity
    ) {
        if (!(affectedEntity instanceof Player)) {
            return true;
        }
        return settings.damagePlayersInTowns() && towny.isPVP(location);
    }

    private boolean townAllowsFire(TownyAPI towny, Location location) {
        Town town = towny.getTown(location);
        return town != null && town.isFire();
    }
}
