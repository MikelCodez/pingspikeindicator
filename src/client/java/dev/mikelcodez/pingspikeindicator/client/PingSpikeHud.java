package dev.mikelcodez.pingspikeindicator.client;

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

	private final Minecraft client;
	private final ClientMonotonicClock clock;
	private final PingSpikeAlertState alertState;
	private String measuredDetailText;
	private int detailWidth;
	private int titleWidth;

	PingSpikeHud(Minecraft client, ClientMonotonicClock clock, PingSpikeAlertState alertState) {
		this.client = client;
		this.clock = clock;
		this.alertState = alertState;
	}

	void register() {
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, LAYER_ID, this::render);
	}

	private void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker tickCounter) {
		if (!alertState.isVisibleAt(clock.millis())) {
			return;
		}

		Font font = client.font;
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
}
