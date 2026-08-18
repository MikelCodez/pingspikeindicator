# Ping Spike Indicator contributor guide

## Scope

Build a lightweight, client-side Fabric mod that warns the local player when client-visible multiplayer ping suddenly becomes meaningfully worse. The initial target is Minecraft 1.21.11 on Java 21, using mod ID `pingspikeindicator` and base package `dev.mikelcodez.pingspikeindicator`.

V1 may only observe state already visible to the client. It must not modify, delay, cancel, or inject packets; generate artificial network traffic; require a server component; automate gameplay; or keep a large permanent network HUD on screen.

## Architecture boundaries

- Put detection policy, immutable samples, state transitions, and other pure Java logic in `src/main/java`. Code in that source tree must have no Minecraft, Fabric, Mixin, rendering, or other game-runtime imports.
- Put every Minecraft/Fabric hook, client-state adapter, lifecycle callback, and renderer in `src/client/java`.
- Keep the mod client-only in `fabric.mod.json`. Do not add a common or server entrypoint.
- Pass plain Java values from client adapters into the detection core. Do not expose Minecraft objects to the core.
- Prefer Fabric client APIs over mixins. Add a narrowly targeted client mixin only when the API cannot provide the required hook, and record the reason in `docs/DECISIONS.md`.
- Keep state per connection/session. Clear it on disconnect or world/server change.
- Treat later roadmap phases as out of scope until the current phase is completed and the next phase is explicitly authorized. Do not implement later-phase features early, even as optional settings or speculative abstractions.

## Testing and validation

- Add deterministic unit tests for pure detection logic, including threshold boundaries, warm-up, recovery, reset, invalid/missing samples, and bounded history.
- Keep pure tests independent of Minecraft/Fabric and real time; inject or pass time when timing matters.
- Add client integration tests only where they provide value beyond unit coverage.
- For every change, run `./gradlew build` (or `.\gradlew.bat build` on Windows). Run narrower tests during iteration when useful, but finish with the full build.
- Record automated results and any required in-game checks in `status.md`. Do not mark manual verification complete unless it was actually performed.
- A phase is not complete while relevant tests fail, the build fails, or its required manual checks remain unresolved.

## Performance goals

- Sampling and detection must be O(1) per update with strictly bounded retained history.
- Read ping no more than once per client tick; prefer a lower fixed sampling cadence when responsiveness permits.
- Avoid work and allocations in render callbacks when no warning is visible.
- Keep rendering to simple text, sprites, and rectangles; do not add blur, custom shaders, or shader-heavy effects.
- Perform no network I/O beyond Minecraft's existing traffic and no routine disk I/O during gameplay.
- Log transitions or exceptional conditions, not every sample or frame.

## Change discipline

- Update `status.md` as phase state or validation changes.
- Record durable architectural choices and meaningful reversals in `docs/DECISIONS.md`.
- Keep documentation consistent with Gradle and `fabric.mod.json` metadata.
- Do not commit unless the user explicitly asks.
