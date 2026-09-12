package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;

/**
 * Holds one preformatted, fixed-duration alert without depending on game types.
 */
public final class PingSpikeAlertState {
	private static final long NOT_VISIBLE = -1;

	private long displayDurationMillis;
	private long visibleUntilMillis = NOT_VISIBLE;
	private String detailText = "";

	public PingSpikeAlertState(long displayDurationMillis) {
		if (displayDurationMillis < 1) {
			throw new IllegalArgumentException("displayDurationMillis must be positive");
		}

		this.displayDurationMillis = displayDurationMillis;
	}

	public void onDetectorUpdate(
			long observedAtMillis,
			int currentPingMillis,
			PingSpikeDetector.Update detectorUpdate
	) {
		Objects.requireNonNull(detectorUpdate, "detectorUpdate");
		validateTimestamp(observedAtMillis);
		if (currentPingMillis < 0) {
			throw new IllegalArgumentException("currentPingMillis must not be negative");
		}

		if (detectorUpdate.signal() != PingSpikeDetector.Signal.SPIKE_STARTED
				|| detectorUpdate.baselineMillis().isEmpty()) {
			return;
		}

		detailText = PingSpikeAlertFormatter.format(currentPingMillis, detectorUpdate.baselineMillis().getAsDouble());
		visibleUntilMillis = expirationTime(observedAtMillis);
	}

	public boolean isVisibleAt(long observedAtMillis) {
		validateTimestamp(observedAtMillis);
		return visibleUntilMillis != NOT_VISIBLE && observedAtMillis < visibleUntilMillis;
	}

	public String detailText() {
		return detailText;
	}

	public void clear() {
		visibleUntilMillis = NOT_VISIBLE;
		detailText = "";
	}

	/**
	 * Changes the duration for future alerts and dismisses any alert using the old policy.
	 */
	public void setDisplayDurationMillis(long displayDurationMillis) {
		if (displayDurationMillis < 1) {
			throw new IllegalArgumentException("displayDurationMillis must be positive");
		}
		this.displayDurationMillis = displayDurationMillis;
		clear();
	}

	private long expirationTime(long observedAtMillis) {
		return observedAtMillis > Long.MAX_VALUE - displayDurationMillis
				? Long.MAX_VALUE
				: observedAtMillis + displayDurationMillis;
	}

	private static void validateTimestamp(long observedAtMillis) {
		if (observedAtMillis < 0) {
			throw new IllegalArgumentException("observedAtMillis must not be negative");
		}
	}
}
