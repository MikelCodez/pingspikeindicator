# Architecture

Ping Spike Indicator is a client-only Fabric mod with a one-way dependency from game integration to a pure Java detection core.

```text
Minecraft client state
        |
        v
client adapter (src/client) -> plain ping/session input
        |                                  |
        |                                  v
        |                    detection core (src/main)
        |                                  |
        +----------- alert state <---------+
                        |
                        v
              lightweight HUD renderer
```

## Source sets

`src/main/java/dev/mikelcodez/pingspikeindicator` is reserved for pure Java models and detection behavior. It must compile conceptually without Minecraft or Fabric types: inputs and outputs are primitives, records, enums, or project-owned Java types.

`src/client/java/dev/mikelcodez/pingspikeindicator/client` owns the Fabric client entrypoint, reads client-visible connection/player-list state, manages session lifecycle, translates values for the core, and renders transient alerts. Minecraft, Fabric, and Mixin imports belong only here.

Resources shared in the mod artifact live under `src/main/resources`; client-only mixin configuration lives under `src/client/resources`.

## Runtime boundaries

- Observe existing client-visible ping and connection state only.
- Never change packet flow or create probe traffic.
- Reset detector and presentation state when a multiplayer session ends or changes.
- Keep stored samples bounded and detection updates constant-time.
- Render only a compact transient indication. No permanent dashboard or expensive visual effects.

The repository currently contains only the client initializer and bootstrap configuration. No detection or HUD feature is implemented yet.
