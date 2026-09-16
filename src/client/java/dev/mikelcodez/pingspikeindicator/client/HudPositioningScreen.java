package dev.mikelcodez.pingspikeindicator.client;

import java.util.Objects;
import java.util.function.Consumer;

import dev.mikelcodez.pingspikeindicator.AlertSprite;
import dev.mikelcodez.pingspikeindicator.CurrentPingStyle;
import dev.mikelcodez.pingspikeindicator.HudPosition;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Interactive full-screen editor that allows players to click and drag the Ping Spike Alert
 * and Current Ping HUD elements freely across their screen.
 */
final class HudPositioningScreen extends Screen {
	private static final String ALERT_TITLE = "PING SPIKE";
	private static final String ALERT_DETAIL = "+80 ms (135 ms)";
	private static final String PING_SAMPLE_TEXT = "Ping: 55 ms";
	private static final int SPRITE_SIZE = 20;
	private static final int PADDING = 3;
	private static final int LINE_GAP = 1;
	private static final int TOP_MARGIN = 8;
	private static final int CURRENT_PING_MARGIN = 8;

	private final Screen parent;
	private final Consumer<PingSpikeConfig> saveAction;
	private PingSpikeConfig workingConfig;

	// Dragging state
	private enum DragTarget {
		NONE,
		ALERT,
		CURRENT_PING
	}

	private DragTarget activeDrag = DragTarget.NONE;
	private int dragGrabOffsetX = 0;
	private int dragGrabOffsetY = 0;

	// Cached bounding boxes for rendering & hit testing
	private int alertBoxX;
	private int alertBoxY;
	private int alertBoxW;
	private int alertBoxH;

	private int pingBoxX;
	private int pingBoxY;
	private int pingBoxW;
	private int pingBoxH;

	HudPositioningScreen(Screen parent, PingSpikeConfig initialConfig, Consumer<PingSpikeConfig> saveAction) {
		super(Component.translatable("screen.pingspikeindicator.hud_positioning"));
		this.parent = parent;
		this.workingConfig = Objects.requireNonNull(initialConfig, "initialConfig");
		this.saveAction = Objects.requireNonNull(saveAction, "saveAction");
	}

	@Override
	protected void init() {
		int buttonWidth = 110;
		int buttonHeight = 22;
		int gap = 8;
		int bottomY = height - buttonHeight - 12;
		int totalWidth = buttonWidth * 2 + gap;
		int startX = (width - totalWidth) / 2;

		// Reset Positions Button
		addRenderableWidget(new EditorButton(
				startX,
				bottomY,
				buttonWidth,
				buttonHeight,
				Component.translatable("option.pingspikeindicator.reset_positions"),
				btn -> {
					workingConfig = workingConfig
							.withAlertOffset(0, 0)
							.withCurrentPingOffset(0, 0);
				},
				0x801A1E29,
				0xFFE0E5EE
		));

		// Done / Save Button
		addRenderableWidget(new EditorButton(
				startX + buttonWidth + gap,
				bottomY,
				buttonWidth,
				buttonHeight,
				Component.translatable("gui.done"),
				btn -> onClose(),
				0x80122B1E,
				workingConfig.accentTheme().accent()
		));
	}

	@Override
	public void onClose() {
		saveAction.accept(workingConfig);
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			// Test Alert Box hit
			if (mouseX >= alertBoxX && mouseX <= alertBoxX + alertBoxW && mouseY >= alertBoxY && mouseY <= alertBoxY + alertBoxH) {
				activeDrag = DragTarget.ALERT;
				dragGrabOffsetX = (int) mouseX - alertBoxX;
				dragGrabOffsetY = (int) mouseY - alertBoxY;
				return true;
			}

			// Test Current Ping Box hit
			if (mouseX >= pingBoxX && mouseX <= pingBoxX + pingBoxW && mouseY >= pingBoxY && mouseY <= pingBoxY + pingBoxH) {
				activeDrag = DragTarget.CURRENT_PING;
				dragGrabOffsetX = (int) mouseX - pingBoxX;
				dragGrabOffsetY = (int) mouseY - pingBoxY;
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			activeDrag = DragTarget.NONE;
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (activeDrag == DragTarget.ALERT) {
			int baseLeft = getBaseAlertLeft(alertBoxW);
			int baseTop = TOP_MARGIN;
			int targetX = (int) mouseX - dragGrabOffsetX;
			int targetY = (int) mouseY - dragGrabOffsetY;

			int clampedX = Mth.clamp(targetX, 2, width - alertBoxW - 2);
			int clampedY = Mth.clamp(targetY, 2, height - alertBoxH - 40);

			workingConfig = workingConfig.withAlertOffset(clampedX - baseLeft, clampedY - baseTop);
			return true;
		} else if (activeDrag == DragTarget.CURRENT_PING) {
			int baseLeft = CURRENT_PING_MARGIN;
			int baseTop = CURRENT_PING_MARGIN;
			int targetX = (int) mouseX - dragGrabOffsetX;
			int targetY = (int) mouseY - dragGrabOffsetY;

			int clampedX = Mth.clamp(targetX, 2, width - pingBoxW - 2);
			int clampedY = Mth.clamp(targetY, 2, height - pingBoxH - 40);

			workingConfig = workingConfig.withCurrentPingOffset(clampedX - baseLeft, clampedY - baseTop);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	private int getBaseAlertLeft(int alertWidth) {
		HudPosition position = workingConfig.hudPosition();
		return switch (position) {
			case TOP_LEFT -> 10;
			case TOP_RIGHT -> width - alertWidth - 10;
			default -> (width - alertWidth) / 2;
		};
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Subtle dark overlay to see HUD clearly over game world
		graphics.fill(0, 0, width, height, 0x40000000);

		// Top Instruction Text
		String instruction = "Click and drag HUD elements to customize their positions";
		graphics.drawCenteredString(font, instruction, width / 2, 10, 0xFFE0E5EE);

		computeBoundingBoxes();

		// Render Alert Banner
		renderAlertBox(graphics, mouseX, mouseY);

		// Render Current Ping Box
		renderPingBox(graphics, mouseX, mouseY);

		// Render buttons
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private void computeBoundingBoxes() {
		// 1. Alert dimensions
		int titleWidth = font.width(ALERT_TITLE);
		int detailWidth = font.width(ALERT_DETAIL);
		int textWidth = Math.max(titleWidth, detailWidth);
		int contentWidth = SPRITE_SIZE + 6 + textWidth;
		alertBoxW = contentWidth + PADDING * 2;
		int textBlockHeight = font.lineHeight * 2 + LINE_GAP;
		alertBoxH = Math.max(SPRITE_SIZE, textBlockHeight) + PADDING * 2;

		int baseAlertLeft = getBaseAlertLeft(alertBoxW);
		int baseAlertTop = TOP_MARGIN;
		alertBoxX = baseAlertLeft + workingConfig.alertOffsetX();
		alertBoxY = baseAlertTop + workingConfig.alertOffsetY();

		// 2. Ping dimensions
		int textW = font.width(PING_SAMPLE_TEXT);
		CurrentPingStyle style = workingConfig.currentPingStyle();
		if (style == CurrentPingStyle.TEXT_ONLY) {
			pingBoxW = textW + 4;
			pingBoxH = font.lineHeight + 4;
		} else {
			pingBoxW = textW + PADDING * 2;
			pingBoxH = font.lineHeight + PADDING * 2;
		}

		int basePingLeft = CURRENT_PING_MARGIN;
		int basePingTop = CURRENT_PING_MARGIN;
		pingBoxX = basePingLeft + workingConfig.currentPingOffsetX();
		pingBoxY = basePingTop + workingConfig.currentPingOffsetY();
	}

	private void renderAlertBox(GuiGraphics graphics, int mouseX, int mouseY) {
		boolean hovered = mouseX >= alertBoxX && mouseX <= alertBoxX + alertBoxW && mouseY >= alertBoxY && mouseY <= alertBoxY + alertBoxH;

		// Alert background & borders
		graphics.fill(alertBoxX, alertBoxY, alertBoxX + alertBoxW, alertBoxY + alertBoxH, 0xEE0C0E14);
		int borderColor = (activeDrag == DragTarget.ALERT || hovered) ? 0xFFFFFFFF : 0xFFD95721;
		graphics.renderOutline(alertBoxX, alertBoxY, alertBoxW, alertBoxH, borderColor);
		graphics.renderOutline(alertBoxX + 1, alertBoxY + 1, alertBoxW - 2, alertBoxH - 2, 0xFF801A0A);

		// Tactical alert sprite
		AlertSprite sprite = workingConfig.alertSprite();
		ResourceLocation fullTexture = new ResourceLocation("pingspikeindicator", "textures/gui/sprites/alert/" + sprite.spritePath() + ".png");
		int spriteX = alertBoxX + PADDING + 1;
		int spriteY = alertBoxY + (alertBoxH - SPRITE_SIZE) / 2;
		graphics.blit(fullTexture, spriteX, spriteY, 0, 0, SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE);

		// Alert text
		int textX = spriteX + SPRITE_SIZE + 5;
		int textY = alertBoxY + (alertBoxH - (font.lineHeight * 2 + LINE_GAP)) / 2;
		graphics.drawString(font, ALERT_TITLE, textX, textY, 0xFFFFAA00, true);
		graphics.drawString(font, ALERT_DETAIL, textX, textY + font.lineHeight + LINE_GAP, 0xFFFFFFFF, true);

		// Drag handle hint
		if (hovered || activeDrag == DragTarget.ALERT) {
			graphics.renderOutline(alertBoxX - 2, alertBoxY - 2, alertBoxW + 4, alertBoxH + 4, workingConfig.accentTheme().accent());
		}
	}

	private void renderPingBox(GuiGraphics graphics, int mouseX, int mouseY) {
		boolean hovered = mouseX >= pingBoxX && mouseX <= pingBoxX + pingBoxW && mouseY >= pingBoxY && mouseY <= pingBoxY + pingBoxH;
		CurrentPingStyle style = workingConfig.currentPingStyle();

		if (style == CurrentPingStyle.TEXT_ONLY) {
			if (hovered || activeDrag == DragTarget.CURRENT_PING) {
				graphics.fill(pingBoxX, pingBoxY, pingBoxX + pingBoxW, pingBoxY + pingBoxH, 0x40000000);
				graphics.renderOutline(pingBoxX, pingBoxY, pingBoxW, pingBoxH, workingConfig.accentTheme().accent());
			}
			graphics.drawString(font, PING_SAMPLE_TEXT, pingBoxX + 2, pingBoxY + 2, 0xFFFFFFFF, true);
			return;
		}

		// Background
		graphics.fill(pingBoxX, pingBoxY, pingBoxX + pingBoxW, pingBoxY + pingBoxH, 0xEE0C0E14);

		int borderColor;
		if (hovered || activeDrag == DragTarget.CURRENT_PING) {
			borderColor = 0xFFFFFFFF;
		} else if (style == CurrentPingStyle.FANCY_ACCENT) {
			borderColor = workingConfig.accentTheme().accent();
		} else {
			borderColor = 0x80353A47;
		}
		graphics.renderOutline(pingBoxX, pingBoxY, pingBoxW, pingBoxH, borderColor);
		graphics.drawString(font, PING_SAMPLE_TEXT, pingBoxX + PADDING, pingBoxY + PADDING, 0xFFFFFFFF, true);

		if (hovered || activeDrag == DragTarget.CURRENT_PING) {
			graphics.renderOutline(pingBoxX - 2, pingBoxY - 2, pingBoxW + 4, pingBoxH + 4, workingConfig.accentTheme().accent());
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private final class EditorButton extends AbstractButton {
		private final Consumer<EditorButton> clickAction;
		private final int bgColor;
		private final int textColor;

		EditorButton(int x, int y, int width, int height, Component message, Consumer<EditorButton> clickAction, int bgColor, int textColor) {
			super(x, y, width, height, message);
			this.clickAction = clickAction;
			this.bgColor = bgColor;
			this.textColor = textColor;
		}

		@Override
		public void onPress() {
			clickAction.accept(this);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			defaultButtonNarrationText(narrationElementOutput);
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int x = getX();
			int y = getY();
			int w = getWidth();
			int h = getHeight();
			boolean hovered = isHoveredOrFocused();

			graphics.fill(x, y, x + w, y + h, hovered ? (bgColor | 0xAA000000) : bgColor);
			graphics.renderOutline(x, y, w, h, hovered ? 0xFFFFFFFF : 0x80404655);
			graphics.drawCenteredString(font, getMessage(), x + w / 2, y + (h - font.lineHeight) / 2 + 1, hovered ? 0xFFFFFFFF : textColor);
		}
	}
}
