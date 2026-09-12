package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Applies a bounded sampling cadence and session lifecycle to a ping detector.
 * The client adapter remains responsible for obtaining client-visible latency.
 */
public final class PingSamplingController {
	private static final long NO_SAMPLE_DUE = -1;

	private PingSpikeDetector detector;
	private final long sampleIntervalMillis;
	private boolean sessionActive;
	private long nextSampleAtMillis = NO_SAMPLE_DUE;

	public PingSamplingController(PingSpikeDetector detector, long sampleIntervalMillis) {
		this.detector = Objects.requireNonNull(detector, "detector");
		if (sampleIntervalMillis < 1) {
			throw new IllegalArgumentException("sampleIntervalMillis must be positive");
		}

		this.sampleIntervalMillis = sampleIntervalMillis;
	}

	public void beginSession() {
		detector.reset();
		sessionActive = true;
		nextSampleAtMillis = NO_SAMPLE_DUE;
	}

	public void endSession() {
		detector.reset();
		sessionActive = false;
		nextSampleAtMillis = NO_SAMPLE_DUE;
	}

	public boolean sessionActive() {
		return sessionActive;
	}

	/**
	 * Applies detector policy changes without carrying an old baseline or spike state forward.
	 */
	public void replaceDetector(PingSpikeDetector replacement) {
		detector = Objects.requireNonNull(replacement, "replacement");
		detector.reset();
		nextSampleAtMillis = NO_SAMPLE_DUE;
	}

	public boolean isSampleDue(long observedAtMillis) {
		validateTimestamp(observedAtMillis);
		return sessionActive && (nextSampleAtMillis == NO_SAMPLE_DUE || observedAtMillis >= nextSampleAtMillis);
	}

	public PollResult poll(long observedAtMillis, OptionalInt observedPingMillis) {
		Objects.requireNonNull(observedPingMillis, "observedPingMillis");
		validateTimestamp(observedAtMillis);

		if (!sessionActive) {
			return new PollResult(PollStatus.INACTIVE, Optional.empty());
		}
		if (!isSampleDue(observedAtMillis)) {
			return new PollResult(PollStatus.NOT_DUE, Optional.empty());
		}

		nextSampleAtMillis = nextSampleTime(observedAtMillis);
		PingSpikeDetector.Update detectorUpdate = observedPingMillis.isPresent()
				? detector.update(observedAtMillis, observedPingMillis)
				: detector.updateMissing(observedAtMillis);
		PollStatus status = detectorUpdate.sampleStatus() == PingSpikeDetector.SampleStatus.ACCEPTED
				? PollStatus.SAMPLED
				: PollStatus.UNAVAILABLE;
		return new PollResult(status, Optional.of(detectorUpdate));
	}

	private long nextSampleTime(long observedAtMillis) {
		return observedAtMillis > Long.MAX_VALUE - sampleIntervalMillis
				? Long.MAX_VALUE
				: observedAtMillis + sampleIntervalMillis;
	}

	private static void validateTimestamp(long observedAtMillis) {
		if (observedAtMillis < 0) {
			throw new IllegalArgumentException("observedAtMillis must not be negative");
		}
	}

	public enum PollStatus {
		INACTIVE,
		NOT_DUE,
		UNAVAILABLE,
		SAMPLED
	}

	public record PollResult(PollStatus status, Optional<PingSpikeDetector.Update> detectorUpdate) {
		public PollResult {
			Objects.requireNonNull(status, "status");
			Objects.requireNonNull(detectorUpdate, "detectorUpdate");
		}
	}
}
