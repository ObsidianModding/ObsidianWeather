package net.obsidianmodding.obsidianweather.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class ProtectionManager {

    private final Logger logger;
    private final List<ProtectionIntegration> integrations = new ArrayList<>();

    public ProtectionManager(Logger logger) {
        this.logger = logger;
    }

    public void register(ProtectionIntegration integration) {
        integrations.add(integration);
        logger.info("Enabled " + integration.name() + " protection integration.");
    }

    public boolean allows(Location location, ProtectionAction action, Entity affectedEntity) {
        for (ProtectionIntegration integration : integrations) {
            try {
                if (!integration.allows(location, action, affectedEntity)) {
                    return false;
                }
            } catch (RuntimeException exception) {
                logger.log(Level.WARNING,
                        integration.name() + " protection query failed; denying the weather action safely.",
                        exception);
                return false;
            }
        }
        return true;
    }

    public List<String> activeIntegrations() {
        return integrations.stream().map(ProtectionIntegration::name).toList();
    }
}
