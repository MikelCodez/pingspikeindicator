# Development Roadmap & Feature Specification

This roadmap details the planned post-v1.0 enhancements for Ping Spike Indicator, organized into logical, reviewable phases.

---

## Phase Breakdown

### Phase 5.3: Alert Banner Customization & Sprites
- **Objective**: Transform the in-game HUD alert banner to support custom pixel-art sprites and customizable audio.
- **Features**:
  - Render selectable 32x32 tactical pixel-art network alert sprites (Signal Bars, Satellite, Cable Disconnect, Wi-Fi Wave).
  - Option to toggle between Sprite + Text mode, Compact Icon-only mode, or Text-only mode.
  - Configurable alert sound cue (Modern Pop/Click, Note Block Pling, Muted Warning Chime, or Silent).
  - HUD position anchor selector (Top-Center, Top-Right, Top-Left) to avoid overlaps with mini-maps or status effects.

### Phase 5.4: Dynamic Latency Colors & Everyday Ping HUD
- **Objective**: Enhance the persistent current-ping readout for everyday gameplay.
- **Features**:
  - Dynamic latency color grading:
    - 🟢 `< 60 ms`: Emerald Green (Smooth / optimal)
    - 🟡 `60–120 ms`: Gold / Yellow (Average / normal)
    - 🟠 `120–200 ms`: Amber (Noticeable delay)
    - 🔴 `> 200 ms`: Crimson Red (Severe lag)
  - Optional compact 4-bar signal strength meter widget next to the number.
  - Position selector matching the alert banner or independent corner placement.

### Phase 5.5: Smart Gameplay Filtering & Two-Stage Spike Alerts
- **Objective**: Eliminate false alarms during normal world loading and provide tiered spike severity.
- **Features**:
  - Dimension & Teleport Grace Period: 3–5 second alert suppression after joining a server or traveling between Nether/End/Overworld.
  - Two-Stage Spike Alert:
    - *Moderate Spike* (+80 ms default): Standard amber warning banner.
    - *Critical Freeze / Hang* (+250 ms or 2+ consecutive dropped seconds): Blazing red warning with hazard diamond.

### Phase 5.6: Smooth Micro-Animations
- **Objective**: Add responsive, high-framerate micro-interactions to the UI.
- **Features**:
  - Settings Toggle Slide: 100–120 ms smooth sliding animation for the square switch knob.
  - HUD Alert Entrance/Exit: Smooth vertical slide-down on trigger and gentle slide-up/fade on dismissal.

### Phase 5.7: Mod Menu Integration
- **Objective**: Standard Fabric ecosystem integration.
- **Features**:
  - Implement `ModMenuApi` client entrypoint.
  - Open `PingSpikeConfigScreen` directly from the Fabric Mod Menu screen.

---

## Sprite Design Guide for Artists / Non-Artists

### 1. Canvas Size & Resolution
- **Canvas Size**: **32 × 32 pixels** (with transparent background).
- **Why 32x32?** 
  - Standard Minecraft 16x16 is too tight to fit both a recognizable network icon AND a distinct red exclamation hazard diamond without pixel distortion.
  - 32x32 gives clean, crisp silhouettes that match modern Minecraft GUI scales perfectly (rendered at 16x16 or 24x24 display pixels on HUD).

### 2. Styling Rules
- **Pixel-Art Aesthetic**: Sharp right angles and clean diagonals. No anti-aliased blur or 3D shading.
- **Primary Silhouette**: Solid white (`#FFFFFF`) with subtle inner shading (`#D8D8D8`) if desired.
- **Outer Outline**: A 1-pixel dark charcoal/black border (`#12141A` or `#000000`). This is crucial so the icon remains clearly visible against white snow, bright skies, or dark caves.
- **Hazard Diamond**: A tilted square / diamond in vivid crimson red (`#FF3344` or `#E74C3C`) with a 1-pixel dark border and a white exclamation mark (`!`) in the center.

### 3. Required Sprite List & Filenames

All sprites belong in:
`src/client/resources/assets/pingspikeindicator/textures/gui/sprites/alert/`

| Filename | Dimensions | Theme / Concept | Details |
| :--- | :---: | :--- | :--- |
| `signal_bars_alert.png` | 32 × 32 | High Average Ping / Ping Spike | 4 ascending white signal bars (left to right) with an overlapping red hazard diamond containing a white exclamation mark (`!`). |
| `satellite_alert.png` | 32 × 32 | Packet Drop / Network Problem | An angled white satellite dish receiver with a red hazard diamond. |
| `plug_disconnect.png` | 32 × 32 | Connection Stall / Cable Issue | Two angled Ethernet/power cable plugs pulled apart with an amber/yellow lightning spark (`#FFAA00`) between them. |
| `wifi_alert.png` | 32 × 32 | Wi-Fi Latency Wave | 3 concentric white arched signal waves with a red exclamation diamond at the base. |

### 4. Normal (Non-Alert) Variants (Optional / Future Use)
For HUD signal meters or settings icons without the red hazard mark:
- `signal_bars.png`: 4 clean white bars (no diamond).
- `satellite.png`: Clean white satellite dish.
- `plug_connected.png`: Connected cable plugs.
- `wifi.png`: Clean 3-wave Wi-Fi arch.
