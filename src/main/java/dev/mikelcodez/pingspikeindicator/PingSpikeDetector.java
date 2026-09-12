package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Detects abrupt ping increases relative to a bounded rolling baseline.
 * Callers supply observation timestamps so recovery behavior is deterministic.
 */
public final class PingSpikeDetector {
	private static final long NO_TIMESTAMP = -1;

	private final Config config;
	private final BoundedPingHistory history;
	private State state = State.NORMAL;
	private long lastObservationTimeMillis = NO_TIMESTAMP;
	private long recoveryCandidateSinceMillis = NO_TIMESTAMP;

	public PingSpikeDetector(Config config) {
		this.config = Objects.requireNonNull(config, "config");
		this.history = new BoundedPingHistory(config.historyCapacity());
	}

	public Update update(long observedAtMillis, OptionalInt observedPingMillis) {
		Objects.requireNonNull(observedPingMillis, "observedPingMillis");

		if (observedAtMillis < 0 || observedAtMillis < lastObservationTimeMillis) {
			return result(SampleStatus.OUT_OF_ORDER, Signal.NONE);
		}

		lastObservationTimeMillis = observedAtMillis;

		if (observedPingMillis.isEmpty()) {
			cancelRecoveryCandidate();
			return result(SampleStatus.MISSING, Signal.NONE);
		}

		int pingMillis = observedPingMillis.getAsInt();
		if (pingMillis < 0) {
			cancelRecoveryCandidate();
			return result(SampleStatus.NEGATIVE, Signal.NONE);
		}
		if (pingMillis > config.maximumAcceptedPingMillis()) {
			cancelRecoveryCandidate();
			return result(SampleStatus.OUTLIER, Signal.NONE);
		}

		if (!baselineReady()) {
			history.add(pingMillis);
			return result(SampleStatus.ACCEPTED, Signal.NONE);
		}

		double baselineMillis = history.averageMillis();
		if (state == State.NORMAL) {
			if (pingMillis - baselineMillis >= config.spikeThresholdMillis()) {
				state = State.SPIKING;
				cancelRecoveryCandidate();
				return result(SampleStatus.ACCEPTED, Signal.SPIKE_STARTED);
			}

			history.add(pingMillis);
			return result(SampleStatus.ACCEPTED, Signal.NONE);
		}

		double recoveryBoundaryMillis = baselineMillis
				+ config.spikeThresholdMillis()
				- config.recoveryHysteresisMillis();
		if (pingMillis <= recoveryBoundaryMillis) {
			if (recoveryCandidateSinceMillis == NO_TIMESTAMP) {
				recoveryCandidateSinceMillis = observedAtMillis;
			}

			if (observedAtMillis - recoveryCandidateSinceMillis >= config.recoveryHoldMillis()) {
				state = State.NORMAL;
				cancelRecoveryCandidate();
				history.add(pingMillis);
			}
		} else {
			cancelRecoveryCandidate();
		}

		return result(SampleStatus.ACCEPTED, Signal.NONE);
	}

	public Update update(long observedAtMillis, int observedPingMillis) {
		return update(observedAtMillis, OptionalInt.of(observedPingMillis));
	}

	public Update updateMissing(long observedAtMillis) {
		return update(observedAtMillis, OptionalInt.empty());
	}

	public State state() {
		return state;
	}

	public OptionalDouble baselineMillis() {
		return history.size() == 0
				? OptionalDouble.empty()
				: OptionalDouble.of(history.averageMillis());
	}

	public boolean baselineReady() {
		return history.size() >= config.minimumBaselineSamples();
	}

	public int historySize() {
		return history.size();
	}

	public int historyCapacity() {
		return history.capacity();
	}

	public void reset() {
		history.clear();
		state = State.NORMAL;
		lastObservationTimeMillis = NO_TIMESTAMP;
		cancelRecoveryCandidate();
	}

	private Update result(SampleStatus sampleStatus, Signal signal) {
		return new Update(
				state,
				signal,
				sampleStatus,
				baselineMillis(),
				baselineReady(),
				history.size()
		);
	}

	private void cancelRecoveryCandidate() {
		recoveryCandidateSinceMillis = NO_TIMESTAMP;
	}

	public enum State {
		NORMAL,
		SPIKING
	}

	public enum Signal {
		NONE,
		SPIKE_STARTED
	}

	public enum SampleStatus {
		ACCEPTED,
		MISSING,
		NEGATIVE,
		OUTLIER,
		OUT_OF_ORDER
	}

	public record Config(
			int historyCapacity,
			int minimumBaselineSamples,
			int spikeThresholdMillis,
			int recoveryHysteresisMillis,
			long recoveryHoldMillis,
			int maximumAcceptedPingMillis
	) {
		public Config {
			if (historyCapacity < 1) {
				throw new IllegalArgumentException("historyCapacity must be positive");
			}
			if (minimumBaselineSamples < 1 || minimumBaselineSamples > historyCapacity) {
				throw new IllegalArgumentException("minimumBaselineSamples must be between 1 and historyCapacity");
			}
			if (spikeThresholdMillis < 1) {
				throw new IllegalArgumentException("spikeThresholdMillis must be positive");
			}
			if (recoveryHysteresisMillis < 1 || recoveryHysteresisMillis > spikeThresholdMillis) {
				throw new IllegalArgumentException("recoveryHysteresisMillis must be between 1 and spikeThresholdMillis");
			}
			if (recoveryHoldMillis < 0) {
				throw new IllegalArgumentException("recoveryHoldMillis must not be negative");
			}
			if (maximumAcceptedPingMillis < spikeThresholdMillis) {
				throw new IllegalArgumentException("maximumAcceptedPingMillis must be at least spikeThresholdMillis");
			}
		}

		public static Config defaults() {
			return new Config(20, 5, 80, 30, 1_000, 5_000);
		}
	}

	public record Update(
			State state,
			Signal signal,
			SampleStatus sampleStatus,
			OptionalDouble baselineMillis,
			boolean baselineReady,
			int historySize
	) {
		public Update {
			Objects.requireNonNull(state, "state");
			Objects.requireNonNull(signal, "signal");
			Objects.requireNonNull(sampleStatus, "sampleStatus");
			Objects.requireNonNull(baselineMillis, "baselineMillis");
		}
	}
}
