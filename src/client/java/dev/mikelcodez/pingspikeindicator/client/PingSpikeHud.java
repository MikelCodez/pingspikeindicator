package dev.mikelcodez.pingspikeindicator.client;

import java.util.Objects;

import dev.mikelcodez.pingspikeindicator.AlertSprite;
import dev.mikelcodez.pingspikeindicator.CurrentPingDisplayState;
import dev.mikelcodez.pingspikeindicator.CurrentPingStyle;
import dev.mikelcodez.pingspikeindicator.HudPosition;
import dev.mikelcodez.pingspikeindicator.PingSpikeAlertState;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

final class PingSpikeHud {
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

	private String measuredDetailText;
	private int measuredDetailWidth;
	private String measuredCurrentPingText;
	private int measuredCurrentPingWidth;

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
		HudRenderCallback.EVENT.register((graphics, tickDelta) -> render(graphics));
	}

	private void render(GuiGraphics graphics) {
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

		long now = clock.millis();
		PingSpikeConfig config = configManager.config();
		boolean alertVisible = alertState.isVisibleAt(now);
		boolean currentPingVisible = config.currentPingVisible() && currentPingState.available();

		if (!alertVisible && !currentPingVisible) {
			return;
		}

		if (alertVisible) {
			renderAlert(graphics, font, config);
		}

		if (currentPingVisible) {
			renderCurrentPing(graphics, font, config, alertVisible);
		}
	}

	private void renderAlert(GuiGraphics graphics, Font font, PingSpikeConfig config) {
		String detailText = alertState.detailText();
		if (!Objects.equals(detailText, measuredDetailText)) {
			measuredDetailText = detailText;
			measuredDetailWidth = font.width(detailText);
		}

		int titleWidth = font.width(TITLE);
		int textBlockWidth = Math.max(titleWidth, measuredDetailWidth);
		int textBlockHeight = font.lineHeight * 2 + LINE_GAP;

		int contentWidth = SPRITE_SIZE + 6 + textBlockWidth;
		int contentHeight = Math.max(SPRITE_SIZE, textBlockHeight);

		int width = contentWidth + PADDING * 2;
		int height = contentHeight + PADDING * 2;

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
		ResourceLocation fullTexture = new ResourceLocation("pingspikeindicator", "textures/gui/sprites/alert/" + sprite.spritePath() + ".png");
		int spriteX = left + PADDING + 1;
		int spriteY = top + (height - SPRITE_SIZE) / 2;
		graphics.blit(fullTexture, spriteX, spriteY, 0, 0, SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);

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
			measuredCurrentPingWidth = font.width(currentPingText);
		}

		CurrentPingStyle style = config.currentPingStyle();

		int baseLeft = CURRENT_PING_MARGIN;
		int baseTop = CURRENT_PING_MARGIN;

		int x = baseLeft + config.currentPingOffsetX();
		int y = baseTop + config.currentPingOffsetY();

		if (style == CurrentPingStyle.TEXT_ONLY) {
			graphics.drawString(font, currentPingText, x, y, DETAIL_COLOR, true);
			return;
		}

		int width = measuredCurrentPingWidth + PADDING * 2;
		int height = font.lineHeight + PADDING * 2;

		graphics.fill(x, y, x + width, y + height, CURRENT_PING_BACKGROUND);

		int borderColor = (style == CurrentPingStyle.FANCY_ACCENT) ? config.accentTheme().accent() : 0x80353A47;
		graphics.renderOutline(x, y, width, height, borderColor);

		graphics.drawString(font, currentPingText, x + PADDING, y + PADDING, DETAIL_COLOR, true);
	}
}
