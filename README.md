# Ping Spike Indicator

Ping Spike Indicator is a lightweight, client-only Fabric mod that warns you when multiplayer ping suddenly becomes meaningfully worse. Its Minecraft-style warning disappears automatically instead of occupying the screen as a permanent network dashboard.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.3 or newer for Minecraft 1.21.11
- Fabric API 0.141.6+1.21.11
- Java 21

This release targets Minecraft 1.21.11 only. The mod is installed only on the client; multiplayer servers do not need it.

## Installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Put Fabric API and `pingspikeindicator-1.0.0.jar` in the client's `mods` folder.
3. Start the Fabric client and join a multiplayer server.

## Usage

The detector quietly builds its first baseline from five valid ping readings, sampled once per second. A warning then appears only when ping rises by at least the configured threshold. Continued high ping does not repeat the warning; after recovery, a later new spike can warn again.

Press `O` in game to open settings. The key can be changed under Minecraft's Controls menu.

| Setting | Default | Available values |
| --- | --- | --- |
| Alerts | On | On or off |
| Spike threshold | 80 ms | 25–500 ms in 5 ms steps |
| Alert duration | 3 seconds | 2, 3, or 5 seconds |
| Alert sound | On | On or off |
| Current ping display | Off | On or off |

F1 hides both the transient warning and the optional current-ping display with the rest of the normal HUD. Settings are stored locally in `config/pingspikeindicator.properties`. Invalid configuration files safely fall back to defaults.

The mod observes only latency already shown to the client. It does not alter packets, generate probe traffic, diagnose packet loss, or claim a cause for a spike.

## Build

On Windows:

```powershell
.\gradlew.bat clean build
```

On macOS or Linux:

```sh
./gradlew clean build
```

The distributable JAR is written to `build/libs/pingspikeindicator-1.0.0.jar`.

See the [changelog](CHANGELOG.md), [architecture](docs/ARCHITECTURE.md), [testing guide](docs/TESTING.md), [decisions](docs/DECISIONS.md), [known issues](docs/KNOWN_ISSUES.md), and [project status](status.md) for release details and implementation constraints.

## License

This project is available under CC0-1.0. See [LICENSE](LICENSE).
