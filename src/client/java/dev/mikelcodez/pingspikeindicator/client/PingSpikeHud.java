package dev.mikelcodez.pingspikeindicator.client;

import java.util.Objects;

import dev.mikelcodez.pingspikeindicator.AlertSprite;
import dev.mikelcodez.pingspikeindicator.CurrentPingDisplayState;
import dev.mikelcodez.pingspikeindicator.CurrentPingStyle;
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
	private static final int TITLE_COLOR = 0xFFFFAA00;
	private static final int DETAIL_COLOR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOR = 0x800C0E14;
	private static final int BORDER_COLOR = 0xFFD95721;
	private static final int BORDER_INNER_COLOR = 0xFF801A0A;
	private static final int CURRENT_PING_BACKGROUND = 0x800C0E14;
	private static final int SPRITE_SIZE = 20;
	private static final int PADDING = 3;
	private static final int LINE_GAP = 1;
	private static final int TOP_MARGIN = 8;
	private static final int CURRENT_PING_MARGIN = 8;

	private final Minecraft client;
	private final ClientMonotonicClock clock;
	private final PingSpikeAlertState alertState;
	private final CurrentPingDisplayState currentPingState;
	private final ClientConfigManager configManager;

	private String measuredDetailText = "";
	private int detailWidth;
	private int titleWidth;
	private String measuredCurrentPingText = "";
	private int currentPingWidth;

	PingSpikeHud(
			Minecraft client,
			ClientMonotonicClock clock,
			PingSpikeAlertState alertState,
			CurrentPingDisplayState currentPingState,
			ClientConfigManager configManager
	) {
		this.client = Objects.requireNonNull(client, "client");
		this.clock = Objects.requireNonNull(clock, "clock");
		this.alertState = Objects.requireNonNull(alertState, "alertState");
		this.currentPingState = Objects.requireNonNull(currentPingState, "currentPingState");
		this.configManager = Objects.requireNonNull(configManager, "configManager");
	}

	void register() {
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, LAYER_ID, this::render);
	}

	private void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker tickCounter) {
		if (client == null || client.options.hideGui) {
			return;
		}
		if (client.screen != null) {
			return;
		}

		Font font = client.font;
		if (font == null) {
			return;
		}

		PingSpikeConfig config = configManager.config();
		boolean alertVisible = alertState.isVisibleAt(clock.millis());

		if (alertVisible && config.alertsEnabled()) {
			renderAlert(graphics, font, config);
		}

		if (config.currentPingVisible() && currentPingState.available()) {
			renderCurrentPing(graphics, font, config, alertVisible);
		}
	}

	private void renderAlert(GuiGraphics graphics, Font font, PingSpikeConfig config) {
		String detailText = alertState.detailText();
		if (!Objects.equals(detailText, measuredDetailText)) {
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

		int baseLeft;
		HudPosition position = config.hudPosition();
		switch (position) {
			case TOP_LEFT -> baseLeft = 10;
			case TOP_RIGHT -> baseLeft = graphics.guiWidth() - width - 10;
			default -> baseLeft = (graphics.guiWidth() - width) / 2;
		}
		int baseTop = TOP_MARGIN;

		int left = baseLeft + config.alertOffsetX();
		int top = baseTop + config.alertOffsetY();
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
		if (!Objects.equals(currentPingText, measuredCurrentPingText)) {
			measuredCurrentPingText = currentPingText;
			currentPingWidth = font.width(currentPingText);
		}

		int baseLeft = CURRENT_PING_MARGIN;
		int baseTop = CURRENT_PING_MARGIN;
		// Avoid overlapping alert if alert is also in TOP_LEFT and no custom offset was set
		if (alertVisible && config.hudPosition() == HudPosition.TOP_LEFT && config.currentPingOffsetX() == 0 && config.currentPingOffsetY() == 0) {
			baseTop = TOP_MARGIN + PADDING * 2 + Math.max(SPRITE_SIZE, font.lineHeight * 2 + LINE_GAP) + 6;
		}

		int left = baseLeft + config.currentPingOffsetX();
		int top = baseTop + config.currentPingOffsetY();
		CurrentPingStyle style = config.currentPingStyle();

		if (style == CurrentPingStyle.TEXT_ONLY) {
			graphics.drawString(font, currentPingText, left, top, DETAIL_COLOR, true);
			return;
		}

		int right = left + currentPingWidth + PADDING * 2;
		int bottom = top + font.lineHeight + PADDING * 2;

		// Background
		graphics.fill(left, top, right, bottom, CURRENT_PING_BACKGROUND);

		// Fancy accent border
		if (style == CurrentPingStyle.FANCY_ACCENT) {
			int accent = config.accentTheme().accent();
			graphics.renderOutline(left, top, right - left, bottom - top, accent);
		}

		graphics.drawString(font, currentPingText, left + PADDING, top + PADDING, DETAIL_COLOR, true);
	}
}
