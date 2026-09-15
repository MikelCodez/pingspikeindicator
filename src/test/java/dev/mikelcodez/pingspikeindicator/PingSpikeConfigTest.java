package dev.mikelcodez.pingspikeindicator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PingSpikeConfigTest {
	@Test
	void defaultsAreSafeAndCurrentPingIsOptIn() {
		PingSpikeConfig defaults = PingSpikeConfig.defaults();

		assertTrue(defaults.alertsEnabled());
		assertEquals(80, defaults.spikeThresholdMillis());
		assertEquals(3_000, defaults.alertDurationMillis());
		assertTrue(defaults.soundEnabled());
		assertFalse(defaults.currentPingVisible());
		assertEquals(AlertSprite.SIGNAL_BARS, defaults.alertSprite());
		assertEquals(HudPosition.TOP_CENTER, defaults.hudPosition());
		assertEquals(TabListPingMode.BOTH, defaults.tabListPingMode());
	}

	@Test
	void thresholdBoundsAreInclusive() {
		PingSpikeConfig defaults = PingSpikeConfig.defaults();

		assertEquals(25, defaults.withSpikeThresholdMillis(25).spikeThresholdMillis());
		assertEquals(500, defaults.withSpikeThresholdMillis(500).spikeThresholdMillis());
		assertThrows(IllegalArgumentException.class, () -> defaults.withSpikeThresholdMillis(24));
		assertThrows(IllegalArgumentException.class, () -> defaults.withSpikeThresholdMillis(501));
	}

	@Test
	void onlySmallDurationPresetsAreAccepted() {
		PingSpikeConfig defaults = PingSpikeConfig.defaults();

		assertEquals(2_000, defaults.withAlertDurationMillis(2_000).alertDurationMillis());
		assertEquals(3_000, defaults.withAlertDurationMillis(3_000).alertDurationMillis());
		assertEquals(5_000, defaults.withAlertDurationMillis(5_000).alertDurationMillis());
		assertThrows(IllegalArgumentException.class, () -> defaults.withAlertDurationMillis(1_999));
		assertThrows(IllegalArgumentException.class, () -> defaults.withAlertDurationMillis(10_000));
	}

	@Test
	void spriteAndPositionMutatorsWorkCorrectly() {
		PingSpikeConfig defaults = PingSpikeConfig.defaults();

		assertEquals(AlertSprite.SATELLITE, defaults.withAlertSprite(AlertSprite.SATELLITE).alertSprite());
		assertEquals(HudPosition.TOP_RIGHT, defaults.withHudPosition(HudPosition.TOP_RIGHT).hudPosition());
		assertEquals(TabListPingMode.NUMBERS_ONLY, defaults.withTabListPingMode(TabListPingMode.NUMBERS_ONLY).tabListPingMode());
	}

	@Test
	void detectorPolicyUsesConfiguredThreshold() {
		PingSpikeDetector.Config detectorConfig = PingSpikeConfig.defaults()
				.withSpikeThresholdMillis(125)
				.detectorConfig();

		assertEquals(125, detectorConfig.spikeThresholdMillis());
		assertTrue(detectorConfig.recoveryHysteresisMillis() <= detectorConfig.spikeThresholdMillis());
	}

	@Test
	void releaseDefaultsPinTheReviewedDetectorProfile() {
		PingSpikeDetector.Config reviewedProfile = new PingSpikeDetector.Config(20, 5, 80, 30, 1_000, 5_000);

		assertEquals(reviewedProfile, PingSpikeDetector.Config.defaults());
		assertEquals(reviewedProfile, PingSpikeConfig.defaults().detectorConfig());
	}
}
