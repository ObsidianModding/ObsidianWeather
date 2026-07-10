package net.obsidianmodding.obsidianweather.events;

import java.util.function.Supplier;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.integration.ProtectionAction;
import net.obsidianmodding.obsidianweather.integration.ProtectionManager;
import net.obsidianmodding.obsidianweather.tornado.physics.TornadoPhysics;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class TornadoProtectionListener implements Listener {

    private final Supplier<WeatherConfig> configSupplier;
    private final ProtectionManager protectionManager;

    public TornadoProtectionListener(
            Supplier<WeatherConfig> configSupplier,
            ProtectionManager protectionManager
    ) {
        this.configSupplier = configSupplier;
        this.protectionManager = protectionManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDebrisDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof FallingBlock debris) || !isTornadoDebris(debris)) {
            return;
        }
        if (!configSupplier.get().damageEnabled()
                || !protectionManager.allows(
                        event.getEntity().getLocation(), ProtectionAction.ENTITY_DAMAGE, event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDebrisLand(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock debris) || !isTornadoDebris(debris)) {
            return;
        }
        if (!protectionManager.allows(event.getBlock().getLocation(), ProtectionAction.BLOCK_PICKUP, null)) {
            event.setCancelled(true);
            debris.remove();
        }
    }

    private boolean isTornadoDebris(FallingBlock debris) {
        return debris.getScoreboardTags().contains(TornadoPhysics.DEBRIS_TAG);
    }
}
