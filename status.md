# Project status

- Current target: Minecraft 1.21.11, Fabric Loader 0.19.3, Fabric API 0.141.6+1.21.11, Java 21
- Current phase: Repository/bootstrap
- Completed phases: Repository/bootstrap documentation and client-only template cleanup
- Product implementation: Not started; Phase 1 has not been implemented

## Automated validation

- Java runtime/toolchain metadata: verified as Java 21
- Gradle wrapper: verified as Gradle 9.5.1
- Fabric Loom resolution: verified as 1.17.19 from the configured `1.17-SNAPSHOT`
- `.\gradlew.bat build --warning-mode all`: passed on 2026-08-18 (7 tasks executed)
- Unit tests: no test sources yet; Gradle reported `test NO-SOURCE`

## Manual verification

- In-game checks: not run; no product behavior exists yet
- Client launch smoke test: not run

## Blockers

- None for repository bootstrap.

## Next action

After explicit authorization, define the exact Phase 1 acceptance criteria and implement only that phase, beginning with deterministic tests for the pure Java detection core.
