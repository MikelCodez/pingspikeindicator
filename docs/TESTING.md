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
