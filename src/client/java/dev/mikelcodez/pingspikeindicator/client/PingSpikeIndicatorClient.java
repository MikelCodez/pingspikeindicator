package dev.mikelcodez.pingspikeindicator.client;

import dev.mikelcodez.pingspikeindicator.CurrentPingDisplayState;
import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import net.fabricmc.api.ClientModInitializer;

public final class PingSpikeIndicatorClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientMonotonicClock clock = new ClientMonotonicClock();
		PingSpikeConfig defaults = PingSpikeConfig.defaults();
		PingSpikeAlertState alertState = new PingSpikeAlertState(defaults.alertDurationMillis());
		CurrentPingDisplayState currentPingState = new CurrentPingDisplayState();
		ClientPingCapture capture = new ClientPingCapture(clock, alertState, currentPingState, defaults);
		ClientConfigManager configManager = new ClientConfigManager(capture::applyConfig);

		capture.register();
		new PingSpikeHud(
				net.minecraft.client.Minecraft.getInstance(),
				clock,
				alertState,
				currentPingState,
				configManager
		).register();
		new PingSpikeConfigControls(configManager, capture).register();
	}
}
