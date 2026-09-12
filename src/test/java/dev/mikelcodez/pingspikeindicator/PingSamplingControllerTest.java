package dev.mikelcodez.pingspikeindicator;

import java.util.OptionalInt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.mikelcodez.pingspikeindicator.PingSamplingController.PollStatus.INACTIVE;
import static dev.mikelcodez.pingspikeindicator.PingSamplingController.PollStatus.NOT_DUE;
import static dev.mikelcodez.pingspikeindicator.PingSamplingController.PollStatus.SAMPLED;
import static dev.mikelcodez.pingspikeindicator.PingSamplingController.PollStatus.UNAVAILABLE;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.Signal.SPIKE_STARTED;
import static dev.mikelcodez.pingspikeindicator.PingSpikeDetector.State.NORMAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PingSamplingControllerTest {
	private PingSpikeDetector detector;
	private PingSamplingController controller;

	@BeforeEach
	void setUp() {
		detector = new PingSpikeDetector(new PingSpikeDetector.Config(8, 4, 50, 20, 200, 5_000));
		controller = new PingSamplingController(detector, 1_000);
	}

	@Test
	void inactiveControllerDoesNotSample() {
		PingSamplingController.PollResult result = controller.poll(0, OptionalInt.of(100));

		assertEquals(INACTIVE, result.status());
		assertTrue(result.detectorUpdate().isEmpty());
		assertEquals(0, detector.historySize());
	}

	@Test
	void cadenceAllowsAtMostOneSamplePerInterval() {
		controller.beginSession();

		assertTrue(controller.isSampleDue(0));
		assertEquals(SAMPLED, controller.poll(0, OptionalInt.of(100)).status());
		assertFalse(controller.isSampleDue(999));
		assertEquals(NOT_DUE, controller.poll(999, OptionalInt.of(200)).status());
		assertTrue(controller.isSampleDue(1_000));
		assertEquals(SAMPLED, controller.poll(1_000, OptionalInt.of(101)).status());
		assertEquals(2, detector.historySize());
	}

	@Test
	void unavailableLatencyConsumesCadenceWithoutEnteringBaseline() {
		controller.beginSession();

		PingSamplingController.PollResult unavailable = controller.poll(0, OptionalInt.empty());

		assertEquals(UNAVAILABLE, unavailable.status());
		assertEquals(0, detector.historySize());
		assertFalse(controller.isSampleDue(999));
		assertTrue(controller.isSampleDue(1_000));
	}

	@Test
	void sessionTransitionsResetDetectorAndAllowImmediateSampling() {
		controller.beginSession();
		controller.poll(0, OptionalInt.of(100));
		controller.poll(1_000, OptionalInt.of(100));
		assertEquals(2, detector.historySize());

		controller.endSession();
		assertFalse(controller.sessionActive());
		assertEquals(NORMAL, detector.state());
		assertEquals(0, detector.historySize());
		assertEquals(INACTIVE, controller.poll(2_000, OptionalInt.of(100)).status());

		controller.beginSession();
		assertTrue(controller.isSampleDue(2_000));
		assertEquals(SAMPLED, controller.poll(2_000, OptionalInt.of(110)).status());
	}

	@Test
	void beginningAnActiveSessionResetsForAWorldTransition() {
		controller.beginSession();
		controller.poll(0, OptionalInt.of(100));
		assertEquals(1, detector.historySize());

		controller.beginSession();

		assertTrue(controller.sessionActive());
		assertEquals(0, detector.historySize());
		assertTrue(controller.isSampleDue(100));
	}

	@Test
	void acceptedSamplesFlowThroughTheExistingDetector() {
		controller.beginSession();
		controller.poll(0, OptionalInt.of(100));
		controller.poll(1_000, OptionalInt.of(100));
		controller.poll(2_000, OptionalInt.of(100));
		controller.poll(3_000, OptionalInt.of(100));

		PingSamplingController.PollResult spike = controller.poll(4_000, OptionalInt.of(160));

		assertEquals(SAMPLED, spike.status());
		assertEquals(SPIKE_STARTED, spike.detectorUpdate().orElseThrow().signal());
	}
}
