# Changelog

All notable changes to Ping Spike Indicator are documented here.

## [1.0.0] - 2026-09-15

Initial Minecraft 1.21.11 production release candidate.

### Added

- **Zero-Delay Ping Spike Detector**: Active 500ms ping probing loop for immediate detection of latency surges relative to rolling baselines, eliminating vanilla 30s delays.
- **Tactical Alert Banner**: Minecraft/Valorant-inspired HUD notification with spike difference, total ping, and optional pling sound alert.
- **Custom Pixel-Art Network Sprites**: 4 handcrafted 32x32 network sprites (Signal Bars, Wi-Fi Wave, Plug Disconnect, Satellite Ping).
- **Interactive Drag-and-Drop HUD Positioner**: `HudPositioningScreen` with real-time mouse drag repositioning and reset-to-defaults functionality.
- **Current Ping HUD Styling**: 3 visual modes (`Translucent`, `Text Only`, and `Fancy Accent`).
- **Persistent Accent Themes**: 6 selectable color themes (Cyan Blue, Emerald Green, Crimson Red, Gold Yellow, Pure White, Obsidian Black) persisted across game sessions.
- **Enhanced Tab Player List**: Custom ping rendering with options for `Both` (color-coded numbers + vanilla bars), `Numbers Only`, and `Vanilla Bars`.
- **Modern Blocky Settings UI**: Compact, semi-transparent settings menu with scroll support, scissor clipping, live alert test button, and custom slider controls.
- **Pure Java Architecture & Validation**: Strict architectural boundaries with 43 automated unit tests, zero reflection/unstable internals, and robust corrupt-file recovery.
