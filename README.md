# Ping Spike Indicator

Ping Spike Indicator is a client-side Fabric mod that will warn a player when multiplayer ping suddenly becomes meaningfully worse. The warning is intended to be transient and Minecraft-like rather than a permanent network dashboard.

## Target

- Minecraft 1.21.11
- Fabric Loader 0.19.3 and Fabric API 0.141.6+1.21.11
- Java 21
- Mod ID: `pingspikeindicator`
- Base package: `dev.mikelcodez.pingspikeindicator`

## Current state

Repository bootstrap is complete. Product detection and HUD behavior have not been implemented.

## Build

On Windows:

```powershell
.\gradlew.bat build
```

On macOS or Linux:

```sh
./gradlew build
```

See [Architecture](docs/ARCHITECTURE.md), [Testing](docs/TESTING.md), [Decisions](docs/DECISIONS.md), and [project status](status.md) for the implementation constraints and current progress.

## License

This project is available under CC0-1.0. See [LICENSE](LICENSE).
