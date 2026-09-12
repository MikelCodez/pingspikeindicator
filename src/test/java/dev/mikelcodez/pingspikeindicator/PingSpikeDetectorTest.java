package dev.mikelcodez.pingspikeindicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.SampleStatus.MISSING;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.SampleStatus.NEGATIVE;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.SampleStatus.OUTLIER;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.SampleStatus.OUT_OF_ORDER;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.Signal.NONE;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.Signal.SPIKE_STARTED;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.State.NORMAL;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.State.SPIKING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PingSpikeDetectorTest {
	private static final PingSpikeDetector.Config CONFIG = new PingSpikeDetector.Config(
			8,
			4,
			50,
			20,
			200,
			5_000
	);

	private PingSpikeDetector detector;

	@BeforeEach
	void setUp() {
		detector = new PingSpikeDetector(CONFIG);
	}

	@Test
	void stablePingBuildsBaselineWithoutSpike() {
		int[] samples = {100, 102, 98, 101, 99, 103, 97, 100};

		for (int index = 0; index < samples.length; index++) {
			PingSpikeDetector.Update update = detector.update(index * 100L, samples[index]);
			assertEquals(NORMAL, update.state());
			assertEquals(NONE, update.signal());
		}

		assertTrue(detector.baselineReady());
		assertEquals(100.0, detector.baselineMillis().orElseThrow(), 0.001);
	}

	@Test
	void suddenSpikeEmitsStartSignal() {
		warmBaseline();

		PingSpikeDetector.Update update = detector.update(400, 150);

		assertEquals(SPIKING, update.state());
		assertEquals(SPIKE_STARTED, update.signal());
		assertEquals(4, update.historySize());
	}

	@Test
	void gradualDriftMovesBaselineWithoutSpike() {
		warmBaseline();

		for (int pingMillis = 105; pingMillis <= 180; pingMillis += 5) {
			PingSpikeDetector.Update update = detector.update(300L + pingMillis * 10L, pingMillis);
			assertEquals(NORMAL, update.state());
			assertEquals(NONE, update.signal());
		}

		assertTrue(detector.baselineMillis().orElseThrow() > 155.0);
	}

	@Test
	void repeatedHighSamplesDoNotDuplicateStartSignal() {
		warmBaseline();
		assertEquals(SPIKE_STARTED, detector.update(400, 160).signal());

		for (int index = 0; index < 10; index++) {
			PingSpikeDetector.Update update = detector.update(500L + index * 100L, 170 + index);
			assertEquals(SPIKING, update.state());
			assertEquals(NONE, update.signal());
		}

		assertEquals(4, detector.historySize());
	}

	@Test
	void recoveryAllowsALaterSecondSpike() {
		warmBaseline();
		assertEquals(SPIKE_STARTED, detector.update(400, 160).signal());

		assertEquals(SPIKING, detector.update(500, 125).state());
		PingSpikeDetector.Update recovered = detector.update(700, 120);
		assertEquals(NORMAL, recovered.state());
		assertEquals(NONE, recovered.signal());

		PingSpikeDetector.Update secondSpike = detector.update(800, 170);
		assertEquals(SPIKING, secondSpike.state());
		assertEquals(SPIKE_STARTED, secondSpike.signal());
	}

	@Test
	void noisyBoundaryValuesDoNotSpamSignalsOrRecoverEarly() {
		warmBaseline();

		assertEquals(SPIKE_STARTED, detector.update(400, 150).signal());
		assertEquals(NONE, detector.update(500, 149).signal());
		assertEquals(NONE, detector.update(600, 131).signal());
		assertEquals(NONE, detector.update(700, 129).signal());
		assertEquals(NONE, detector.update(800, 132).signal());
		assertEquals(NONE, detector.update(900, 128).signal());
		assertEquals(SPIKING, detector.update(1_000, 129).state());
		assertEquals(NORMAL, detector.update(1_100, 127).state());
	}

	@Test
	void historyRemainsBoundedAndUsesMostRecentAcceptedSamples() {
		detector = new PingSpikeDetector(new PingSpikeDetector.Config(5, 3, 500, 100, 0, 5_000));

		for (int index = 0; index < 50; index++) {
			detector.update(index, 100 + index);
		}

		assertEquals(5, detector.historySize());
		assertEquals(5, detector.historyCapacity());
		assertEquals(147.0, detector.baselineMillis().orElseThrow(), 0.001);
	}

	@Test
	void missingNegativeOutlierAndOutOfOrderSamplesAreIgnored() {
		warmBaseline();
		double baseline = detector.baselineMillis().orElseThrow();

		assertEquals(MISSING, detector.updateMissing(400).sampleStatus());
		assertEquals(NEGATIVE, detector.update(500, -1).sampleStatus());
		assertEquals(OUTLIER, detector.update(600, 5_001).sampleStatus());
		assertEquals(OUT_OF_ORDER, detector.update(599, 100).sampleStatus());

		assertEquals(NORMAL, detector.state());
		assertEquals(4, detector.historySize());
		assertEquals(baseline, detector.baselineMillis().orElseThrow(), 0.001);
	}

	@Test
	void resetClearsSessionState() {
		warmBaseline();
		detector.update(400, 160);

		detector.reset();

		assertEquals(NORMAL, detector.state());
		assertEquals(0, detector.historySize());
		assertFalse(detector.baselineReady());
		assertTrue(detector.baselineMillis().isEmpty());
	}

	private void warmBaseline() {
		detector.update(0, 100);
		detector.update(100, 100);
		detector.update(200, 100);
		detector.update(300, 100);
	}
}
