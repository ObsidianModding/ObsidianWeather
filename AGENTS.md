# AGENTS.md

These instructions apply to the entire repository.

## Project and non-negotiable design

ObsidianWeather is a Java 21 Paper 1.21.11+ plugin. V1 implements five wind-event strategies and six Fujita ratings, F0 through F5.

- Do not add a spawn cooldown. `spawn.check-interval-ticks` is a probability-roll interval.
- Do not add boss bars, action-bar countdowns, sirens, or server-wide warnings. The optional local chat cue stays off by default.
- Never use Bukkit/Paper world or entity APIs asynchronously.
- Never force-load chunks for a weather event.
- Keep WorldGuard and Towny optional soft dependencies.
- Standard tornadoes, overworld firenadoes, icenadoes, and waterspouts require thunder.
- Nether firenadoes do not require thunder. Dust devils require clear weather and a hot, dry biome.

## Build and verification

Use Java 21 and run `mvn -B clean package` before pushing whenever Maven is available. If Maven is unavailable in a particular environment, do not try to install it without authorization and do not claim a local Maven build ran; use `git diff --check`, manual source inspection, and `.github/workflows/build.yml` for authoritative verification.

For all environments:

1. Make a focused change.
2. Run `mvn -B clean package` when Maven is available.
3. Commit and push using the appropriate contributor workflow.
4. Inspect the GitHub Actions **Build** result.
5. Fix failures forward; never rewrite shared history to hide CI fixes.

If Actions cannot be queried, report that limitation and do not claim success.

## Layout

```text
pom.xml
.github/workflows/build.yml
src/main/resources/
  plugin.yml                 Paper metadata, command, permission, softdepends
  config.yml                 commented tunables and all type/rating defaults
src/main/java/net/obsidianmodding/obsidianweather/
  ObsidianWeatherPlugin.java lifecycle and synchronous scheduler
  commands/                  admin command
  config/                    immutable parsed configuration
  tornado/core/             instance, manager, funnel profiles, effects, local warning
  tornado/tier/             Fujita rating identity
  tornado/type/             behavior strategies and registry
  tornado/spawner/          continuous weather/dimension-eligibility rolls
  tornado/movement/         bounded momentum path and Nether ground locator
  tornado/physics/          budgeted block/entity effects
  integration/              shared policy plus isolated adapters
  metrics/                  isolated bStats bootstrap
```

## Coding conventions

- Use focused classes, immutable config records, four-space indentation, and no wildcard imports.
- Avoid NMS/internal APIs.
- Keep world/entity work synchronous.
- Prefer hard per-tick budgets to full-volume scans; new expensive work needs a tunable cap.
- Check chunk loading before crossing into another chunk.
- Keep effects player-local with distance falloff.
- Query `ProtectionManager` before every destructive, fire, knockback, or damage action.
- Deny safely and log when an integration query fails.
- Keep third-party imports inside isolated adapter/bootstrap packages.

## Rating/type/profile architecture

`TornadoTier` is the Fujita rating identity. `TierStats` loads numeric values from the selected type's config matrix, so all 30 combinations may differ. Legacy `weak`, `moderate`, `strong`, `severe`, and `violent` keys map to F0–F4 for existing configurations; F5 has no legacy alias.

`TornadoBehavior` owns natural weather/dimension and location eligibility, block-pickup permission, movement multiplier, variant entity/block effects, particles, and sounds. Shared lifecycle, pathing, protection, physics, and rendering remain outside strategies.

`FunnelProfile` controls standard-tornado geometry. Standard instances randomly select classic cone, rope, stovepipe, wedge, or multi-vortex. Type strategies still control the particle palette.

To add a type:

1. Add a `TornadoType` constant and strategy.
2. Register it in `BehaviorRegistry`.
3. Add `enabled`, type weight, and all six Fujita sections to `config.yml`.
4. Add the fallback type weight in `WeatherConfig`.
5. Update README tables and protection routing.

To add a rating:

1. Extend `TornadoTier` and `TierStats.defaultsFor`.
2. Add it beneath every type in `config.yml`.
3. Update README and confirm weighted selection/tab completion.

To add a standard profile:

1. Extend `FunnelProfile` with distinct shell width, height, strand, centerline, and ground-ring behavior.
2. Confirm its particle cost stays inside the existing per-player rendering cap.
3. Update the README profile list.

## Configuration mapping

Per-combination values map as follows:

```text
types.<type>.enabled
types.<type>.spawn-weight
types.<type>.tiers.<rating>.radius
types.<type>.tiers.<rating>.movement-speed
types.<type>.tiers.<rating>.lifespan-seconds
types.<type>.tiers.<rating>.pickup-weight-limit
types.<type>.tiers.<rating>.damage-multiplier
types.<type>.tiers.<rating>.particle-density
types.<type>.tiers.<rating>.spawn-weight
```

Every new tunable requires synchronized YAML, parser/model, consumer, and README changes. Reload replaces global config; active events intentionally retain spawn-time `TierStats`.

## Dimension and ground routing

`GroundLocator` uses the surface heightmap outside the Nether. In the Nether it searches near the player/event's current Y for loaded, open cave-level ground, avoiding the bedrock roof. Natural spawning, manual spawning, path movement, block pickup, and variant block effects must all use this shared locator.

The Nether chance is `spawn.chance-per-check * spawn.nether-firenado-chance-multiplier`, capped at `1.0`. Do not turn this into a separate timer or cooldown.

## Soft-dependency pattern

`plugin.yml` declares WorldGuard and Towny as softdepends. `IntegrationBootstrap` checks plugin availability and reflectively constructs isolated adapters. Core physics knows only `ProtectionIntegration`.

For future GriefPrevention/Residence support:

1. Add a provided dependency and repository.
2. Add the exact plugin name to `plugin.yml` softdepends.
3. Implement `ProtectionIntegration` in an isolated package with a public `Supplier<WeatherConfig>` constructor.
4. Register the adapter class name in `IntegrationBootstrap`.
5. Verify startup with and without the dependency.

WorldGuard queries `BUILD` as a non-member actor. Towny protects claims by default, checks location PvP for player damage, and respects the town fire toggle.

## Git workflow

- Normal contributors and automated agents should use a focused branch and pull request.
- Work directly on `main` only when the maintainer explicitly requests a direct-to-main workflow.
- Preserve unrelated worktree changes and stage explicit paths.
- Make one descriptive commit per logical unit.
- In a maintainer-authorized direct workflow, push small commits to `origin/main`; otherwise push the contributor branch.
- Fix CI forward. Do not squash or force-push shared history.
- Do not commit `target/`, local server files, IDE settings, or secrets.
