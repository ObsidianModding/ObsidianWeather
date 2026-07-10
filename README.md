# ObsidianWeather

ObsidianWeather is a Paper plugin that turns vanilla thunderstorms into dangerous, discoverable weather. Tornadoes form near loaded players, follow bounded semi-random paths, lift configured blocks and entities, and dissipate naturally without server-wide alerts or artificial spawn cooldowns.

## Features

- Five independently tunable severity tiers: Weak, Moderate, Strong, Severe, and Violent.
- Four behaviorally distinct tornado types:
  - **Standard:** the baseline moving funnel, block pickup, debris, rotational lift, and damage.
  - **Firenado:** forms in hot biomes or near fire/lava, burns entities, and starts protected-policy-aware fires.
  - **Icenado:** forms in frozen biomes, applies freezing and slowness, extinguishes fire, and freezes surface water.
  - **Waterspout:** forms only over ocean/river water, moves 25% faster, pulls entities downward with stronger horizontal force, and never lifts land blocks.
- A fresh weighted probability roll every configured interval during vanilla thunderstorms. There is no post-spawn cooldown.
- Per-world concurrency caps and loaded-chunk-only movement; tornadoes never force chunk generation.
- Budgeted surface sampling instead of synchronous radius-wide block scans.
- Nearby-only particles and sound with distance falloff.
- No boss bar, countdown, action bar, siren, or default chat warning. An optional local chat cue is off by default.
- Runtime-safe soft integrations for WorldGuard and Towny.
- Admin spawning, stopping, listing, reloading, and tab completion.

## Requirements

- Paper **1.21.11 or newer in the 1.21 line**
- Java **21**
- Maven is only needed when building from source; server owners install the produced JAR.

## Installation

1. Download an `ObsidianWeather-*.jar` from a successful GitHub Actions build artifact.
2. Stop the Paper server.
3. Place the JAR in the server's `plugins/` directory.
4. Start the server once to create `plugins/ObsidianWeather/config.yml`.
5. Adjust the configuration and run `/obsidianweather reload`, or restart the server.

WorldGuard and Towny are optional. Do not install either plugin solely for ObsidianWeather.

## How spawning works

Every `spawn.check-interval-ticks`, each configured world gets an independent probability roll if it is currently thundering, has an online player, and is below its tornado cap. A candidate location is chosen in a loaded chunk near a player. The plugin filters enabled tornado types by their environment constraints, rolls a type by weight, then rolls that type's tier by its own tier weights.

A successful spawn does not alter the next probability check. This continuous model intentionally has no “wait N minutes after a tornado” timer.

## Environmental awareness

Players normally discover tornadoes through their funnels, debris, nearby weather particles, and local roar or elemental sounds. Effects are sent only to players within `effects.max-distance` and diminish with range.

`warnings.enabled` optionally sends one minimal chat message to players near the new funnel or its short projected path. It defaults to `false`, is never server-wide, and does not create persistent UI.

## Protection integrations

Both integrations are true `softdepend` entries. Their classes live in isolated packages and are loaded only when the corresponding plugin and API are present.

| Integration | Installed | Not installed |
|---|---|---|
| WorldGuard | Tornado block changes, fire, knockback, and damage are denied wherever a non-member actor fails the region `BUILD` query. | All non-Towny wilderness behavior follows ObsidianWeather's own config. |
| Towny | Wilderness is unaffected by Towny policy. Claimed blocks are protected by default. Player damage, entity knockback, and town destruction are independently configured. Player damage also respects Towny's location-aware PvP decision; firenado fire respects the town fire toggle. | All non-WorldGuard areas follow ObsidianWeather's own config. |

If both are installed, every active integration must allow an action before it occurs. A failed protection query denies the action safely.

## Commands and permissions

All subcommands require `obsidianweather.admin`, which defaults to server operators.

| Command | Description |
|---|---|
| `/obsidianweather spawn <world> [x z] [type] [tier]` | Spawn a tornado. Omitted type/tier values use weighted selection. Players may omit coordinates to use their current X/Z; console must provide them. |
| `/obsidianweather stop <id\|all>` | Stop one tornado by full/partial ID, or all tornadoes. |
| `/obsidianweather list` | List IDs, type, tier, world, position, and remaining lifetime. |
| `/obsidianweather reload` | Reload `config.yml`. Existing tornadoes retain their spawn-time tier stats. |
| `/ow ...` | Short alias for `/obsidianweather`. |

Valid types are `standard`, `firenado`, `icenado`, and `waterspout`. Valid tiers are `weak`, `moderate`, `strong`, `severe`, and `violent`.

## Configuration reference

The bundled [`config.yml`](src/main/resources/config.yml) contains inline comments and is the source of truth.

### Spawn, physics, effects, warnings, and Towny

| Path | Default | Meaning |
|---|---:|---|
| `spawn.check-interval-ticks` | `100` | Interval between independent thunderstorm chance rolls. |
| `spawn.chance-per-check` | `0.004` | Probability per eligible world and check, from `0.0` to `1.0`. |
| `spawn.max-concurrent-per-world` | `2` | Hard per-world active tornado cap. |
| `spawn.minimum-distance-from-player` | `48.0` | Minimum natural spawn distance. |
| `spawn.maximum-distance-from-player` | `112.0` | Maximum natural spawn distance. |
| `spawn.affected-worlds` | `['*']` | Exact world names, or `*` for all worlds. |
| `physics.block-pickup-enabled` | `true` | Allow eligible tornadoes to turn blocks into falling debris. |
| `physics.entity-pickup-enabled` | `true` | Apply rotational/updraft velocity to nearby entities. |
| `physics.damage-enabled` | `true` | Enable direct periodic damage and debris entity damage. |
| `physics.base-damage` | `1.5` | Direct damage before the tier multiplier. |
| `physics.damage-interval-ticks` | `20` | Interval between direct damage applications. |
| `physics.blocks-per-tick` | `12` | Maximum sampled pickup attempts per tornado per tick. |
| `physics.variant-blocks-per-tick` | `2` | Maximum fire/freeze terrain attempts per tornado per tick. |
| `physics.entities-per-tick` | `48` | Maximum nearby entities processed per tornado per tick. |
| `physics.block-whitelist` | `[]` | If non-empty, only listed materials may be lifted. |
| `physics.block-blacklist` | see YAML | Materials never lifted in addition to hard-coded unbreakable/tile-state safety. |
| `effects.max-distance` | `96.0` | Player-local particle/sound cutoff and falloff distance. |
| `effects.particle-interval-ticks` | `2` | Particle rendering interval. |
| `effects.sound-interval-ticks` | `40` | Ambient sound interval. |
| `warnings.enabled` | `false` | Enable the optional one-time nearby chat cue. |
| `warnings.radius` | `64.0` | Current/projected-position recipient radius. |
| `warnings.message` | distant-rumble text | Minimal chat cue text. |
| `towny.destroy-towns` | `false` | Permit block pickup and elemental transformations in claims. |
| `towny.damage-players-in-towns` | `true` | Permit player damage when Towny's location PvP decision also allows it. |
| `towny.knockback-entities-in-towns` | `true` | Permit non-destructive vortex velocity in claims. |

### Type-level defaults

| Type | Enabled | Selection weight | Natural constraint | Extra behavior |
|---|---:|---:|---|---|
| `standard` | `true` | `60.0` | Any eligible loaded location | Baseline pickup and vortex. |
| `firenado` | `true` | `15.0` | Hot biome or nearby fire/lava | Fire trail and entity ignition. |
| `icenado` | `true` | `15.0` | Cold/snow/frozen biome | Freezing, slowness, water freezing. |
| `waterspout` | `true` | `10.0` | Water in ocean/river biome | 1.25x movement, downward pull, no blocks. |

Each type owns `types.<type>.tiers.<tier>`. Tier fields are `radius`, `movement-speed`, `lifespan-seconds`, `pickup-weight-limit`, `damage-multiplier`, `particle-density`, and `spawn-weight`.

Pickup weight uses this approximate scale: `1` foliage/plants, `2` soil/sand, `3` wood, `4` stone, `6` metals, and `99` obsidian-class blocks. Tile states and unbreakable/special blocks remain protected regardless of the value.

### Complete tier defaults

Columns: radius (`R`), movement speed (`S`), lifespan seconds (`L`), pickup weight limit (`P`), damage multiplier (`D`), particle density (`Fx`), and tier spawn weight (`W`).

| Type | Tier | R | S | L | P | D | Fx | W |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| Standard | Weak | 6 | 0.12 | 90 | 2 | 0.65 | 0.65 | 45 |
| Standard | Moderate | 8 | 0.15 | 120 | 3 | 0.9 | 0.85 | 30 |
| Standard | Strong | 11 | 0.18 | 150 | 4 | 1.2 | 1.0 | 16 |
| Standard | Severe | 14 | 0.21 | 180 | 6 | 1.6 | 1.25 | 7 |
| Standard | Violent | 18 | 0.24 | 210 | 8 | 2.2 | 1.5 | 2 |
| Firenado | Weak | 5 | 0.11 | 75 | 2 | 0.8 | 0.7 | 50 |
| Firenado | Moderate | 7 | 0.14 | 100 | 3 | 1.05 | 0.9 | 29 |
| Firenado | Strong | 9 | 0.17 | 130 | 4 | 1.4 | 1.1 | 14 |
| Firenado | Severe | 12 | 0.20 | 155 | 6 | 1.9 | 1.35 | 5.5 |
| Firenado | Violent | 15 | 0.23 | 180 | 8 | 2.6 | 1.65 | 1.5 |
| Icenado | Weak | 7 | 0.10 | 100 | 2 | 0.55 | 0.75 | 46 |
| Icenado | Moderate | 9 | 0.13 | 130 | 3 | 0.8 | 0.95 | 31 |
| Icenado | Strong | 12 | 0.16 | 160 | 4 | 1.1 | 1.15 | 15 |
| Icenado | Severe | 15 | 0.18 | 190 | 6 | 1.45 | 1.4 | 6 |
| Icenado | Violent | 19 | 0.20 | 220 | 8 | 2.0 | 1.7 | 2 |
| Waterspout | Weak | 6 | 0.14 | 80 | 0 | 0.7 | 0.7 | 52 |
| Waterspout | Moderate | 8 | 0.17 | 105 | 0 | 0.95 | 0.9 | 28 |
| Waterspout | Strong | 10 | 0.20 | 130 | 0 | 1.3 | 1.1 | 13 |
| Waterspout | Severe | 13 | 0.23 | 155 | 0 | 1.75 | 1.35 | 5.5 |
| Waterspout | Violent | 16 | 0.27 | 180 | 0 | 2.3 | 1.6 | 1.5 |

## Building with GitHub Actions

The [Build workflow](.github/workflows/build.yml) runs on every push to `main` and every pull request. It sets up Java 21, caches the local Maven repository, runs `mvn -B clean package`, and uploads `target/ObsidianWeather-*.jar` as the `ObsidianWeather` artifact for 14 days.

To obtain a development build:

1. Open the repository's **Actions** tab.
2. Select a successful **Build** run.
3. Download the **ObsidianWeather** artifact from the run summary.
4. Extract the ZIP and install the JAR.

This repository's documented local Flatpak development environment does not provide Maven. Build verification there is intentionally performed through GitHub Actions.

## Contributing

Keep changes small and focused, preserve the type-strategy and protection-integration boundaries, and update the config reference when adding tunables. Repository coding agents must follow [`AGENTS.md`](AGENTS.md); Claude Code must also follow [`CLAUDE.md`](CLAUDE.md).

## License

ObsidianWeather is available under the [MIT License](LICENSE). Copyright © 2026 Obsidian Modding.
