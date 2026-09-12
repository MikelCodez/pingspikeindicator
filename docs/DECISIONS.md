# Decisions

## 2026-08-18: Client-only environment

The manifest uses `"environment": "client"` and exposes only a Fabric client entrypoint. The generated common initializer and server mixin were removed because the product has no server-side component.

## 2026-08-18: Pure detection core

Future detection behavior belongs in `src/main/java` and may depend only on the Java standard library and project-owned pure Java types. All Minecraft/Fabric integration remains in the client source set. This keeps policy deterministic and unit-testable without launching the game.

## 2026-08-18: Passive observation only

V1 will consume only ping and connection state already visible to the Minecraft client. Packet interception, mutation, delay, cancellation, synthetic probes, and gameplay automation are outside the product boundary.

## 2026-08-18: Lightweight transient presentation

The eventual warning will use ordinary Minecraft HUD primitives and appear only when useful. A permanent network dashboard, blur, custom shaders, and shader-heavy effects are excluded.

## 2026-08-18: Phase-gated delivery

Repository bootstrap establishes constraints and validation before product behavior. Phase 1 and every later phase require explicit authorization; code for later phases is not introduced early.

## 2026-08-18: Bounded rolling-mean detector

Phase 1 uses a fixed-capacity ring buffer with a running sum for an explainable, O(1) rolling-mean baseline. Samples that start or sustain a spike are not added, keeping the baseline stable through the event. A configurable absolute threshold starts a spike; a lower hysteresis boundary held for a caller-timed duration ends it. Missing, negative, configured extreme, and out-of-order observations are ignored. The model reports ping behavior only and makes no packet-loss or root-cause claim.

## 2026-09-11: Passive one-second client sampling

Phase 2 reads only the local player's latency already held in the client player list. A pure Java controller limits reads to one per second and resets the Phase 1 detector at connection and world boundaries. Fabric lifecycle events are sufficient, so no mixin or packet handler is used. Logging is limited to startup, the first accepted sample per session/world, and debug-only spike transitions.

## 2026-09-12: Fixed transient vanilla HUD layer

Phase 3 renders a compact top-center block for three seconds only when the detector emits `SPIKE_STARTED`. Alert text and layout width are cached rather than rebuilt every frame. The Fabric HUD layer is attached relative to vanilla chat so F1 visibility is inherited. Rendering uses only the vanilla font and filled rectangles; configurable placement, editors, shaders, blur, and framebuffer effects remain out of scope.

## 2026-09-12: Minimal versioned local configuration

Phase 4 uses a Java properties file with an explicit format version because five scalar settings do not justify another dependency. The pure Java loader validates the complete file and falls back to safe defaults on malformed, incomplete, unsupported-version, or out-of-range input. Writes use a sibling temporary file followed by a replacement move. The file lives in Fabric's client config directory and is touched only when the user closes the settings screen.

The configuration screen uses vanilla buttons and one bounded slider, opened by a rebindable `O` key. Mod Menu integration and a separate settings dependency are intentionally omitted from V1. Threshold changes replace and reset the detector so policy state is never mixed; duration and presentation changes remain independent. The optional small current-ping label is off by default, and no analytics, history, graph, drag editor, shader, or blur was added.
