package dev.mikelcodez.pingspikeindicator.client;

import java.util.OptionalInt;

import dev.mikelcodez.pingspikeindicator.PingSamplingController;
import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import dev.mikelcodez.pingspikeindicator.PingSpikeDetector;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ClientPingCapture {
	private static final Logger LOGGER = LoggerFactory.getLogger("pingspikeindicator");
	private static final long SAMPLE_INTERVAL_MILLIS = 1_000;

	private final ClientMonotonicClock clock;
	private final PingSpikeAlertState alertState;
	private final PingSamplingController controller;
	private boolean sessionMarkerLogged;

	ClientPingCapture(ClientMonotonicClock clock, PingSpikeAlertState alertState) {
		this.clock = clock;
		this.alertState = alertState;
		this.controller = new PingSamplingController(
				new PingSpikeDetector(PingSpikeDetector.Config.defaults()),
				SAMPLE_INTERVAL_MILLIS
		);
	}

	void register() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> endSession());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> endSession());
		ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> resetActiveSession());
		LOGGER.info("Ping capture initialized with a {} ms sampling interval", SAMPLE_INTERVAL_MILLIS);
	}

	private void onEndClientTick(Minecraft client) {
		if (!hasRemoteMultiplayerConnection(client)) {
			endSession();
			return;
		}

		if (!controller.sessionActive()) {
			controller.beginSession();
			sessionMarkerLogged = false;
		}

		if (client.screen != null || client.level == null || client.player == null) {
			return;
		}

		long observedAtMillis = clock.millis();
		if (!controller.isSampleDue(observedAtMillis)) {
			return;
		}

		OptionalInt latency = currentLatency(client);
		PingSamplingController.PollResult result = controller.poll(observedAtMillis, latency);
		if (result.status() != PingSamplingController.PollStatus.SAMPLED) {
			return;
		}

		PingSpikeDetector.Update update = result.detectorUpdate().orElseThrow();
		if (!sessionMarkerLogged) {
			LOGGER.info("Multiplayer ping capture active; first valid sample accepted");
			sessionMarkerLogged = true;
		}
		if (update.signal() == PingSpikeDetector.Signal.SPIKE_STARTED) {
			alertState.onDetectorUpdate(observedAtMillis, latency.orElseThrow(), update);
			LOGGER.debug("Ping spike start observed by Phase 1 detector");
		}
	}

	private static boolean hasRemoteMultiplayerConnection(Minecraft client) {
		return client.getConnection() != null && !client.isSingleplayer();
	}

	private static OptionalInt currentLatency(Minecraft client) {
		ClientPacketListener connection = client.getConnection();
		if (connection == null || client.player == null) {
			return OptionalInt.empty();
		}

		PlayerInfo localPlayerInfo = connection.getPlayerInfo(client.player.getUUID());
		if (localPlayerInfo == null) {
			return OptionalInt.empty();
		}

		int latencyMillis = localPlayerInfo.getLatency();
		return latencyMillis < 0 ? OptionalInt.empty() : OptionalInt.of(latencyMillis);
	}

	private void resetActiveSession() {
		if (controller.sessionActive()) {
			controller.beginSession();
		}
		alertState.clear();
		sessionMarkerLogged = false;
	}

	private void endSession() {
		if (controller.sessionActive()) {
			controller.endSession();
		}
		alertState.clear();
		sessionMarkerLogged = false;
	}
}
