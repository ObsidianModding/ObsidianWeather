package net.obsidianmodding.obsidianweather.integration;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * Shared extension point for optional claim/protection plugins.
 *
 * <p>Future GriefPrevention or Residence adapters should implement this interface in their
 * own isolated package, then be conditionally registered by the plugin bootstrap.</p>
 */
public interface ProtectionIntegration {

    String name();

    boolean allows(Location location, ProtectionAction action, Entity affectedEntity);
}
