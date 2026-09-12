package dev.mikelcodez.pingspikeindicator.client;

import dev.mikelcodez.pingspikeindicator.CurrentPingDisplayState;
import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

final class PingSpikeHud {
	private static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(
			"pingspikeindicator",
			"spike_alert"
	);
	private static final String TITLE = "PING SPIKE";
	private static final int BORDER_COLOR = 0xFFD95721;
	private static final int BACKGROUND_COLOR = 0xD0100C08;
	private static final int TITLE_COLOR = 0xFFFFAA00;
	private static final int DETAIL_COLOR = 0xFFFFFFFF;
	private static final int PADDING = 4;
	private static final int LINE_GAP = 2;
	private static final int TOP_MARGIN = 10;
	private static final int CURRENT_PING_MARGIN = 5;
	private static final int CURRENT_PING_BACKGROUND = 0x90000000;

	private final Minecraft client;
	private final ClientMonotonicClock clock;
	private final PingSpikeAlertState alertState;
	private final CurrentPingDisplayState currentPingState;
	private final ClientConfigManager configManager;
	private String measuredDetailText;
	private String measuredCurrentPingText;
	private int detailWidth;
	private int currentPingWidth;
	private int titleWidth;

	PingSpikeHud(
			Minecraft client,
			ClientMonotonicClock clock,
			PingSpikeAlertState alertState,
			CurrentPingDisplayState currentPingState,
			ClientConfigManager configManager
	) {
		this.client = client;
		this.clock = clock;
		this.alertState = alertState;
		this.currentPingState = currentPingState;
		this.configManager = configManager;
	}

	void register() {
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, LAYER_ID, this::render);
	}

	private void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker tickCounter) {
		Font font = client.font;
		if (configManager.config().currentPingVisible() && currentPingState.available()) {
			renderCurrentPing(graphics, font);
		}
		if (alertState.isVisibleAt(clock.millis())) {
			renderAlert(graphics, font);
		}
	}

	private void renderAlert(GuiGraphics graphics, Font font) {
		String detailText = alertState.detailText();
		if (detailText != measuredDetailText) {
			measuredDetailText = detailText;
			detailWidth = font.width(detailText);
		}
		if (titleWidth == 0) {
			titleWidth = font.width(TITLE);
		}

		int contentWidth = Math.max(titleWidth, detailWidth);
		int width = contentWidth + PADDING * 2;
		int height = PADDING * 2 + font.lineHeight * 2 + LINE_GAP;
		int left = (graphics.guiWidth() - width) / 2;
		int top = TOP_MARGIN;
		int right = left + width;
		int bottom = top + height;

		graphics.fill(left, top, right, bottom, BORDER_COLOR);
		graphics.fill(left + 1, top + 1, right - 1, bottom - 1, BACKGROUND_COLOR);
		graphics.drawString(font, TITLE, left + (width - titleWidth) / 2, top + PADDING, TITLE_COLOR, true);
		graphics.drawString(
				font,
				detailText,
				left + (width - detailWidth) / 2,
				top + PADDING + font.lineHeight + LINE_GAP,
				DETAIL_COLOR,
				true
		);
	}

	private void renderCurrentPing(GuiGraphics graphics, Font font) {
		String currentPingText = currentPingState.text();
		if (currentPingText != measuredCurrentPingText) {
			measuredCurrentPingText = currentPingText;
			currentPingWidth = font.width(currentPingText);
		}

		int left = CURRENT_PING_MARGIN;
		int top = CURRENT_PING_MARGIN;
		int right = left + currentPingWidth + PADDING * 2;
		int bottom = top + font.lineHeight + PADDING * 2;
		graphics.fill(left, top, right, bottom, CURRENT_PING_BACKGROUND);
		graphics.drawString(font, currentPingText, left + PADDING, top + PADDING, DETAIL_COLOR, true);
	}
}
