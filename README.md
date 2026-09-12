# Ping Spike Indicator

Ping Spike Indicator is a client-side Fabric mod that will warn a player when multiplayer ping suddenly becomes meaningfully worse. The warning is intended to be transient and Minecraft-like rather than a permanent network dashboard.

## Target

- Minecraft 1.21.11
- Fabric Loader 0.19.3 and Fabric API 0.141.6+1.21.11
- Java 21
- Mod ID: `pingspikeindicator`
- Base package: `dev.mikelcodez.pingspikeindicator`

## Current state

Phase 4 adds a small in-game settings screen, opened with the rebindable `O` key by default. Alerts, spike threshold, alert duration, sound, and an optional compact current-ping readout can be configured; the current-ping readout is off by default. Settings are stored locally in a versioned client properties file.

## Build

On Windows:

```powershell
.\gradlew.bat build
```

On macOS or Linux:

```sh
./gradlew build
```

See [Architecture](docs/ARCHITECTURE.md), [Testing](docs/TESTING.md), [Decisions](docs/DECISIONS.md), [Known issues](docs/KNOWN_ISSUES.md), and [project status](status.md) for the implementation constraints and current progress.

## License

This project is available under CC0-1.0. See [LICENSE](LICENSE).
