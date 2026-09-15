# Changelog

All notable changes to Ping Spike Indicator are documented here.

## [1.0.0] - Unreleased

Initial Minecraft 1.21.11 release candidate.

### Added

- Client-only multiplayer ping sampling from Minecraft's existing player-list latency.
- Bounded rolling baseline with configurable absolute spike detection and hysteresis recovery.
- One lightweight, temporary Minecraft-style HUD warning per continuous spike.
- Current ping and increase-over-baseline alert text, with optional alert sound.
- Local versioned configuration for alerts, threshold, duration, sound, and an opt-in compact current-ping display.
- Safe fallback for missing, malformed, unsupported-version, and out-of-range configuration files.

### Technical

- Java 21 and Fabric for Minecraft 1.21.11.
- Pure Java detector and configuration tests with deterministic timing.
- No packet modification, synthetic traffic, server component, gameplay automation, analytics, shaders, or blur effects.
