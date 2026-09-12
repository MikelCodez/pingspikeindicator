package dev.mikelcodez.pingspikeindicator.client;

import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import net.fabricmc.api.ClientModInitializer;

public final class PingSpikeIndicatorClient implements ClientModInitializer {
	private static final long ALERT_DURATION_MILLIS = 3_000;

	@Override
	public void onInitializeClient() {
		ClientMonotonicClock clock = new ClientMonotonicClock();
		PingSpikeAlertState alertState = new PingSpikeAlertState(ALERT_DURATION_MILLIS);
		new ClientPingCapture(clock, alertState).register();
		new PingSpikeHud(net.minecraft.client.Minecraft.getInstance(), clock, alertState).register();
	}
}
