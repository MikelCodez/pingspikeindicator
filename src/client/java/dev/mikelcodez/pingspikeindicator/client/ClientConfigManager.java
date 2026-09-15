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

public final class ClientConfigManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("pingspikeindicator");
	private static final String FILE_NAME = "pingspikeindicator.properties";
	private static ClientConfigManager instance;

	private final PingSpikeConfigStore store;
	private final Consumer<PingSpikeConfig> runtimeApplier;
	private PingSpikeConfig config;

	public ClientConfigManager(Consumer<PingSpikeConfig> runtimeApplier) {
		this.runtimeApplier = Objects.requireNonNull(runtimeApplier, "runtimeApplier");
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		store = new PingSpikeConfigStore(configPath);
		PingSpikeConfigStore.LoadResult result = store.load();
		config = result.config();
		if (result.recoveredFromInvalidFile()) {
			LOGGER.warn("Invalid ping spike configuration; using safe defaults");
		}
		runtimeApplier.accept(config);
		instance = this;
	}

	public static PingSpikeConfig currentConfig() {
		return instance != null ? instance.config() : PingSpikeConfig.defaults();
	}

	public PingSpikeConfig config() {
		return config;
	}

	public void saveAndApply(PingSpikeConfig replacement) {
		config = Objects.requireNonNull(replacement, "replacement");
		runtimeApplier.accept(config);
		try {
			store.save(config);
		} catch (IOException exception) {
			LOGGER.error("Failed to save ping spike configuration", exception);
		}
	}
}
