package net.obsidianmodding.obsidianweather.config;

public record TownySettings(
        boolean destroyTowns,
        boolean damagePlayersInTowns,
        boolean knockbackEntitiesInTowns
) {
}
