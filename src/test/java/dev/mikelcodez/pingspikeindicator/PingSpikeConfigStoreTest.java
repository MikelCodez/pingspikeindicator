package dev.mikelcodez.pingspikeindicator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PingSpikeConfigStoreTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void missingFileUsesDefaultsWithoutReportingCorruption() {
		PingSpikeConfigStore.LoadResult result = store().load();

		assertEquals(PingSpikeConfig.defaults(), result.config());
		assertFalse(result.recoveredFromInvalidFile());
	}

	@Test
	void validConfigurationRoundTrips() throws IOException {
		PingSpikeConfig expected = new PingSpikeConfig(
				false,
				145,
				5_000,
				false,
				true,
				AlertSprite.WIFI,
				HudPosition.TOP_RIGHT,
				TabListPingMode.NUMBERS_ONLY,
				CurrentPingStyle.FANCY_ACCENT,
				AccentTheme.EMERALD_GREEN,
				15,
				25,
				30,
				40
		);
		PingSpikeConfigStore store = store();

		store.save(expected);
		PingSpikeConfigStore.LoadResult result = store.load();

		assertEquals(expected, result.config());
		assertFalse(result.recoveredFromInvalidFile());
		assertFalse(Files.exists(configPath().resolveSibling("pingspikeindicator.properties.tmp")));
	}

	@Test
	void olderConfigurationMissingNewKeysFallsBackSafelyToDefaults() throws IOException {
		Files.writeString(configPath(), validText());

		PingSpikeConfigStore.LoadResult result = store().load();

		assertEquals(AlertSprite.SIGNAL_BARS, result.config().alertSprite());
		assertEquals(HudPosition.TOP_CENTER, result.config().hudPosition());
		assertEquals(TabListPingMode.BOTH, result.config().tabListPingMode());
		assertEquals(CurrentPingStyle.TRANSLUCENT, result.config().currentPingStyle());
		assertEquals(AccentTheme.CYAN_BLUE, result.config().accentTheme());
		assertEquals(0, result.config().alertOffsetX());
		assertEquals(0, result.config().alertOffsetY());
		assertEquals(0, result.config().currentPingOffsetX());
		assertEquals(0, result.config().currentPingOffsetY());
		assertFalse(result.recoveredFromInvalidFile());
	}

	@Test
	void corruptConfigurationFallsBackSafely() throws IOException {
		Files.writeString(configPath(), "version=1\nalertsEnabled=maybe\n");

		PingSpikeConfigStore.LoadResult result = store().load();

		assertEquals(PingSpikeConfig.defaults(), result.config());
		assertTrue(result.recoveredFromInvalidFile());
	}

	@Test
	void outOfRangeConfigurationFallsBackSafely() throws IOException {
		Files.writeString(configPath(), validText().replace("spikeThresholdMillis=80", "spikeThresholdMillis=999"));

		PingSpikeConfigStore.LoadResult result = store().load();

		assertEquals(PingSpikeConfig.defaults(), result.config());
		assertTrue(result.recoveredFromInvalidFile());
	}

	@Test
	void unsupportedVersionFallsBackSafely() throws IOException {
		Files.writeString(configPath(), validText().replace("version=1", "version=2"));

		PingSpikeConfigStore.LoadResult result = store().load();

		assertEquals(PingSpikeConfig.defaults(), result.config());
		assertTrue(result.recoveredFromInvalidFile());
	}

	@Test
	void malformedUnicodeEscapeFallsBackSafely() throws IOException {
		Files.writeString(configPath(), "version=1\nalertsEnabled=\\uZZZZ\n");

		PingSpikeConfigStore.LoadResult result = store().load();

		assertEquals(PingSpikeConfig.defaults(), result.config());
		assertTrue(result.recoveredFromInvalidFile());
	}

	private PingSpikeConfigStore store() {
		return new PingSpikeConfigStore(configPath());
	}

	private Path configPath() {
		return temporaryDirectory.resolve("pingspikeindicator.properties");
	}

	private static String validText() {
		return """
				version=1
				alertsEnabled=true
				spikeThresholdMillis=80
				alertDurationMillis=3000
				soundEnabled=true
				currentPingVisible=false
				""";
	}
}
