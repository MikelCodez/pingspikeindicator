package dev.mikelcodez.pingspikeindicator.client;

import java.util.OptionalInt;

import dev.mikelcodez.pingspikeindicator.CurrentPingDisplayState;
import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import dev.mikelcodez.pingspikeindicator.PingSpikeDetector;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientPingCapture {
	private static final Logger LOGGER = LoggerFactory.getLogger("pingspikeindicator");
	private static final long LIVE_PING_PROBE_INTERVAL_MILLIS = 500;

	private static ClientPingCapture activeCapture;

	private final ClientMonotonicClock clock;
	private final PingSpikeAlertState alertState;
	private final CurrentPingDisplayState currentPingState;
	private PingSpikeDetector detector;
	private PingSpikeConfig config;
	private boolean sessionActive;
	private boolean sessionMarkerLogged;
	private long lastProbeSentTimeMillis = 0;
	private volatile int latestRealTimeLatencyMs = -1;

	public ClientPingCapture(
			ClientMonotonicClock clock,
			PingSpikeAlertState alertState,
			CurrentPingDisplayState currentPingState,
			PingSpikeConfig initialConfig
	) {
		this.clock = clock;
		this.alertState = alertState;
		this.currentPingState = currentPingState;
		this.config = initialConfig;
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
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> endSession());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> endSession());
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> resetActiveSession());
		LOGGER.info("Ping capture initialized with live {} ms probe interval", LIVE_PING_PROBE_INTERVAL_MILLIS);
	}

	private void onEndClientTick(Minecraft client) {
		if (!hasRemoteMultiplayerConnection(client)) {
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

		// Actively send live ping request packet every 500ms
		long now = Util.getMillis();
		if (now - lastProbeSentTimeMillis >= LIVE_PING_PROBE_INTERVAL_MILLIS) {
			ClientPacketListener connection = client.getConnection();
			if (connection != null) {
				connection.send(new ServerboundPingRequestPacket(now));
				lastProbeSentTimeMillis = now;
			}
		}
	}

	/**
	 * Called immediately when the server responds with ClientboundPongResponsePacket.
	 * This executes directly in real-time, bypassing any slow tab latency polling!
	 */
	public void onRealTimePongReceived(int realTimeLatencyMs) {
		if (realTimeLatencyMs < 0) {
			return;
		}

		this.latestRealTimeLatencyMs = realTimeLatencyMs;

		// 1. Immediately update Current Ping HUD element
		if (config.currentPingVisible()) {
			currentPingState.accept(realTimeLatencyMs);
		}

		// 2. Feed directly into detector on every pong response
		if (sessionActive) {
			long observedAtMillis = clock.millis();
			PingSpikeDetector.Update update = detector.update(observedAtMillis, OptionalInt.of(realTimeLatencyMs));

			if (!sessionMarkerLogged) {
				LOGGER.debug("Multiplayer ping capture active; first valid live sample accepted: {} ms", realTimeLatencyMs);
				sessionMarkerLogged = true;
			}

			if (update.signal() == PingSpikeDetector.Signal.SPIKE_STARTED && config.alertsEnabled()) {
				alertState.onDetectorUpdate(observedAtMillis, realTimeLatencyMs, update);
				Minecraft client = Minecraft.getInstance();
				if (config.soundEnabled() && client != null) {
					client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.25F));
				}
				LOGGER.info("LIVE Ping spike alert triggered: ping={} ms, baseline={}", realTimeLatencyMs, update.baselineMillis());
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

	private static boolean hasRemoteMultiplayerConnection(Minecraft client) {
		return client.getConnection() != null && !client.isSingleplayer();
	}

	private void resetActiveSession() {
		if (sessionActive) {
			detector.reset();
		}
		alertState.clear();
		currentPingState.clear();
		sessionMarkerLogged = false;
	}

	private void endSession() {
		sessionActive = false;
		detector.reset();
		alertState.clear();
		currentPingState.clear();
		sessionMarkerLogged = false;
	}
}
