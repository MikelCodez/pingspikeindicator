package dev.mikelcodez.pingspikeindicator.client;

import java.util.Objects;
import java.util.OptionalInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.mikelcodez.pingspikeindicator.CurrentPingDisplayState;
import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import dev.mikelcodez.pingspikeindicator.PingSpikeDetector;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;

public final class ClientPingCapture {
	private static final Logger LOGGER = LoggerFactory.getLogger("pingspikeindicator");
	private static final long LIVE_PING_PROBE_INTERVAL_MILLIS = 500;

	private static ClientPingCapture activeCapture;

	private final ClientMonotonicClock clock;
	private final PingSpikeAlertState alertState;
	private final CurrentPingDisplayState currentPingState;
	private PingSpikeConfig config;
	private PingSpikeDetector detector;
	private boolean sessionActive = false;
	private boolean sessionMarkerLogged = false;
	private long lastProbeSentTimeMillis = 0;
	private volatile int latestRealTimeLatencyMs = -1;

	public ClientPingCapture(
			ClientMonotonicClock clock,
			PingSpikeAlertState alertState,
			CurrentPingDisplayState currentPingState,
			PingSpikeConfig initialConfig
	) {
		this.clock = Objects.requireNonNull(clock, "clock");
		this.alertState = Objects.requireNonNull(alertState, "alertState");
		this.currentPingState = Objects.requireNonNull(currentPingState, "currentPingState");
		this.config = Objects.requireNonNull(initialConfig, "initialConfig");
		this.detector = new PingSpikeDetector(initialConfig.detectorConfig());
		activeCapture = this;
	}

	public static ClientPingCapture getActive() {
		return activeCapture;
	}

	public int latestRealTimeLatencyMs() {
		return latestRealTimeLatencyMs;
	}

	void register() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> endSession());
	}

	private void onEndTick(Minecraft client) {
		if (client == null) {
			return;
		}

		if (client.isSingleplayer()) {
			endSession();
			return;
		}

		if (!sessionActive) {
			sessionActive = true;
			detector.reset();
			sessionMarkerLogged = false;
		}

		if (client.level == null || client.player == null) {
			return;
		}

		long now = Util.getMillis();
		if (now - lastProbeSentTimeMillis >= LIVE_PING_PROBE_INTERVAL_MILLIS) {
			ClientPacketListener connection = client.getConnection();
			if (connection != null) {
				connection.send(new ServerboundPingRequestPacket(now));
				lastProbeSentTimeMillis = now;
			}
		}
	}

	public void onRealTimePongReceived(int realTimeLatencyMs) {
		if (realTimeLatencyMs < 0) {
			return;
		}

		this.latestRealTimeLatencyMs = realTimeLatencyMs;

		if (config.currentPingVisible()) {
			currentPingState.accept(realTimeLatencyMs);
		}

		if (sessionActive) {
			long observedAtMillis = clock.millis();
			PingSpikeDetector.Update update = detector.update(observedAtMillis, OptionalInt.of(realTimeLatencyMs));

			if (!sessionMarkerLogged) {
				LOGGER.debug("Multiplayer ping capture active; first sample: {} ms", realTimeLatencyMs);
				sessionMarkerLogged = true;
			}

			if (update.signal() == PingSpikeDetector.Signal.SPIKE_STARTED && config.alertsEnabled()) {
				alertState.onDetectorUpdate(observedAtMillis, realTimeLatencyMs, update);
				Minecraft client = Minecraft.getInstance();
				if (config.soundEnabled() && client != null) {
					client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.25F));
				}
				LOGGER.info("Ping spike detected: ping={} ms, baseline={}", realTimeLatencyMs, update.baselineMillis());
			}
		}
	}

	void applyConfig(PingSpikeConfig replacement) {
		if (replacement.spikeThresholdMillis() != config.spikeThresholdMillis()) {
			detector = new PingSpikeDetector(replacement.detectorConfig());
		}
		if (replacement.alertDurationMillis() != config.alertDurationMillis()) {
			alertState.setDisplayDurationMillis(replacement.alertDurationMillis());
		}
		config = replacement;
		if (!config.alertsEnabled()) {
			alertState.clear();
		}
		if (!config.currentPingVisible()) {
			currentPingState.clear();
		}
	}

	void dismissAlert() {
		alertState.clear();
	}

	private void endSession() {
		if (!sessionActive) {
			return;
		}
		sessionActive = false;
		sessionMarkerLogged = false;
		lastProbeSentTimeMillis = 0;
		latestRealTimeLatencyMs = -1;
		detector.reset();
		alertState.clear();
		currentPingState.clear();
	}
}
