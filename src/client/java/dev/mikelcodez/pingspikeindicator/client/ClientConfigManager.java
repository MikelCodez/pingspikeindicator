package dev.mikelcodez.pingspikeindicator.client;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfigStore;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ClientConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("pingspikeindicator");
	private static final String FILE_NAME = "pingspikeindicator.properties";

	private final PingSpikeConfigStore store;
	private final Consumer<PingSpikeConfig> runtimeApplier;
	private PingSpikeConfig config;

	ClientConfigManager(Consumer<PingSpikeConfig> runtimeApplier) {
		this.runtimeApplier = Objects.requireNonNull(runtimeApplier, "runtimeApplier");
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		store = new PingSpikeConfigStore(configPath);
		PingSpikeConfigStore.LoadResult result = store.load();
		config = result.config();
		if (result.recoveredFromInvalidFile()) {
			LOGGER.warn("Invalid ping spike configuration; using safe defaults");
		}
		runtimeApplier.accept(config);
	}

	PingSpikeConfig config() {
		return config;
	}

	void saveAndApply(PingSpikeConfig replacement) {
		config = Objects.requireNonNull(replacement, "replacement");
		runtimeApplier.accept(config);
		try {
			store.save(config);
		} catch (IOException exception) {
			LOGGER.warn("Could not save ping spike configuration; changes apply for this session", exception);
		}
	}
}
