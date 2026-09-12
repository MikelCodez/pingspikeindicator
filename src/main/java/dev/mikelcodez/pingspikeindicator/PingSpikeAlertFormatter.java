package dev.mikelcodez.pingspikeindicator;

final class PingSpikeAlertFormatter {
	private PingSpikeAlertFormatter() {
	}

	static String format(int currentPingMillis, double baselineMillis) {
		if (currentPingMillis < 0 || !Double.isFinite(baselineMillis) || baselineMillis < 0) {
			throw new IllegalArgumentException("ping and baseline must be finite non-negative values");
		}

		long increaseMillis = Math.max(0, Math.round(currentPingMillis - baselineMillis));
		return currentPingMillis + " ms  (+" + increaseMillis + " ms)";
	}
}
