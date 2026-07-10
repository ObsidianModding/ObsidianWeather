package net.obsidianmodding.obsidianweather.tornado.physics;

import java.util.Collection;
import java.util.function.Supplier;
import net.obsidianmodding.obsidianweather.config.WeatherConfig;
import net.obsidianmodding.obsidianweather.integration.ProtectionAction;
import net.obsidianmodding.obsidianweather.integration.ProtectionManager;
import net.obsidianmodding.obsidianweather.tornado.core.TornadoInstance;
import net.obsidianmodding.obsidianweather.tornado.type.TornadoType;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class TornadoPhysics {

    private static final String DEBRIS_TAG = "obsidianweather-debris";

    private final Supplier<WeatherConfig> configSupplier;
    private final ProtectionManager protectionManager;

    public TornadoPhysics(Supplier<WeatherConfig> configSupplier, ProtectionManager protectionManager) {
        this.configSupplier = configSupplier;
        this.protectionManager = protectionManager;
    }

    public void tick(TornadoInstance tornado) {
        WeatherConfig config = configSupplier.get();
        if (config.entityPickupEnabled()) {
            affectEntities(tornado, config);
        }
        if (config.blockPickupEnabled() && tornado.behavior().canPickupBlocks()) {
            pickUpBlocks(tornado, config);
        }
        applyVariantBlockEffects(tornado, config);
    }

    private void affectEntities(TornadoInstance tornado, WeatherConfig config) {
        Location center = tornado.location();
        double radius = tornado.stats().radius();
        Collection<Entity> nearby = tornado.world().getNearbyEntities(center, radius, radius * 1.6, radius);
        int processed = 0;
        for (Entity entity : nearby) {
            if (processed >= config.entitiesPerTick()) {
                break;
            }
            if (!canMove(entity) || entity.getLocation().distanceSquared(center) > radius * radius) {
                continue;
            }
            if (!protectionManager.allows(entity.getLocation(), ProtectionAction.ENTITY_KNOCKBACK, entity)) {
                continue;
            }

            Vector fromCenter = entity.getLocation().toVector().subtract(center.toVector());
            fromCenter.setY(0.0);
            double distance = Math.max(0.4, fromCenter.length());
            double proximity = Math.max(0.15, 1.0 - (distance / radius));
            double tierForce = 0.65 + tornado.stats().damageMultiplier() * 0.35;
            double strength = proximity * tierForce;

            Vector tangent = new Vector(-fromCenter.getZ(), 0.0, fromCenter.getX()).normalize();
            Vector inward = fromCenter.clone().normalize().multiply(-1.0);
            Vector velocity = tangent.multiply(0.75 * strength)
                    .add(inward.multiply(0.35 * strength))
                    .setY(0.28 + 0.42 * strength);
            velocity = tornado.behavior().modifyEntityVelocity(entity, center, velocity, strength);
            entity.setVelocity(velocity);

            if (config.damageEnabled()
                    && tornado.ageTicks() % config.damageIntervalTicks() == 0
                    && protectionManager.allows(entity.getLocation(), ProtectionAction.ENTITY_DAMAGE, entity)) {
                tornado.behavior().affectEntity(entity, strength);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.damage(config.baseDamage() * tornado.stats().damageMultiplier());
                }
            }
            processed++;
        }
    }

    private void pickUpBlocks(TornadoInstance tornado, WeatherConfig config) {
        World world = tornado.world();
        Location center = tornado.location();
        double radius = tornado.stats().radius();
        for (int i = 0; i < config.blocksPerTick(); i++) {
            double angle = tornado.random().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(tornado.random().nextDouble()) * radius;
            int x = (int) Math.floor(center.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(center.getZ() + Math.sin(angle) * distance);
            int y = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
            Block block = world.getBlockAt(x, y, z);
            Material material = block.getType();

            if (!config.allowsMaterial(material)
                    || MaterialWeight.of(material) > tornado.stats().pickupWeightLimit()
                    || block.getState() instanceof TileState
                    || !protectionManager.allows(block.getLocation(), ProtectionAction.BLOCK_PICKUP, null)) {
                continue;
            }

            FallingBlock debris = world.spawnFallingBlock(block.getLocation().add(0.5, 0.1, 0.5), block.getBlockData());
            debris.setDropItem(false);
            debris.setHurtEntities(config.damageEnabled());
            debris.addScoreboardTag(DEBRIS_TAG);
            Vector debrisVelocity = new Vector(-Math.sin(angle), 0.7, Math.cos(angle))
                    .multiply(0.25 + tornado.stats().damageMultiplier() * 0.08);
            debris.setVelocity(debrisVelocity);
            block.setType(Material.AIR, false);
        }
    }

    private void applyVariantBlockEffects(TornadoInstance tornado, WeatherConfig config) {
        if (tornado.type() == TornadoType.STANDARD || tornado.type() == TornadoType.WATERSPOUT) {
            return;
        }
        World world = tornado.world();
        Location center = tornado.location();
        for (int i = 0; i < config.variantBlocksPerTick(); i++) {
            double angle = tornado.random().nextDouble() * Math.PI * 2.0;
            double distance = Math.sqrt(tornado.random().nextDouble()) * tornado.stats().radius();
            int x = (int) Math.floor(center.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(center.getZ() + Math.sin(angle) * distance);
            int surfaceY = world.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
            int y = tornado.type() == TornadoType.FIRENADO ? surfaceY + 1 : surfaceY;
            Block block = world.getBlockAt(x, y, z);
            ProtectionAction action = tornado.type() == TornadoType.FIRENADO
                    ? ProtectionAction.FIRE_SPREAD
                    : ProtectionAction.VARIANT_BLOCK_CHANGE;
            if (protectionManager.allows(block.getLocation(), action, null)) {
                tornado.behavior().affectBlock(block);
            }
        }
    }

    private boolean canMove(Entity entity) {
        if (!entity.isValid() || entity instanceof ArmorStand) {
            return false;
        }
        if (entity instanceof FallingBlock && entity.getScoreboardTags().contains(DEBRIS_TAG)) {
            return false;
        }
        if (entity instanceof Player player) {
            return player.getGameMode() != GameMode.SPECTATOR && player.getGameMode() != GameMode.CREATIVE;
        }
        return true;
    }
}
