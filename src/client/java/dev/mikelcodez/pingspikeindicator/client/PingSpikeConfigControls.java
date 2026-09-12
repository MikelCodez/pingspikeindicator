package dev.mikelcodez.pingspikeindicator.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

final class PingSpikeConfigControls {
	private final ClientConfigManager configManager;
	private final ClientPingCapture pingCapture;
	private final KeyMapping openConfigKey = new KeyMapping(
			"key.pingspikeindicator.open_config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_O,
			KeyMapping.Category.MISC
	);

	PingSpikeConfigControls(ClientConfigManager configManager, ClientPingCapture pingCapture) {
		this.configManager = configManager;
		this.pingCapture = pingCapture;
	}

	void register() {
		KeyBindingHelper.registerKeyBinding(openConfigKey);
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
	}

	private void onEndClientTick(Minecraft client) {
		while (openConfigKey.consumeClick()) {
			if (client.screen == null) {
				pingCapture.dismissAlert();
				client.setScreen(new PingSpikeConfigScreen(null, configManager.config(), configManager::saveAndApply));
			}
		}
	}
}
