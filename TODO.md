# TODO and Pull Request Ideas

This is an idea backlog, not a promised roadmap. Contributions are welcome, including ideas not listed here, but larger features should be discussed in an issue before implementation so their gameplay, configuration, and performance costs can be agreed on.

## More weather events

- [ ] Add hailstorms with local particles, configurable entity damage, and a strict per-tick impact budget.
- [ ] Add blizzards that reduce visibility, accumulate temporary snow where allowed, and interact naturally with cold biomes.
- [ ] Add sandstorms for deserts and badlands with wind, dust, and visibility effects distinct from dust devils.
- [ ] Add intense rain or flash-flood events without force-loading chunks or performing unbounded block scans.
- [ ] Add dry thunderstorms with lightning and fire risk in appropriate biomes, routed through protection integrations.
- [ ] Add heat waves and cold snaps as slower environmental events with configurable local effects.
- [ ] Explore rare supernatural or dimension-specific weather for the Nether and End while keeping each dimension recognizable.
- [ ] Design a shared weather-event lifecycle so non-tornado events can reuse spawning, loaded-chunk checks, protection policy, commands, and cleanup.

## Tornadoes and wind events

- [ ] Add more standard funnel profiles with clearly different silhouettes and bounded particle costs.
- [ ] Improve debris visuals while preserving block and entity budgets.
- [ ] Add configurable interactions between nearby compatible weather events.
- [ ] Expand biome-aware particles and sounds without adding server-wide warnings or persistent UI.
- [ ] Improve path selection around cliffs, caves, shorelines, and other difficult terrain.
- [ ] Add more type-specific environmental effects that always consult `ProtectionManager`.
- [ ] Explore an opt-in recovery mode for changed blocks, with durable state and safe restart behavior.

## Gameplay and configuration

- [ ] Add per-world configuration overrides while retaining sensible global defaults.
- [ ] Add finer allowlists and denylists for event types by world, biome, and dimension.
- [ ] Add configurable event-to-event spacing or density rules without turning the spawn interval into a cooldown.
- [ ] Add permissions for finer-grained command access.
- [ ] Add commands that explain why a natural event is or is not eligible at a location.
- [ ] Add a dry-run configuration validator with useful paths and error messages.
- [ ] Expose a small public API and Paper events for spawn, movement, impact, and dissipation.

## Integrations

- [ ] Add optional GriefPrevention support using the existing isolated soft-dependency pattern.
- [ ] Add optional Residence support using the existing isolated soft-dependency pattern.
- [ ] Add hooks for logging or rollback plugins without making them hard dependencies.
- [ ] Document integration behavior and provide test scenarios for overlapping protection plugins.

## Performance and reliability

- [ ] Add automated tests for configuration parsing, legacy rating aliases, and weighted selection.
- [ ] Add tests for weather, biome, dimension, and location eligibility for every event type.
- [ ] Add regression tests for protection routing and safe-deny behavior.
- [ ] Add lightweight timing metrics for physics and rendering budgets.
- [ ] Profile large multiplayer scenarios with several simultaneous events.
- [ ] Audit event cleanup across world unloads, plugin reloads, player disconnects, and server shutdowns.
- [ ] Add reproducible seeded simulations for movement and spawn-selection testing without touching Bukkit APIs asynchronously.

## Visuals, audio, and accessibility

- [ ] Improve particle readability at different view distances and graphics settings.
- [ ] Give each event a recognizable local sound profile with distance falloff.
- [ ] Add color-independent visual cues for elemental variants.
- [ ] Review local chat cues for clarity while keeping them optional and disabled by default.
- [ ] Document recommended server resource-pack hooks without requiring a resource pack.

## Documentation and contributor experience

- [ ] Add a developer architecture guide for event strategies, lifecycle, physics budgets, and protection routing.
- [ ] Add a guide for creating a new weather type from configuration through registration and documentation.
- [ ] Add example configurations for survival, lightweight, and high-chaos servers.
- [ ] Add a troubleshooting guide for event eligibility, protection plugins, and performance tuning.
- [ ] Add screenshots or short clips demonstrating each event and standard funnel profile.
- [ ] Create issue templates for bug reports, new weather proposals, integrations, and balance changes.

## Pull request checklist

Before opening a pull request:

- Keep the change focused and explain its gameplay goal.
- Discuss large features or new weather systems in an issue first.
- Keep all Bukkit and Paper world/entity access on the server thread.
- Never force-load chunks; stop or redirect work at unloaded chunk boundaries.
- Put a configurable hard budget on new per-tick scanning or effects.
- Check protection policy before destructive, fire, knockback, or damage actions.
- Keep optional dependencies isolated and safe when absent.
- Update `config.yml`, its parser/model, consumers, and `README.md` together for every new tunable.
- Add or update tests where practical.
- Run `mvn -B clean package` with Java 21 before pushing when Maven is available, then confirm the GitHub Actions **Build** result.

## Out of scope for the current design

- Spawn cooldowns; `spawn.check-interval-ticks` remains a probability-roll interval.
- Boss bars, action-bar countdowns, sirens, or server-wide event warnings.
- Asynchronous Bukkit/Paper world or entity access.
- Force-loading chunks for weather events.
- Making WorldGuard, Towny, or future protection integrations required dependencies.
