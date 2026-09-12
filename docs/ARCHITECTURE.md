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
        |               |
        |               v
        |     lightweight HUD renderer
        v
versioned config store (src/main) <- native config screen (src/client)
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
- Render only a compact transient indication and, when explicitly enabled, one small current-ping label. No dashboard or expensive visual effects.

## Phase 1 detection model

`PingSpikeDetector` maintains a fixed-capacity ring of accepted baseline samples and a running sum, making insertion, eviction, and baseline updates O(1). The baseline is the arithmetic mean of that bounded history. Detection starts only after a configurable warm-up count.

A valid observation starts a spike when it is at least the configured absolute threshold above the current baseline. That sample and later samples during the spike are excluded from history, so an active spike cannot pull its own baseline upward. The detector emits `SPIKE_STARTED` only on the `NORMAL` to `SPIKING` transition.

Recovery requires observations below a separate lower boundary for a configured hold duration. Observation timestamps come from the caller; the core never reads a wall clock. Missing, negative, configured extreme, and out-of-order observations do not enter the baseline. `reset()` clears all per-session state.

The detector describes ping behavior only. It does not infer packet loss or network cause.

## Phase 2 client capture

`ClientPingCapture` uses Fabric client lifecycle events and an end-of-tick callback. While a remote multiplayer world is active and no screen is open, it reads the local player's existing player-list `PlayerInfo` latency no more than once per second. It does not register packet handlers or send, delay, cancel, or modify traffic.

The pure Java `PingSamplingController` owns cadence and session state, then passes accepted observations to the Phase 1 detector. Disconnects, connection changes, and client world changes reset the controller and detector. Singleplayer, menus, absent player state, and unavailable latency produce no capture marker or alert behavior.

Phase 2 logs one startup marker, one first-valid-sample marker per session/world, and debug-only spike transitions for manual verification.

## Phase 3 transient alert

`PingSpikeAlertState` accepts Phase 1 detector updates and changes only on `SPIKE_STARTED`. It preformats the current ping and increase over baseline once, retains them for the configured short duration, and clears on session/world reset. Sustained high samples carry no new signal, so they cannot restart the timer; a recovered and later spike can.

`PingSpikeHud` is a client-only Fabric HUD layer attached before vanilla chat. This placement inherits vanilla HUD visibility, including F1. While visible it draws strings and rectangles using the vanilla font; it uses cached strings and widths and performs no per-frame object creation in project code. There are no shaders, blur, framebuffer effects, or position editor.

## Phase 4 configuration

`PingSpikeConfig` validates the five V1 user options without depending on Minecraft. `PingSpikeConfigStore` persists them as a versioned properties file in Fabric's client config directory, using a temporary file and replacement move. Missing files use defaults; malformed, incomplete, unsupported-version, and out-of-range files safely fall back to defaults.

The client registers one rebindable key and builds the settings screen from vanilla buttons and a slider. Sampling is already suspended while any screen is open, and opening this screen dismisses a visible alert. A threshold change replaces and resets the detector, preventing a baseline or active spike created under the old policy from leaking into the new one. Other setting changes do not disturb detector state. Current-ping text is formatted only when the one-second sample is accepted and is rendered only when opted in.
