package dev.mikelcodez.pingspikeindicator.client.mixin;

import dev.mikelcodez.pingspikeindicator.TabListPingFormatter;
import dev.mikelcodez.pingspikeindicator.TabListPingMode;
import dev.mikelcodez.pingspikeindicator.client.ClientConfigManager;
import dev.mikelcodez.pingspikeindicator.client.ClientPingCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {

	@Inject(
			method = "renderPingIcon",
			at = @At("HEAD"),
			cancellable = true
	)
	private void onRenderPingIcon(
			GuiGraphics graphics,
			int width,
			int x,
			int y,
			PlayerInfo playerInfo,
			CallbackInfo ci
	) {
		TabListPingMode mode = ClientConfigManager.currentConfig().tabListPingMode();
		if (mode == TabListPingMode.VANILLA_BARS) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			return;
		}
		Font font = client.font;
		if (font == null) {
			return;
		}

		int ping = playerInfo.getLatency();
		// If this is the local player and we have a live real-time latency probe, use it!
		if (client.player != null && client.player.getUUID().equals(playerInfo.getProfile().getId())) {
			ClientPingCapture capture = ClientPingCapture.getActive();
			if (capture != null && capture.latestRealTimeLatencyMs() >= 0) {
				ping = capture.latestRealTimeLatencyMs();
			}
		}

		String text = TabListPingFormatter.formatPing(ping);
		int color = TabListPingFormatter.pingColor(ping);
		int textWidth = font.width(text);
		int textY = y + (8 - font.lineHeight) / 2 + 1;

		if (mode == TabListPingMode.BOTH) {
			int textX = x + width - 13 - textWidth;
			graphics.drawString(font, text, textX, textY, color, true);
		} else if (mode == TabListPingMode.NUMBERS_ONLY) {
			int textX = x + width - 2 - textWidth;
			graphics.drawString(font, text, textX, textY, color, true);
			ci.cancel();
		}
	}
}
