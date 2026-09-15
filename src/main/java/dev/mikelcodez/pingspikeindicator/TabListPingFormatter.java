package dev.mikelcodez.pingspikeindicator;

/**
 * Pure Java utility for formatting latency values and determining tier colors for Tab list rendering.
 */
public final class TabListPingFormatter {
	// Crisp dynamic color coding ARGB
	public static final int COLOR_UNKNOWN = 0xFFAAAAAA; // Gray
	public static final int COLOR_EXCELLENT = 0xFF55FF55; // Crisp Bright Green (<60ms)
	public static final int COLOR_GOOD = 0xFFFFFF55;      // Crisp Yellow (60-120ms)
	public static final int COLOR_MODERATE = 0xFFFFAA00;  // Crisp Gold/Amber (120-200ms)
	public static final int COLOR_POOR = 0xFFFF5555;      // Crisp Red (>200ms)

	private TabListPingFormatter() {
	}

	/**
	 * Formats a latency integer into a readable string:
	 * - Negative values (< 0): "?"
	 * - 0 ms: "<1ms"
	 * - > 0 ms: "%dms"
	 */
	public static String formatPing(int latencyMs) {
		if (latencyMs < 0) {
			return "?";
		}
		if (latencyMs == 0) {
			return "<1ms";
		}
		return latencyMs + "ms";
	}

	/**
	 * Resolves the ARGB color integer for a given latency:
	 * - < 0: Gray (#AAAAAA)
	 * - < 60ms: Green (#55FF55)
	 * - 60-119ms: Yellow (#FFFF55)
	 * - 120-199ms: Amber (#FFAA00)
	 * - >= 200ms: Red (#FF5555)
	 */
	public static int pingColor(int latencyMs) {
		if (latencyMs < 0) {
			return COLOR_UNKNOWN;
		}
		if (latencyMs < 60) {
			return COLOR_EXCELLENT;
		}
		if (latencyMs < 120) {
			return COLOR_GOOD;
		}
		if (latencyMs < 200) {
			return COLOR_MODERATE;
		}
		return COLOR_POOR;
	}
}
