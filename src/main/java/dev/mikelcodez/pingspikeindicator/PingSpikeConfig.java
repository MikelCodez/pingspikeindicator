package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;

/**
 * Validated, client-persisted user options expressed without game-runtime types.
 */
public record PingSpikeConfig(
		boolean alertsEnabled,
		int spikeThresholdMillis,
		int alertDurationMillis,
		boolean soundEnabled,
		boolean currentPingVisible,
		AlertSprite alertSprite,
		HudPosition hudPosition,
		TabListPingMode tabListPingMode,
		CurrentPingStyle currentPingStyle,
		AccentTheme accentTheme,
		int alertOffsetX,
		int alertOffsetY,
		int currentPingOffsetX,
		int currentPingOffsetY
) {
	public static final int FORMAT_VERSION = 1;
	public static final int MINIMUM_THRESHOLD_MILLIS = 25;
	public static final int MAXIMUM_THRESHOLD_MILLIS = 500;
	public static final int THRESHOLD_STEP_MILLIS = 5;
	public static final int DEFAULT_THRESHOLD_MILLIS = 80;
	public static final int DEFAULT_ALERT_DURATION_MILLIS = 3_000;
	private static final int[] ALERT_DURATION_PRESETS_MILLIS = {2_000, 3_000, 5_000};

	public PingSpikeConfig {
		Objects.requireNonNull(alertSprite, "alertSprite");
		Objects.requireNonNull(hudPosition, "hudPosition");
		Objects.requireNonNull(tabListPingMode, "tabListPingMode");
		Objects.requireNonNull(currentPingStyle, "currentPingStyle");
		Objects.requireNonNull(accentTheme, "accentTheme");
		if (spikeThresholdMillis < MINIMUM_THRESHOLD_MILLIS
				|| spikeThresholdMillis > MAXIMUM_THRESHOLD_MILLIS) {
			throw new IllegalArgumentException("spikeThresholdMillis is outside the supported range");
		}
		if (!isAllowedAlertDuration(alertDurationMillis)) {
			throw new IllegalArgumentException("alertDurationMillis is not a supported preset");
		}
	}

	public PingSpikeConfig(
			boolean alertsEnabled,
			int spikeThresholdMillis,
			int alertDurationMillis,
			boolean soundEnabled,
			boolean currentPingVisible,
			AlertSprite alertSprite,
			HudPosition hudPosition,
			TabListPingMode tabListPingMode
	) {
		this(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				CurrentPingStyle.TRANSLUCENT,
				AccentTheme.CYAN_BLUE,
				0,
				0,
				0,
				0
		);
	}

	public PingSpikeConfig(
			boolean alertsEnabled,
			int spikeThresholdMillis,
			int alertDurationMillis,
			boolean soundEnabled,
			boolean currentPingVisible
	) {
		this(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				AlertSprite.SIGNAL_BARS,
				HudPosition.TOP_CENTER,
				TabListPingMode.BOTH,
				CurrentPingStyle.TRANSLUCENT,
				AccentTheme.CYAN_BLUE,
				0,
				0,
				0,
				0
		);
	}

	public static PingSpikeConfig defaults() {
		return new PingSpikeConfig(
				true,
				DEFAULT_THRESHOLD_MILLIS,
				DEFAULT_ALERT_DURATION_MILLIS,
				true,
				false,
				AlertSprite.SIGNAL_BARS,
				HudPosition.TOP_CENTER,
				TabListPingMode.BOTH,
				CurrentPingStyle.TRANSLUCENT,
				AccentTheme.CYAN_BLUE,
				0,
				0,
				0,
				0
		);
	}

	public PingSpikeConfig withAlertsEnabled(boolean alertsEnabled) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withSpikeThresholdMillis(int spikeThresholdMillis) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withAlertDurationMillis(int alertDurationMillis) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withSoundEnabled(boolean soundEnabled) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withCurrentPingVisible(boolean currentPingVisible) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withAlertSprite(AlertSprite alertSprite) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withHudPosition(HudPosition hudPosition) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withTabListPingMode(TabListPingMode tabListPingMode) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withCurrentPingStyle(CurrentPingStyle currentPingStyle) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withAccentTheme(AccentTheme accentTheme) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withAlertOffset(int alertOffsetX, int alertOffsetY) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeConfig withCurrentPingOffset(int currentPingOffsetX, int currentPingOffsetY) {
		return new PingSpikeConfig(
				alertsEnabled,
				spikeThresholdMillis,
				alertDurationMillis,
				soundEnabled,
				currentPingVisible,
				alertSprite,
				hudPosition,
				tabListPingMode,
				currentPingStyle,
				accentTheme,
				alertOffsetX,
				alertOffsetY,
				currentPingOffsetX,
				currentPingOffsetY
		);
	}

	public PingSpikeDetector.Config detectorConfig() {
		int recoveryHysteresisMillis = Math.min(30, spikeThresholdMillis);
		return new PingSpikeDetector.Config(20, 5, spikeThresholdMillis, recoveryHysteresisMillis, 1_000, 5_000);
	}

	public static boolean isAllowedAlertDuration(int alertDurationMillis) {
		for (int preset : ALERT_DURATION_PRESETS_MILLIS) {
			if (preset == alertDurationMillis) {
				return true;
			}
		}
		return false;
	}
}
