# Ping Spike Indicator

Ping Spike Indicator is a lightweight, client-only Fabric mod that gives you immediate, tactical visual and audio warnings the moment your multiplayer ping suddenly spikes. It also features live Tab list ping numbers, customizable HUD positioning with click-and-drag, and multiple visual styles.

## Key Features

- **Instant Spike Detection**: Active 500ms ping probing guarantees zero-lag notifications instead of waiting for vanilla Minecraft's 30-second tab ping refresh.
- **Tactical Alert Banner**: Sleek Valorant/Minecraft-styled HUD banner with sound alert when your ping exceeds your configured baseline threshold.
- **4 Custom Pixel-Art Network Sprites**: Choose from Signal Bars, Wi-Fi Wave, Disconnected Plug, and Satellite Ping.
- **Interactive Click & Drag HUD Positioner**: Freely move the Alert Banner and Current Ping element anywhere on your screen.
- **Current Ping HUD Styling**:
  - `Translucent`: Clean 50% opacity dark obsidian background.
  - `Text Only`: Borderless, pure text HUD.
  - `Fancy Accent`: Translucent background framed with your chosen accent color.
- **6 Accent Color Themes**: Cyan Blue, Emerald Green, Crimson Red, Gold Yellow, Pure White, and Obsidian Black (saved persistently across restarts).
- **Tab List Ping Display**:
  - `Both`: Side-by-side color-coded numbers + vanilla bars.
  - `Numbers Only`: Clean millisecond numbers.
  - `Vanilla Bars`: Standard latency icon.
- **Client-Only & Server-Friendly**: 100% client-side. Requires no server-side mod installation. Safe for vanilla and competitive servers.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.19.3 or newer
- Fabric API 0.141.6+1.21.11
- Java 21

## Installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Place Fabric API and `pingspikeindicator-1.0.0.jar` into your `.minecraft/mods` folder.
3. Launch Minecraft and join any multiplayer server!

## Configuration

Press `O` in-game (or type `/pingspike`) to open the settings menu. You can rebind the hotkey anytime in Minecraft's Controls menu.

| Setting | Default | Options |
| --- | --- | --- |
| **Accent Theme** | Cyan Blue | Cyan Blue, Emerald Green, Crimson Red, Gold Yellow, Pure White, Obsidian Black |
| **Visual Alerts** | On | On / Off |
| **Alert Icon** | Signal Bars | Signal Bars, Wi-Fi Wave, Plug Disconnect, Satellite Ping |
| **HUD Anchor Position** | Top Center | Top Center, Top Left, Top Right |
| **Spike Threshold** | 80 ms | 25–500 ms (in 5 ms increments) |
| **Alert Duration** | 3 seconds | 2, 3, or 5 seconds |
| **Alert Sound** | On | On / Off |
| **Current Ping HUD** | Off | On / Off |
| **HUD Style** | Translucent | Translucent, Text Only, Fancy Accent |
| **Tab List Ping** | Both | Both, Numbers Only, Vanilla Bars |
| **Customize HUD Positions** | — | Interactive click-and-drag screen editor with snap-to-default Reset |

Settings are automatically saved locally in `config/pingspikeindicator.properties`.

## Build

To compile from source:

On Windows:
```powershell
.\gradlew.bat clean build
```

On macOS or Linux:
```sh
./gradlew clean build
```

The compiled mod JAR will be located at `build/libs/pingspikeindicator-1.0.0.jar`.

## License

This project is licensed under CC0-1.0. See [LICENSE](LICENSE).
