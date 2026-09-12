package dev.mikelcodez.pingspikeindicator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Properties;

/**
 * Reads and writes the small versioned client configuration file.
 */
public final class PingSpikeConfigStore {
	private static final String KEY_VERSION = "version";
	private static final String KEY_ALERTS_ENABLED = "alertsEnabled";
	private static final String KEY_SPIKE_THRESHOLD = "spikeThresholdMillis";
	private static final String KEY_ALERT_DURATION = "alertDurationMillis";
	private static final String KEY_SOUND_ENABLED = "soundEnabled";
	private static final String KEY_CURRENT_PING_VISIBLE = "currentPingVisible";

	private final Path path;

	public PingSpikeConfigStore(Path path) {
		this.path = Objects.requireNonNull(path, "path");
	}

	public LoadResult load() {
		if (!Files.isRegularFile(path)) {
			return new LoadResult(PingSpikeConfig.defaults(), false);
		}

		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			properties.load(reader);
			int version = parseInteger(properties, KEY_VERSION);
			if (version != PingSpikeConfig.FORMAT_VERSION) {
				throw new IllegalArgumentException("unsupported config version");
			}

			PingSpikeConfig config = new PingSpikeConfig(
					parseBoolean(properties, KEY_ALERTS_ENABLED),
					parseInteger(properties, KEY_SPIKE_THRESHOLD),
					parseInteger(properties, KEY_ALERT_DURATION),
					parseBoolean(properties, KEY_SOUND_ENABLED),
					parseBoolean(properties, KEY_CURRENT_PING_VISIBLE)
			);
			return new LoadResult(config, false);
		} catch (IOException | IllegalArgumentException exception) {
			return new LoadResult(PingSpikeConfig.defaults(), true);
		}
	}

	public void save(PingSpikeConfig config) throws IOException {
		Objects.requireNonNull(config, "config");
		Path parent = path.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}

		Properties properties = new Properties();
		properties.setProperty(KEY_VERSION, Integer.toString(PingSpikeConfig.FORMAT_VERSION));
		properties.setProperty(KEY_ALERTS_ENABLED, Boolean.toString(config.alertsEnabled()));
		properties.setProperty(KEY_SPIKE_THRESHOLD, Integer.toString(config.spikeThresholdMillis()));
		properties.setProperty(KEY_ALERT_DURATION, Integer.toString(config.alertDurationMillis()));
		properties.setProperty(KEY_SOUND_ENABLED, Boolean.toString(config.soundEnabled()));
		properties.setProperty(KEY_CURRENT_PING_VISIBLE, Boolean.toString(config.currentPingVisible()));

		Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			try (Writer writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
				properties.store(writer, "Ping Spike Indicator client configuration");
			}

			try {
				Files.move(
						temporaryPath,
						path,
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING
				);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporaryPath);
		}
	}

	private static int parseInteger(Properties properties, String key) {
		String value = requiredValue(properties, key);
		return Integer.parseInt(value);
	}

	private static boolean parseBoolean(Properties properties, String key) {
		return switch (requiredValue(properties, key)) {
			case "true" -> true;
			case "false" -> false;
			default -> throw new IllegalArgumentException("invalid boolean for " + key);
		};
	}

	private static String requiredValue(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("missing config property " + key);
		}
		return value.trim();
	}

	public record LoadResult(PingSpikeConfig config, boolean recoveredFromInvalidFile) {
		public LoadResult {
			Objects.requireNonNull(config, "config");
		}
	}
}
