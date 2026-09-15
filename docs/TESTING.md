# Testing

## Automated validation

Run the full validation from the repository root with Java 21:

```powershell
.\gradlew.bat build
```

The build compiles both Loom source sets, processes resources, runs unit tests, validates the pure Java import boundary and mappings/remapping, and creates the development and remapped artifacts.

Pure Java detector tests live in `src/test/java` and do not start Minecraft. They cover stable baselines, sudden spikes, gradual drift, sustained degradation, recovery and a later spike, noisy threshold boundaries, invalid observations, session reset, and bounded history. Timing tests pass explicit timestamps rather than sleeping or reading the wall clock.

Run the core boundary check directly with:

```powershell
.\gradlew.bat verifyPureJavaBoundaries
```

The `check` lifecycle also runs `verifyClientOnlyMetadata`, which rejects a non-client environment or any common/server entrypoint in `fabric.mod.json`.

## Manual verification

Phase 1 is pure Java and has no Minecraft runtime behavior to verify. Its detector and the Phase 2 cadence/session controller are covered by deterministic unit tests. Phase 2 adds client integration and requires these Minecraft 1.21.11 checks with Fabric Loader and Fabric API:

1. The startup marker appears once when the client initializes.
2. Joining a multiplayer server without a server-side mod produces one first-valid-sample marker.
3. Singleplayer and menus produce no capture marker while inactive or paused.
4. Disconnecting, changing servers, and changing worlds reset capture state; the next valid sample produces one new marker.
5. No HUD element or gameplay behavior is present.

For Phase 3, use a multiplayer connection with a stable baseline for at least five accepted samples, then a natural or externally controlled latency increase of at least the configured threshold. The mod itself must not manipulate traffic. Verify:

1. One blocky `PING SPIKE` alert appears with the current ping and increase over baseline.
2. It disappears after the configured short duration.
3. Continued high latency does not restart or repeat it.
4. After recovery, a later new spike can display another alert.
5. F1 hides the alert along with the normal HUD.
6. No drag editor, shader, blur, or persistent panel is present.

For Phase 4, open the native settings screen with the rebindable `O` key while connected to multiplayer and verify:

1. Alerts can be disabled and re-enabled; disabled alerts produce neither the warning nor its sound.
2. The threshold slider stays between 25 and 500 ms and future detection uses the new threshold after a fresh five-sample warm-up.
3. The 2, 3, and 5 second duration presets visibly control later alerts.
4. The sound toggle controls the short alert sound.
5. The small current-ping display is absent by default, appears when enabled, updates at the sampling cadence, and follows F1 HUD visibility.
6. Opening the settings screen dismisses an active warning and no warnings begin while any screen is open.
7. Settings survive a client restart in `config/pingspikeindicator.properties`.
8. Replacing that file with invalid text does not crash startup and loads safe defaults.

Do not simulate degradation by modifying packets or generating traffic inside the mod. Record each performed check in `status.md`; unperformed checks remain pending.

## Phase 5 clean-instance release verification

Use a new Minecraft 1.21.11 game directory with only Fabric API and the built Ping Spike Indicator JAR installed. Do not copy the development profile's options or config. Verify:

1. The client reaches the title screen without errors, and `latest.log` lists Ping Spike Indicator 1.0.0 among the loaded mods.
2. A world opens in singleplayer without a warning, current-ping display, or repeated Ping Spike Indicator log messages.
3. Joining an unmodified multiplayer server requires no server-side mod and produces no disconnect or protocol warning.
4. After five or more stable one-second readings, a controlled external latency increase above 80 ms produces one readable top-center warning and one sound.
5. The warning shows current ping and increase, disappears after about three seconds, stays suppressed during the same spike, and can return after recovery and a later spike.
6. F1 hides the warning. Check GUI scales 1, 2, 3, 4, and Auto where available: text remains legible, on screen, and clear of the hotbar.
7. Pressing the default `O` key opens the native settings screen at those GUI scales; every control and Done button remains visible and usable.
8. Alerts off, sound off, the 25 ms and 500 ms threshold limits, all duration presets, and the opt-in current-ping display behave as labeled.
9. Settings survive a full restart. A deliberately malformed config falls back to defaults without preventing startup.
10. Disconnecting, changing servers, and changing dimensions clear visible/current-ping state and require a fresh baseline where applicable.
11. Chat is readable with default chat opacity. Confirm the development-only PSI-001 symptom does not reproduce on the clean profile or when the mod is removed.
12. `latest.log` contains at most the single startup INFO marker during normal play; session and spike diagnostics appear only when DEBUG logging is enabled.
