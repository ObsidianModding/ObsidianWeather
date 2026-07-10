# CLAUDE.md

## Project overview

ObsidianWeather is a Java 21 Paper 1.21.11+ plugin adding dangerous tornadoes to vanilla thunderstorms. The Maven artifact is `net.obsidianmodding:obsidianweather`; the entry point is `net.obsidianmodding.obsidianweather.ObsidianWeatherPlugin`.

Read `AGENTS.md` before editing. It also applies to Claude Code.

## Build and contributor workflow

Run `mvn -B clean package` with Java 21 before pushing whenever Maven is available, then confirm `.github/workflows/build.yml`. If Maven is unavailable, use `git diff --check`, manual inspection, and CI, and do not claim a local package check succeeded.

Use a focused branch and pull request by default. Commit directly to `main` only when the maintainer explicitly requests a direct-to-main workflow. Fix shared CI history forward; never force-push it.

## Key files

| Area | Location under `src/main` |
|---|---|
| Lifecycle/scheduler | `java/net/obsidianmodding/obsidianweather/ObsidianWeatherPlugin.java` |
| Commands | `java/net/obsidianmodding/obsidianweather/commands/ObsidianWeatherCommand.java` |
| Parsed config | `java/net/obsidianmodding/obsidianweather/config/WeatherConfig.java` |
| Defaults | `resources/config.yml` |
| Paper metadata | `resources/plugin.yml` |
| Active manager | `java/net/obsidianmodding/obsidianweather/tornado/core/TornadoManager.java` |
| Tier/type logic | `java/net/obsidianmodding/obsidianweather/tornado/{tier,type}/` |
| Spawner/path/physics | `java/net/obsidianmodding/obsidianweather/tornado/{spawner,movement,physics}/` |
| Integrations | `java/net/obsidianmodding/obsidianweather/integration/` |

## Architecture

The main class runs one synchronous tick task. `TornadoSpawner` performs independent chance rolls at the configured interval. `TornadoManager` ages, moves, applies budgeted physics, and renders nearby-only effects.

Natural spawning chooses a loaded location near a player, filters types by environment, weights the type, then weights a tier from that type's own stats. `PathMovement` preserves heading with bounded turning and stops at unloaded chunks or the world border.

`TornadoTier` is identity; `TierStats` holds runtime numbers. `TornadoBehavior` strategies own eligibility, variant movement/effects, particles, and sounds. Shared lifecycle code must not be duplicated into strategies.

Per-combination config lives at `types.<type>.tiers.<tier>.<stat>`. Adding a type requires the enum, strategy, registry, fallback weight, complete five-tier config section, README update, and protection routing. Adding a tier requires every type's matrix plus fallback and docs.

WorldGuard/Towny are provided dependencies and softdepends. All third-party imports stay in isolated adapter packages. `IntegrationBootstrap` reflectively loads adapters; core physics calls only `ProtectionManager`.

## Gotchas

- Paper API: `1.21.11-R0.1-SNAPSHOT`; Java 21; `api-version: 1.21.11`.
- No tornado spawn cooldown. The check interval only schedules independent rolls.
- No boss bar, action bar, timer, siren, or global warning. Local chat warning defaults off.
- Never touch worlds asynchronously or force-load chunks.
- Keep effects player-local and distance-limited.
- Falling debris is tagged `obsidianweather-debris`, drops no items, and is not reprocessed.
- Tile states and unbreakable/special blocks stay protected.
- Every block, fire, knockback, and damage path queries protection first.
- WorldGuard uses public 7.x APIs and a non-member `BUILD` query.
- Towny defaults to no claim destruction; player damage checks PvP and fire checks the town toggle.

## Git conventions

- Normal work: focused branch, local Maven build when available, pull request, then CI.
- Maintainer exception: direct `main` only when explicitly requested.
- Preserve unrelated changes and commit one logical unit at a time.
- Fix CI forward; never rewrite shared history.
