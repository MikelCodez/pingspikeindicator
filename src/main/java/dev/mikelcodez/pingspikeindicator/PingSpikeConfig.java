package dev.mikelcodez.pingspikeindicator;

/**
 * Validated, client-persisted user options expressed without game-runtime types.
 */
public record PingSpikeConfig(
		boolean alertsEnabled,
		int spikeThresholdMillis,
		int alertDurationMillis,
		boolean soundEnabled,
		boolean currentPingVisible
) {
	public static final int FORMAT_VERSION = 1;
	public static final int MINIMUM_THRESHOLD_MILLIS = 25;
	public static final int MAXIMUM_THRESHOLD_MILLIS = 500;
	public static final int THRESHOLD_STEP_MILLIS = 5;
	public static final int DEFAULT_THRESHOLD_MILLIS = 80;
	public static final int DEFAULT_ALERT_DURATION_MILLIS = 3_000;
	private static final int[] ALERT_DURATION_PRESETS_MILLIS = {2_000, 3_000, 5_000};

	public PingSpikeConfig {
		if (spikeThresholdMillis < MINIMUM_THRESHOLD_MILLIS
				|| spikeThresholdMillis > MAXIMUM_THRESHOLD_MILLIS) {
			throw new IllegalArgumentException("spikeThresholdMillis is outside the supported range");
		}
		if (!isAllowedAlertDuration(alertDurationMillis)) {
			throw new IllegalArgumentException("alertDurationMillis is not a supported preset");
		}
	}

	public static PingSpikeConfig defaults() {
		return new PingSpikeConfig(true, DEFAULT_THRESHOLD_MILLIS, DEFAULT_ALERT_DURATION_MILLIS, true, false);
	}

	public PingSpikeConfig withAlertsEnabled(boolean enabled) {
		return new PingSpikeConfig(enabled, spikeThresholdMillis, alertDurationMillis, soundEnabled, currentPingVisible);
	}

	public PingSpikeConfig withSpikeThresholdMillis(int thresholdMillis) {
		return new PingSpikeConfig(alertsEnabled, thresholdMillis, alertDurationMillis, soundEnabled, currentPingVisible);
	}

	public PingSpikeConfig withAlertDurationMillis(int durationMillis) {
		return new PingSpikeConfig(alertsEnabled, spikeThresholdMillis, durationMillis, soundEnabled, currentPingVisible);
	}

	public PingSpikeConfig withSoundEnabled(boolean enabled) {
		return new PingSpikeConfig(alertsEnabled, spikeThresholdMillis, alertDurationMillis, enabled, currentPingVisible);
	}

	public PingSpikeConfig withCurrentPingVisible(boolean visible) {
		return new PingSpikeConfig(alertsEnabled, spikeThresholdMillis, alertDurationMillis, soundEnabled, visible);
	}

	public PingSpikeDetector.Config detectorConfig() {
		int recoveryHysteresisMillis = Math.min(30, spikeThresholdMillis);
		return new PingSpikeDetector.Config(20, 5, spikeThresholdMillis, recoveryHysteresisMillis, 1_000, 5_000);
	}

	public static boolean isAllowedAlertDuration(int durationMillis) {
		for (int preset : ALERT_DURATION_PRESETS_MILLIS) {
			if (durationMillis == preset) {
				return true;
			}
		}
		return false;
	}
}
