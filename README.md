# ⚡ Ping Spike Indicator

[![CurseForge](https://img.shields.io/badge/CurseForge-ping--spike--indicator-f16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ping-spike-indicator/)
[![Website](https://img.shields.io/badge/Website-pingspike.mikelcodez.xyz-FF6B1A)](https://pingspike.mikelcodez.xyz)
[![License: CC0-1.0](https://img.shields.io/badge/License-CC0_1.0-lightgrey.svg)](https://creativecommons.org/publicdomain/zero/1.0/)

**Ping Spike Indicator** is a lightweight, client-only Fabric mod that gives you immediate, tactical visual and audio warnings the moment your multiplayer ping suddenly spikes. It features live Tab list player ping numbers, an interactive click-and-drag HUD canvas, customizable themes, and zero network packet overhead.

🌐 **Live Interactive Web Demo & Downloads**: [https://pingspike.mikelcodez.xyz](https://pingspike.mikelcodez.xyz)  
🔥 **CurseForge Page**: [https://www.curseforge.com/minecraft/mc-mods/ping-spike-indicator/](https://www.curseforge.com/minecraft/mc-mods/ping-spike-indicator/)

---

## ✨ Key Features

- **Instant Spike Detection**: Real-time sliding-window latency tracking guarantees zero-lag notifications instead of waiting for vanilla Minecraft's sluggish 30-second tab ping refresh.
- **Tactical Alert Banner**: Valorant/Minecraft-styled HUD banner and optional audio chime when your ping exceeds your configured baseline threshold.
- **Interactive Click & Drag HUD Canvas**: Press your hotkey (`H` by default) to freely drag and drop the Alert Banner and Current Ping widget anywhere on your screen with coordinate snapping.
- **5 Accent Color Themes**: Tactical Cyan, Ember Orange, Toxic Lime, Void Purple, and Pure White.
- **Tab List Ping Display**: Displays exact real-time millisecond ping (`42 ms`, `128 ms`) for every player on the server.
- **Current Ping HUD Styling**:
  - `Translucent`: Clean dark background box.
  - `Text Only`: Minimalist borderless text.
  - `Fancy Accent`: Box framed with your chosen theme accent glow.
- **100% Client-Side & Tournament Safe**: Zero outbound networking packets sent to the server. Completely safe on Hypixel, 2b2t, Minemen, and competitive SMPs.

---

## 📦 Requirements & Supported Versions

- **Minecraft Versions**: `1.21.11` (Latest), `1.21 – 1.21.10` (Backport), `1.20.1` (Combat/Modpack Backport), `26.x`
- **Loader**: Fabric Loader (>= 0.16)
- **Dependencies**: Fabric API
- **Optional Integrations**: Mod Menu, Cloth Config

---

## 🚀 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for your Minecraft version.
2. Download `Fabric API` and `pingspikeindicator-1.0.0.jar` from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/ping-spike-indicator/) or [pingspike.mikelcodez.xyz](https://pingspike.mikelcodez.xyz).
3. Place both `.jar` files into your `.minecraft/mods` folder.
4. Launch Minecraft!

---

## 🎮 Controls & Configuration

Press `Right Shift` in-game (or configure via **Mod Menu**) to open the settings menu. Press `H` to open the interactive Drag & Drop HUD Positioning screen.

| Setting | Default | Options |
| --- | --- | --- |
| **Accent Theme** | Tactical Cyan | Tactical Cyan, Ember Orange, Toxic Lime, Void Purple, Pure White |
| **Visual Alerts** | On | On / Off |
| **Alert Icon** | Signal Bars | Signal Bars, Wi-Fi Wave, Warning, Satellite |
| **Spike Threshold** | 80 ms | 25–500 ms |
| **Alert Duration** | 3.0s | 1.0s – 10.0s |
| **Alert Sound** | On | On / Off |
| **Current Ping HUD** | Off | On / Off |
| **HUD Style** | Translucent | Translucent, Text Only, Fancy Accent |
| **Tab List Ping** | Both | Both, Numbers Only, Vanilla Bars |
| **HUD Position Canvas** | `H` Hotkey | Drag-and-drop elements with Reset button |

Settings are automatically saved locally in `config/pingspikeindicator.properties`.

---

## 🛠️ Building from Source

```powershell
# Windows
.\gradlew.bat build

# Linux / macOS
./gradlew build
```

Compiled JARs will be generated in `build/libs/`.

---

## 📄 License

This project is licensed under [CC0-1.0 Universal](LICENSE).
