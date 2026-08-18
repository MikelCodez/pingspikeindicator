# Testing

## Automated validation

Run the full validation from the repository root with Java 21:

```powershell
.\gradlew.bat build
```

The build compiles both Loom source sets, processes resources, runs unit tests, validates mappings/remapping, and creates the development and remapped artifacts.

Future pure Java detector tests belong in `src/test/java` and must not start Minecraft. Cover normal baselines, spike threshold boundaries, warm-up, sustained degradation, recovery, disconnect/session reset, missing or invalid ping values, and bounded-history behavior. Timing tests must use explicit timestamps or a fake clock rather than sleeps.

## Manual verification

When client functionality exists, verify in a Minecraft 1.21.11 client with Fabric Loader and Fabric API:

1. The mod loads and joins a multiplayer server without a server-side mod.
2. Normal latency does not leave a permanent HUD element.
3. A genuine, client-observed degradation produces the intended transient warning.
4. The warning clears according to the defined recovery behavior.
5. Disconnecting and changing servers clears prior session state.
6. Menus, chat, debug UI, HUD scaling, and common screen sizes remain legible and unobstructed.

Do not simulate degradation by modifying packets or generating traffic inside the mod. Record each performed check in `status.md`; unperformed checks remain pending.
