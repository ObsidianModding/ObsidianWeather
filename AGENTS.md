# AGENTS.md

These instructions apply to the entire ObsidianWeather repository.

## Project summary

ObsidianWeather is a Java 21 Paper plugin for Minecraft 1.21.11+. Its v1 feature is a configurable tornado system with four behavior strategies and five severity tiers. Tornadoes form from continuous chance rolls during vanilla thunderstorms, move through loaded chunks, affect sampled blocks and nearby entities, and communicate through local environmental effects.

Hard design constraints:

- Do not add a spawn cooldown. `spawn.check-interval-ticks` is a chance-roll interval, not a post-spawn delay.
- Do not add boss bars, action-bar countdowns, sirens, or server-wide warnings. The optional nearby chat cue must remain off by default.
- Never access Bukkit/Paper worlds or entities asynchronously. The current bounded work runs on the server thread.
- Do not force-load chunks for a tornado. Natural spawning and path movement stop or skip at unloaded chunks.
- WorldGuard and Towny must remain optional soft dependencies.

## Build and verification

The repository's local Flatpak development environment does not have Maven. Do not run or assume availability of `mvn`, and do not attempt to install Maven locally.

Verification happens through `.github/workflows/build.yml`:

1. Commit and push a focused change to `main`.
2. Inspect the GitHub Actions **Build** run.
3. If it fails, read the Actions log and make a new fix-forward commit.
4. Never rewrite or force-push history to hide a CI fix.

The workflow uses Java 21 and runs `mvn -B clean package`. It uploads the plugin JAR as an artifact.

When CI cannot be queried because `gh` is missing or unauthenticated, state that limitation. Still perform `git diff --check`, manual API reasoning, and focused source inspection; do not claim the build passed.

## Repository layout

```text
pom.xml
.github/workflows/build.yml
src/main/resources/
  plugin.yml                 Paper metadata, command, permission, softdepends
  config.yml                 commented defaults and all server tunables
src/main/java/net/obsidianmodding/obsidianweather/
  ObsidianWeatherPlugin.java lifecycle and one-tick scheduler bootstrap
  commands/                  /obsidianweather implementation
  config/                    immutable parsed config records/models
  tornado/
    core/                    active instance state, manager, effects, warnings
    tier/                    TornadoTier enum
    type/                    TornadoBehavior strategies and registry
    spawner/                 continuous thunderstorm chance checks
    movement/                bounded random walk with momentum
    physics/                 sampled block/entity effects and weight mapping
  integration/
    ProtectionIntegration    shared future-proof policy interface
    ProtectionManager        all-integrations-must-allow coordinator
    worldguard/              isolated WorldGuard API references
    towny/                   isolated Towny API references
```

## Coding conventions

- Use Java 21 language features only when they improve clarity.
- Keep classes focused and favor immutable records for parsed configuration.
- Use four-space indentation and braces for every control-flow block.
- Avoid wildcard imports and internal/NMS classes.
- Keep Paper world/entity calls on the main thread.
- Prefer fixed per-tick budgets over full-volume scans. New expensive work needs a config cap.
- Do not force chunk loads. Check `World#isChunkLoaded` before operating on a new chunk.
- Use player-targeted particles/sounds to preserve distance falloff and avoid global effects.
- All destructive, damaging, fire, and knockback paths must query `ProtectionManager` first.
- Treat integration query errors as deny-by-default and log them.
- Keep soft-dependency API types inside their isolated adapter package so class loading succeeds without the dependency.

## Type and tier architecture

`TornadoTier` identifies severity. Runtime numeric values do not live in the enum; `TierStats` loads them from the selected type's config matrix. This permits Standard/Weak and Firenado/Weak to have different values.

`TornadoBehavior` owns variant-specific policy and effects:

- natural spawn eligibility;
- block-pickup permission;
- movement multiplier;
- entity velocity modification and elemental effects;
- sampled block effect;
- particle and sound choices.

Shared movement, lifespan, physics, protection, and rendering stay outside behavior classes.

### Adding a tornado type

1. Add an enum constant and config key to `TornadoType`.
2. Add one `TornadoBehavior` implementation under `tornado/type/`.
3. Register it in `BehaviorRegistry`.
4. Add `enabled`, type `spawn-weight`, and all five tier sections under `types.<key>` in `config.yml`.
5. Add its default type-selection weight in `WeatherConfig`.
6. Update README type and tier tables.
7. Route every block/entity mutation through an existing or new `ProtectionAction`.
8. Commit the behavior separately from unrelated changes and verify through Actions.

### Adding a tier

1. Add the enum constant/config key in `TornadoTier`.
2. Add fallback values in `TierStats.defaultsFor`.
3. Add that tier to every type in `config.yml`; every type/tier pair must be explicit.
4. Update README's complete tier table.
5. Check weighted selection behavior and command tab completion.

## Configuration mapping

`WeatherConfig.load(FileConfiguration, Logger)` parses global settings and builds immutable `TypeSettings` maps. The path contract is:

```text
types.<type>.enabled
types.<type>.spawn-weight
types.<type>.tiers.<tier>.radius
types.<type>.tiers.<tier>.movement-speed
types.<type>.tiers.<tier>.lifespan-seconds
types.<type>.tiers.<tier>.pickup-weight-limit
types.<type>.tiers.<tier>.damage-multiplier
types.<type>.tiers.<tier>.particle-density
types.<type>.tiers.<tier>.spawn-weight
```

When adding a tunable, update all of these together:

- `src/main/resources/config.yml` with an inline comment and sane default;
- the immutable config model/parser;
- every consumer;
- README's configuration reference.

Reload replaces the current `WeatherConfig`. Existing tornadoes intentionally retain their spawn-time `TierStats`; global budgets and policy toggles are read from the current config each tick.

## Soft-dependency pattern

`plugin.yml` declares `softdepend: [WorldGuard, Towny]`. `IntegrationBootstrap` checks plugin enablement and reflectively constructs isolated adapters. The core only knows `ProtectionIntegration`, so missing third-party classes cannot prevent startup.

To add GriefPrevention, Residence, or another protection plugin:

1. Add its provided/compile-only Maven dependency and public repository.
2. Add the exact plugin name to `plugin.yml` softdepends.
3. Create `integration/<plugin>/<Plugin>Integration.java` implementing `ProtectionIntegration`.
4. Keep all third-party imports in that package.
5. Give the adapter a public constructor accepting `Supplier<WeatherConfig>`.
6. Register its plugin name and adapter class name in `IntegrationBootstrap`.
7. Add config toggles only when server-owner policy is needed.
8. Test startup both with and without the dependency through an actual Paper test server when available.

WorldGuard treats weather as a non-member actor and queries the `BUILD` flag. Towny distinguishes wilderness and claims, defaults to no town destruction, consults location PvP for player damage, and respects the town fire toggle.

## Git workflow

- Work directly on `main`; do not create feature branches or pull requests for repository-agent work.
- Preserve user changes and inspect `git status` before staging.
- Make one descriptive commit per logical unit.
- Stage explicit paths when unrelated changes exist.
- Push each commit, or a very small related batch, directly to `origin/main`.
- Fix CI forward with another commit. Never squash or force-push published fixes.
- Do not commit generated `target/` output, local server files, IDE settings, or secrets.
