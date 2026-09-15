package dev.mikelcodez.pingspikeindicator.client;

import dev.mikelcodez.pingspikeindicator.AlertSprite;
import dev.mikelcodez.pingspikeindicator.CurrentPingDisplayState;
import dev.mikelcodez.pingspikeindicator.HudPosition;
import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class PingSpikeHud {
	private static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(
			"pingspikeindicator",
			"spike_alert"
	);
	private static final String TITLE = "PING SPIKE";
	private static final int BORDER_COLOR = 0xFFD95721;
	private static final int BORDER_INNER_COLOR = 0xFF801A0A;
	private static final int BACKGROUND_COLOR = 0xEE0C0E14;
	private static final int TITLE_COLOR = 0xFFFFAA00;
	private static final int DETAIL_COLOR = 0xFFFFFFFF;
	private static final int PADDING = 4;
	private static final int LINE_GAP = 2;
	private static final int TOP_MARGIN = 10;
	private static final int CURRENT_PING_MARGIN = 5;
	private static final int CURRENT_PING_BACKGROUND = 0x90000000;
	private static final int SPRITE_SIZE = 22;

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
		PingSpikeConfig config = configManager.config();

		boolean alertVisible = alertState.isVisibleAt(clock.millis());
		if (alertVisible) {
			renderAlert(graphics, font, config);
		}

		if (config.currentPingVisible() && currentPingState.available()) {
			renderCurrentPing(graphics, font, config, alertVisible);
		}
	}

	private void renderAlert(GuiGraphics graphics, Font font, PingSpikeConfig config) {
		String detailText = alertState.detailText();
		if (detailText != measuredDetailText) {
			measuredDetailText = detailText;
			detailWidth = font.width(detailText);
		}
		if (titleWidth == 0) {
			titleWidth = font.width(TITLE);
		}

		int textWidth = Math.max(titleWidth, detailWidth);
		int contentWidth = SPRITE_SIZE + 6 + textWidth;
		int width = contentWidth + PADDING * 2;
		int textBlockHeight = font.lineHeight * 2 + LINE_GAP;
		int height = Math.max(SPRITE_SIZE, textBlockHeight) + PADDING * 2;

		int left;
		HudPosition position = config.hudPosition();
		switch (position) {
			case TOP_LEFT -> left = 10;
			case TOP_RIGHT -> left = graphics.guiWidth() - width - 10;
			default -> left = (graphics.guiWidth() - width) / 2;
		}
		int top = TOP_MARGIN;
		int right = left + width;
		int bottom = top + height;

		// Blocky alert panel backdrop and borders
		graphics.fill(left, top, right, bottom, BACKGROUND_COLOR);
		graphics.renderOutline(left, top, width, height, BORDER_COLOR);
		graphics.renderOutline(left + 1, top + 1, width - 2, height - 2, BORDER_INNER_COLOR);

		// Render tactical alert sprite
		AlertSprite sprite = config.alertSprite();
		Identifier spriteId = Identifier.fromNamespaceAndPath("pingspikeindicator", "alert/" + sprite.spritePath());
		int spriteX = left + PADDING + 1;
		int spriteY = top + (height - SPRITE_SIZE) / 2;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, spriteX, spriteY, SPRITE_SIZE, SPRITE_SIZE);

		// Render text block
		int textX = spriteX + SPRITE_SIZE + 5;
		int textY = top + (height - textBlockHeight) / 2;
		graphics.drawString(font, TITLE, textX, textY, TITLE_COLOR, true);
		graphics.drawString(
				font,
				detailText,
				textX,
				textY + font.lineHeight + LINE_GAP,
				DETAIL_COLOR,
				true
		);
	}

	private void renderCurrentPing(GuiGraphics graphics, Font font, PingSpikeConfig config, boolean alertVisible) {
		String currentPingText = currentPingState.text();
		if (currentPingText != measuredCurrentPingText) {
			measuredCurrentPingText = currentPingText;
			currentPingWidth = font.width(currentPingText);
		}

		int left = CURRENT_PING_MARGIN;
		int top = CURRENT_PING_MARGIN;
		// Avoid overlapping alert if alert is also in TOP_LEFT
		if (alertVisible && config.hudPosition() == HudPosition.TOP_LEFT) {
			top = TOP_MARGIN + PADDING * 2 + Math.max(SPRITE_SIZE, font.lineHeight * 2 + LINE_GAP) + 6;
		}

		int right = left + currentPingWidth + PADDING * 2;
		int bottom = top + font.lineHeight + PADDING * 2;
		graphics.fill(left, top, right, bottom, CURRENT_PING_BACKGROUND);
		graphics.drawString(font, currentPingText, left + PADDING, top + PADDING, DETAIL_COLOR, true);
	}
}
