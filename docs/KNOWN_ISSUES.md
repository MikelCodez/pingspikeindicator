# Known issues and pre-publication checks

## PSI-001: Unfocused chat appears invisible in the development client

- Status: Configuration cause identified; not currently considered a Ping Spike Indicator defect
- Observed: 2026-09-12 during Phase 2 manual verification
- Symptom: Chat, command feedback, and advancement announcements were received but appeared invisible or nearly invisible until the chat interface was focused.
- Evidence: `run/logs/latest.log` contains the expected Minecraft chat and advancement messages. The development profile's ignored `run/options.txt` contains `chatVisibility:0` and `chatOpacity:0.0`. Ping Spike Indicator has no chat or HUD renderer in Phase 2.
- Workaround: In Minecraft, open Options > Chat Settings and raise Chat Text Opacity above zero.
- Before publication: Retest chat with a clean/default client profile and normal chat opacity. Escalate this to a release-blocking mod bug only if it reproduces with normal settings while Ping Spike Indicator is enabled and disappears when the mod is disabled.

Do not close a pre-publication check without recording the reproduction environment and whether the behavior occurs without this mod.
