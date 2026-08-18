# Decisions

## 2026-08-18: Client-only environment

The manifest uses `"environment": "client"` and exposes only a Fabric client entrypoint. The generated common initializer and server mixin were removed because the product has no server-side component.

## 2026-08-18: Pure detection core

Future detection behavior belongs in `src/main/java` and may depend only on the Java standard library and project-owned pure Java types. All Minecraft/Fabric integration remains in the client source set. This keeps policy deterministic and unit-testable without launching the game.

## 2026-08-18: Passive observation only

V1 will consume only ping and connection state already visible to the Minecraft client. Packet interception, mutation, delay, cancellation, synthetic probes, and gameplay automation are outside the product boundary.

## 2026-08-18: Lightweight transient presentation

The eventual warning will use ordinary Minecraft HUD primitives and appear only when useful. A permanent network dashboard, blur, custom shaders, and shader-heavy effects are excluded.

## 2026-08-18: Phase-gated delivery

Repository bootstrap establishes constraints and validation before product behavior. Phase 1 and every later phase require explicit authorization; code for later phases is not introduced early.
