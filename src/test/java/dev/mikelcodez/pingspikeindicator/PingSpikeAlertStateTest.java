package dev.mikelcodez.pingspikeindicator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.Signal.SPIKE_STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PingSpikeAlertStateTest {
	private PingSpikeDetector detector;
	private PingSpikeAlertState alertState;

	@BeforeEach
	void setUp() {
		detector = new PingSpikeDetector(new PingSpikeDetector.Config(8, 4, 50, 20, 200, 5_000));
		alertState = new PingSpikeAlertState(3_000);
		warmBaseline();
	}

	@Test
	void spikeStartFormatsCurrentPingAndIncrease() {
		PingSpikeDetector.Update spike = detector.update(400, 160);

		alertState.onDetectorUpdate(400, 160, spike);

		assertEquals(SPIKE_STARTED, spike.signal());
		assertEquals("160 ms  (+60 ms)", alertState.detailText());
		assertTrue(alertState.isVisibleAt(400));
	}

	@Test
	void formatterRoundsIncreaseToTheNearestMillisecond() {
		assertEquals("181 ms  (+80 ms)", PingSpikeAlertFormatter.format(181, 100.6));
	}

	@Test
	void alertExpiresAfterFixedDuration() {
		alertState.onDetectorUpdate(400, 160, detector.update(400, 160));

		assertTrue(alertState.isVisibleAt(3_399));
		assertFalse(alertState.isVisibleAt(3_400));
	}

	@Test
	void repeatedHighSamplesDoNotRestartAlert() {
		alertState.onDetectorUpdate(400, 160, detector.update(400, 160));
		alertState.onDetectorUpdate(2_000, 170, detector.update(2_000, 170));

		assertFalse(alertState.isVisibleAt(3_400));
	}

	@Test
	void recoveredLaterSpikeStartsANewAlert() {
		alertState.onDetectorUpdate(400, 160, detector.update(400, 160));
		detector.update(500, 125);
		detector.update(700, 120);
		PingSpikeDetector.Update secondSpike = detector.update(800, 170);

		alertState.onDetectorUpdate(800, 170, secondSpike);

		assertEquals("170 ms  (+66 ms)", alertState.detailText());
		assertTrue(alertState.isVisibleAt(3_799));
		assertFalse(alertState.isVisibleAt(3_800));
	}

	@Test
	void nonSpikeUpdatesDoNotShowAnAlert() {
		PingSpikeDetector.Update normal = detector.update(400, 110);

		alertState.onDetectorUpdate(400, 110, normal);

		assertFalse(alertState.isVisibleAt(400));
		assertEquals("", alertState.detailText());
	}

	@Test
	void clearImmediatelyHidesAlert() {
		alertState.onDetectorUpdate(400, 160, detector.update(400, 160));

		alertState.clear();

		assertFalse(alertState.isVisibleAt(400));
		assertEquals("", alertState.detailText());
	}

	@Test
	void durationChangeDismissesOldAlertAndAppliesToNextSpike() {
		alertState.onDetectorUpdate(400, 160, detector.update(400, 160));

		alertState.setDisplayDurationMillis(5_000);
		assertFalse(alertState.isVisibleAt(401));
		detector.update(500, 125);
		detector.update(700, 120);
		alertState.onDetectorUpdate(800, 170, detector.update(800, 170));

		assertTrue(alertState.isVisibleAt(5_799));
		assertFalse(alertState.isVisibleAt(5_800));
	}

	private void warmBaseline() {
		detector.update(0, 100);
		detector.update(100, 100);
		detector.update(200, 100);
		detector.update(300, 100);
	}
}
