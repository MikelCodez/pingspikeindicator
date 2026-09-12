package dev.mikelcodez.pingspikeindicator.client;

import net.fabricmc.api.ClientModInitializer;

public final class PingSpikeIndicatorClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientMonotonicClock clock = new ClientMonotonicClock();
		new ClientPingCapture(clock).register();
	}
}
