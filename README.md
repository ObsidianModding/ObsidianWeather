# ObsidianWeather

ObsidianWeather is a Paper plugin that adds dangerous, discoverable wind events to Minecraft weather. Tornadoes form during vanilla thunderstorms, while dust devils cross hot, dry biomes under clear skies. Every event follows a bounded semi-random path, affects configured blocks and entities, and dissipates naturally without server-wide alerts or artificial spawn cooldowns.

## Features

- Five independently tunable tiers: Weak, Moderate, Strong, Severe, and Violent.
- Five behaviorally distinct types:
  - **Standard:** baseline moving funnel, debris, rotational lift, and damage.
  - **Firenado:** forms in hot biomes or near fire/lava during thunder, burns entities, and starts policy-aware fires.
  - **Icenado:** forms in frozen biomes during thunder, applies freezing/slowness, extinguishes fire, and freezes water.
  - **Waterspout:** forms over ocean/river water during thunder, moves 25% faster, pulls entities downward, and never lifts land blocks.
  - **Dust Devil:** forms under clear skies in deserts, badlands, and savannas; moves 40% faster with a low rotational pull and blinding dust.
- A fresh weighted probability roll every configured interval, with no post-spawn cooldown.
- Per-world concurrency caps, loaded-chunk-only movement, and budgeted surface sampling.
- Clearly shaped, nearby-only particle funnels and sound with distance falloff.
- No boss bar, countdown, action bar, siren, or default chat warning. An optional local chat cue is off by default.
- Runtime-safe soft integrations for WorldGuard and Towny.
- Anonymous, server-owner-controllable usage metrics through bStats.
- Admin spawning, stopping, listing, reloading, and tab completion.

## Requirements

- Paper **1.21.11 or newer in the 1.21 line**
- Java **21**
- Maven when building from source

## Installation

1. Download an `ObsidianWeather-*.jar` from a successful GitHub Actions build artifact.
2. Stop the Paper server and place the JAR in `plugins/`.
3. Start the server once to create `plugins/ObsidianWeather/config.yml`.
4. Adjust the configuration and run `/obsidianweather reload`, or restart the server.

WorldGuard and Towny are optional. Do not install either plugin solely for ObsidianWeather.

## How spawning works

Every `spawn.check-interval-ticks`, each configured world below its event cap gets an independent probability roll. A candidate location is chosen in a loaded chunk near an online player. The plugin filters enabled types by current weather and environment, rolls an eligible type by weight, then rolls that type's tier using its own weights.

Standard tornadoes, firenadoes, icenadoes, and waterspouts require a vanilla thunderstorm. Dust devils instead require clear weather and a hot, dry biome. A successful spawn does not alter the next probability check; there is intentionally no “wait N minutes after an event” timer.

## Environmental awareness

Players normally discover events through their shaped funnels, debris, nearby particles, and local roar or elemental sounds. Effects are sent only within `effects.max-distance` and diminish with range.

`warnings.enabled` optionally sends one minimal chat message to players near a new funnel or its short projected path. It defaults to `false`, is never server-wide, and creates no persistent UI.

## Protection integrations

Both integrations are true `softdepend` entries. Their classes live in isolated packages and load only when the corresponding plugin and API are present.

| Integration | Behavior when installed |
|---|---|
| WorldGuard | Block changes, fire, knockback, and damage are denied wherever a non-member actor fails the region `BUILD` query. |
| Towny | Wilderness remains unaffected. Claimed blocks are protected by default; damage, knockback, and destruction are independently configured. Player damage respects location-aware PvP and firenado fire respects the town fire toggle. |

If both are installed, every integration must allow an action. Failed protection queries deny safely. Without them, ObsidianWeather follows its own configuration.

## Anonymous metrics

ObsidianWeather uses bStats plugin ID `32529` to collect bStats' standard anonymous server and plugin usage metrics. The library is bundled and relocated inside the plugin, so no separate bStats plugin is required. Server owners can disable metrics globally in `plugins/bStats/config.yml`; see the [bStats server-owner documentation](https://bstats.org/docs/server-owners) for details.

## Commands and permissions

All subcommands require `obsidianweather.admin`, which defaults to operators.

| Command | Description |
|---|---|
| `/obsidianweather spawn <world> [x z] [type] [tier]` | Spawn an event. Omitted type/tier values use weighted selection. Players may omit coordinates; console must provide them. |
| `/obsidianweather stop <id\|all>` | Stop one event by full/partial ID, or all events. |
| `/obsidianweather list` | List IDs, type, tier, world, position, and remaining lifetime. |
| `/obsidianweather reload` | Reload config. Existing events retain their spawn-time tier stats. |
| `/ow ...` | Short alias. |

Valid types: `standard`, `firenado`, `icenado`, `waterspout`, `dust-devil`. Valid tiers: `weak`, `moderate`, `strong`, `severe`, `violent`.

## Configuration reference

The bundled [`config.yml`](src/main/resources/config.yml) contains inline comments and is the source of truth.

### Global settings

| Path | Default | Meaning |
|---|---:|---|
| `spawn.check-interval-ticks` | `100` | Interval between independent weather-event chance rolls. |
| `spawn.chance-per-check` | `0.004` | Probability per eligible world and check. |
| `spawn.max-concurrent-per-world` | `2` | Hard per-world active cap. |
| `spawn.minimum-distance-from-player` | `48.0` | Minimum natural spawn distance. |
| `spawn.maximum-distance-from-player` | `112.0` | Maximum natural spawn distance. |
| `spawn.affected-worlds` | `['*']` | Exact world names, or `*` for all. |
| `physics.block-pickup-enabled` | `true` | Allow eligible block debris. |
| `physics.entity-pickup-enabled` | `true` | Apply vortex velocity. |
| `physics.damage-enabled` | `true` | Enable direct and debris damage. |
| `physics.base-damage` | `1.5` | Direct damage before tier scaling. |
| `physics.damage-interval-ticks` | `20` | Direct damage interval. |
| `physics.blocks-per-tick` | `12` | Pickup attempt budget per event. |
| `physics.variant-blocks-per-tick` | `2` | Fire/freeze attempt budget per event. |
| `physics.entities-per-tick` | `48` | Entity budget per event. |
| `physics.block-whitelist` | `[]` | Optional allowed-material list. |
| `physics.block-blacklist` | see YAML | Disallowed materials in addition to built-in safety. |
| `effects.max-distance` | `96.0` | Particle/sound cutoff and falloff distance. |
| `effects.particle-interval-ticks` | `2` | Particle interval. |
| `effects.sound-interval-ticks` | `40` | Ambient sound interval. |
| `warnings.enabled` | `false` | Enable the nearby one-time chat cue. |
| `warnings.radius` | `64.0` | Current/projected-position recipient radius. |
| `warnings.message` | distant-rumble text | Chat cue text. |
| `towny.destroy-towns` | `false` | Permit block/elemental changes in claims. |
| `towny.damage-players-in-towns` | `true` | Permit player damage when Towny PvP also allows it. |
| `towny.knockback-entities-in-towns` | `true` | Permit vortex velocity in claims. |

### Type defaults

| Type | Enabled | Weight | Natural constraint | Extra behavior |
|---|---:|---:|---|---|
| `standard` | `true` | `60.0` | Thunderstorm; any eligible loaded location | Baseline pickup/vortex. |
| `firenado` | `true` | `15.0` | Thunderstorm; hot biome or nearby fire/lava | Fire and ignition. |
| `icenado` | `true` | `15.0` | Thunderstorm; cold/snow/frozen biome | Freezing and slowness. |
| `waterspout` | `true` | `10.0` | Thunderstorm; ocean/river water | Faster, downward pull, no blocks. |
| `dust-devil` | `true` | `25.0` | Clear weather; desert/badlands/savanna | Faster, low spin, blindness. |

Each type owns `types.<type>.tiers.<tier>`. Fields are `radius`, `movement-speed`, `lifespan-seconds`, `pickup-weight-limit`, `damage-multiplier`, `particle-density`, and `spawn-weight`.

Pickup weights approximate: `1` foliage/plants, `2` soil/sand, `3` wood, `4` stone, `6` metals, `99` obsidian-class. Tile states and unbreakable/special blocks remain protected.

### Complete tier defaults

Columns: radius (`R`), movement (`S`), lifespan seconds (`L`), pickup limit (`P`), damage (`D`), particle density (`Fx`), tier weight (`W`). Type-specific movement multipliers apply after `S`.

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
| Dust Devil | Weak | 4 | 0.17 | 45 | 1 | 0.25 | 1.1 | 50 |
| Dust Devil | Moderate | 5 | 0.20 | 60 | 2 | 0.4 | 1.25 | 30 |
| Dust Devil | Strong | 7 | 0.23 | 75 | 2 | 0.6 | 1.4 | 14 |
| Dust Devil | Severe | 9 | 0.26 | 90 | 3 | 0.85 | 1.6 | 5 |
| Dust Devil | Violent | 11 | 0.30 | 105 | 3 | 1.15 | 1.8 | 1 |

## Building

Run `mvn -B clean package` with Java 21. The plugin JAR is written under `target/`.

The [Build workflow](.github/workflows/build.yml) runs on pushes to `main` and pull requests, caches Maven dependencies, packages the shaded plugin, and uploads `ObsidianWeather-*.jar` as the `ObsidianWeather` artifact for 14 days. Artifacts are available from successful runs in the repository's **Actions** tab.

Contributors should build locally with `mvn -B clean package` before pushing whenever Maven is available and still confirm CI. If Maven is unavailable in a particular environment, rely on GitHub Actions and do not claim a local build passed.

## Contributing

Keep changes focused, preserve type-strategy and protection-integration boundaries, and update config documentation when adding tunables. Contributors should use a focused branch, build locally when Maven is available, and submit a pull request. Direct pushes to `main` are reserved for the maintainer's explicitly requested workflow. Coding agents must follow [`AGENTS.md`](AGENTS.md); Claude Code must also follow [`CLAUDE.md`](CLAUDE.md).

## License

ObsidianWeather is available under the [MIT License](LICENSE). Copyright © 2026 Obsidian Modding.
