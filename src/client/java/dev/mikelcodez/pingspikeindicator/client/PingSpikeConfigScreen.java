package dev.mikelcodez.pingspikeindicator.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import dev.mikelcodez.pingspikeindicator.AccentTheme;
import dev.mikelcodez.pingspikeindicator.AlertSprite;
import dev.mikelcodez.pingspikeindicator.CurrentPingStyle;
import dev.mikelcodez.pingspikeindicator.HudPosition;
import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import dev.mikelcodez.pingspikeindicator.TabListPingMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

final class PingSpikeConfigScreen extends Screen {
	private static final int MODAL_WIDTH = 290;
	private static final int MODAL_HEIGHT = 246;
	private static final int CONTROL_WIDTH = 266;
	private static final int CONTROL_HEIGHT = 24;
	private static final int HALF_CONTROL_WIDTH = 131;
	private static final int HALF_GAP = 4;
	private static final int TEST_BUTTON_WIDTH = 56;
	private static final int DONE_BUTTON_WIDTH = CONTROL_WIDTH - TEST_BUTTON_WIDTH - HALF_GAP;
	private static final int ROW_GAP = 5;
	private static final int ROW_STEP = CONTROL_HEIGHT + ROW_GAP;

	private static final int MODAL_BG_COLOR = 0x800C0E14;
	private static final int HEADER_BG_COLOR = 0x80141720;
	private static final int BUTTON_BG_COLOR = 0x8012151D;
	private static final int BUTTON_HOVER_BG_COLOR = 0xAA1E2432;
	private static final int SLIDER_TRACK_BG_COLOR = 0x8010131A;

	private static final int PREVIEW_BORDER = 0xFFD95721;
	private static final int PREVIEW_BORDER_INNER = 0xFF801A0A;
	private static final int PREVIEW_BG = 0xEE0C0E14;
	private static final int PREVIEW_TITLE_COLOR = 0xFFFFAA00;
	private static final int PREVIEW_DETAIL_COLOR = 0xFFFFFFFF;
	private static final int PREVIEW_SPRITE_SIZE = 20;

	private final Screen parent;
	private final Consumer<PingSpikeConfig> saveAction;
	private PingSpikeConfig workingConfig;
	private boolean testAlertActive = false;
	private long testAlertEndTime = 0;

	private final List<AbstractWidget> scrollableWidgets = new ArrayList<>();
	private final List<AbstractWidget> pinnedWidgets = new ArrayList<>();
	private final List<Integer> initialWidgetY = new ArrayList<>();
	private double scrollAmount = 0.0;
	private int maxScroll = 0;
	private boolean isDraggingScrollBar = false;
	private int contentTop = 0;
	private int contentBottom = 0;
	private int modalLeft = 0;
	private int modalTop = 0;

	PingSpikeConfigScreen(Screen parent, PingSpikeConfig initialConfig, Consumer<PingSpikeConfig> saveAction) {
		super(Component.translatable("screen.pingspikeindicator.title"));
		this.parent = parent;
		this.workingConfig = initialConfig;
		this.saveAction = saveAction;
	}

	@Override
	protected void init() {
		scrollableWidgets.clear();
		pinnedWidgets.clear();
		initialWidgetY.clear();
		scrollAmount = 0.0;

		modalLeft = (width - MODAL_WIDTH) / 2;
		modalTop = Math.max(8, (height - MODAL_HEIGHT) / 2);
		int left = modalLeft + (MODAL_WIDTH - CONTROL_WIDTH) / 2;
		contentTop = modalTop + 34;
		contentBottom = modalTop + MODAL_HEIGHT - CONTROL_HEIGHT - 12;

		int y = contentTop;

		addScrollable(new BlockyButton(
				left,
				y,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				accentOption(),
				button -> {
					workingConfig = workingConfig.withAccentTheme(workingConfig.accentTheme().next());
					button.setMessage(accentOption());
				},
				false,
				null
		));
		y += ROW_STEP;

		addScrollable(new BlockyButton(
				left,
				y,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.alerts.label"),
				button -> workingConfig = workingConfig.withAlertsEnabled(!workingConfig.alertsEnabled()),
				false,
				() -> workingConfig.alertsEnabled()
		));
		y += ROW_STEP;

		int halfLeft2 = left + HALF_CONTROL_WIDTH + HALF_GAP;
		addScrollable(new BlockyButton(
				left,
				y,
				HALF_CONTROL_WIDTH,
				CONTROL_HEIGHT,
				alertSpriteOption(),
				button -> {
					workingConfig = workingConfig.withAlertSprite(workingConfig.alertSprite().next());
					button.setMessage(alertSpriteOption());
				},
				false,
				null
		));
		addScrollable(new BlockyButton(
				halfLeft2,
				y,
				HALF_CONTROL_WIDTH,
				CONTROL_HEIGHT,
				hudPositionOption(),
				button -> {
					workingConfig = workingConfig.withHudPosition(workingConfig.hudPosition().next());
					button.setMessage(hudPositionOption());
				},
				false,
				null
		));
		y += ROW_STEP;

		addScrollable(new ThresholdSlider(left, y));
		y += ROW_STEP;

		addScrollable(new BlockyButton(
				left,
				y,
				HALF_CONTROL_WIDTH,
				CONTROL_HEIGHT,
				durationOption(),
				button -> {
					int nextDuration = switch (workingConfig.alertDurationMillis()) {
						case 2_000 -> 3_000;
						case 3_000 -> 5_000;
						default -> 2_000;
					};
					workingConfig = workingConfig.withAlertDurationMillis(nextDuration);
					button.setMessage(durationOption());
				},
				false,
				null
		));
		addScrollable(new BlockyButton(
				halfLeft2,
				y,
				HALF_CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.sound.short"),
				button -> workingConfig = workingConfig.withSoundEnabled(!workingConfig.soundEnabled()),
				false,
				() -> workingConfig.soundEnabled()
		));
		y += ROW_STEP;

		addScrollable(new BlockyButton(
				left,
				y,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.current_ping.label"),
				button -> workingConfig = workingConfig.withCurrentPingVisible(!workingConfig.currentPingVisible()),
				false,
				() -> workingConfig.currentPingVisible()
		));
		y += ROW_STEP;

		addScrollable(new BlockyButton(
				left,
				y,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				currentPingStyleOption(),
				button -> {
					workingConfig = workingConfig.withCurrentPingStyle(workingConfig.currentPingStyle().next());
					button.setMessage(currentPingStyleOption());
				},
				false,
				null
		));
		y += ROW_STEP;

		addScrollable(new BlockyButton(
				left,
				y,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				tabListPingOption(),
				button -> {
					workingConfig = workingConfig.withTabListPingMode(workingConfig.tabListPingMode().next());
					button.setMessage(tabListPingOption());
				},
				false,
				null
		));
		y += ROW_STEP;

		addScrollable(new BlockyButton(
				left,
				y,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.edit_hud_positions"),
				button -> openHudPositioningScreen(),
				false,
				null
		));
		y += ROW_STEP;

		int totalContentHeight = y - contentTop;
		int visibleHeight = contentBottom - contentTop;
		maxScroll = Math.max(0, totalContentHeight - visibleHeight);

		int bottomRowY = modalTop + MODAL_HEIGHT - CONTROL_HEIGHT - 6;
		BlockyButton doneBtn = new BlockyButton(
				left,
				bottomRowY,
				DONE_BUTTON_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("gui.done"),
				button -> onClose(),
				true,
				null
		);
		pinnedWidgets.add(doneBtn);
		addRenderableWidget(doneBtn);

		BlockyButton testBtn = new BlockyButton(
				left + DONE_BUTTON_WIDTH + HALF_GAP,
				bottomRowY,
				TEST_BUTTON_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.test_alert"),
				button -> triggerTestAlert(),
				false,
				null
		);
		pinnedWidgets.add(testBtn);
		addRenderableWidget(testBtn);
	}

	private void openHudPositioningScreen() {
		if (minecraft != null) {
			minecraft.setScreen(new HudPositioningScreen(
					this,
					workingConfig,
					updatedConfig -> {
						this.workingConfig = updatedConfig;
					}
			));
		}
	}

	private void addScrollable(AbstractWidget widget) {
		scrollableWidgets.add(widget);
		initialWidgetY.add(widget.getY());
		addRenderableWidget(widget);
	}

	private void updateScrollPositions() {
		int offset = (int) Math.round(scrollAmount);
		for (int i = 0; i < scrollableWidgets.size(); i++) {
			AbstractWidget widget = scrollableWidgets.get(i);
			int origY = initialWidgetY.get(i);
			int newY = origY - offset;
			widget.setY(newY);
			widget.visible = (newY + widget.getHeight() >= contentTop && newY <= contentBottom);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (maxScroll > 0 && mouseY >= contentTop && mouseY <= contentBottom) {
			scrollAmount = Mth.clamp(scrollAmount - verticalAmount * 18.0, 0.0, maxScroll);
			updateScrollPositions();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (maxScroll > 0 && event.button() == 0) {
			int scrollBarX = modalLeft + MODAL_WIDTH - 8;
			if (event.x() >= scrollBarX - 4 && event.x() <= scrollBarX + 8 && event.y() >= contentTop && event.y() <= contentBottom) {
				isDraggingScrollBar = true;
				updateScrollFromMouse(event.y());
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0) {
			isDraggingScrollBar = false;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
		if (isDraggingScrollBar && maxScroll > 0) {
			updateScrollFromMouse(event.y());
			return true;
		}
		return super.mouseDragged(event, deltaX, deltaY);
	}

	private void updateScrollFromMouse(double mouseY) {
		int visibleHeight = contentBottom - contentTop;
		double ratio = (mouseY - contentTop) / (double) visibleHeight;
		scrollAmount = Mth.clamp(ratio * maxScroll, 0.0, maxScroll);
		updateScrollPositions();
	}

	private void triggerTestAlert() {
		testAlertActive = true;
		testAlertEndTime = System.currentTimeMillis() + workingConfig.alertDurationMillis();
		if (workingConfig.soundEnabled() && minecraft != null) {
			minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.25F));
		}
	}

	@Override
	public void onClose() {
		saveAction.accept(workingConfig);
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Modal background and border outline
		graphics.fill(modalLeft, modalTop, modalLeft + MODAL_WIDTH, modalTop + MODAL_HEIGHT, MODAL_BG_COLOR);

		
		graphics.renderOutline(modalLeft, modalTop, MODAL_WIDTH, MODAL_HEIGHT, workingConfig.accentTheme().accent());
		graphics.renderOutline(modalLeft + 1, modalTop + 1, MODAL_WIDTH - 2, MODAL_HEIGHT - 2, workingConfig.accentTheme().borderDark());

		// Inset header bar
		int headerHeight = 24;
		graphics.fill(modalLeft + 4, modalTop + 4, modalLeft + MODAL_WIDTH - 4, modalTop + 4 + headerHeight, HEADER_BG_COLOR);
		graphics.renderOutline(modalLeft + 4, modalTop + 4, MODAL_WIDTH - 8, headerHeight, 0x80232734);
		graphics.drawCenteredString(font, title, modalLeft + MODAL_WIDTH / 2, modalTop + 12, workingConfig.accentTheme().accent());

		// Scissored scrollable controls
		graphics.enableScissor(modalLeft + 2, contentTop - 2, modalLeft + MODAL_WIDTH - 2, contentBottom + 2);
		for (AbstractWidget widget : scrollableWidgets) {
			widget.render(graphics, mouseX, mouseY, partialTick);
		}
		graphics.disableScissor();

		// Scrollbar
		if (maxScroll > 0) {
			int scrollBarX = modalLeft + MODAL_WIDTH - 7;
			int visibleHeight = contentBottom - contentTop;
			int trackHeight = visibleHeight;
			int thumbHeight = Math.max(16, (int) ((double) visibleHeight / (visibleHeight + maxScroll) * trackHeight));
			int thumbY = contentTop + (int) ((scrollAmount / maxScroll) * (trackHeight - thumbHeight));

			graphics.fill(scrollBarX, contentTop, scrollBarX + 3, contentBottom, 0x600C0E14);
			graphics.fill(scrollBarX, thumbY, scrollBarX + 3, thumbY + thumbHeight, workingConfig.accentTheme().accent());
		}

		// Pinned bottom controls
		for (AbstractWidget widget : pinnedWidgets) {
			widget.render(graphics, mouseX, mouseY, partialTick);
		}

		// In-screen alert preview
		long now = System.currentTimeMillis();
		if (testAlertActive && now < testAlertEndTime) {
			renderInScreenAlertPreview(graphics);
		} else if (testAlertActive) {
			testAlertActive = false;
		}
	}

	private void renderInScreenAlertPreview(GuiGraphics graphics) {
		String titleStr = "PING SPIKE";
		String detailStr = "+" + workingConfig.spikeThresholdMillis() + " ms (" + (50 + workingConfig.spikeThresholdMillis()) + " ms)";

		int textWidth = Math.max(font.width(titleStr), font.width(detailStr));
		int bannerW = PREVIEW_SPRITE_SIZE + 6 + textWidth + 8;
		int bannerH = 26;

		int bannerX;
		HudPosition position = workingConfig.hudPosition();
		switch (position) {
			case TOP_LEFT -> bannerX = 10;
			case TOP_RIGHT -> bannerX = width - bannerW - 10;
			default -> bannerX = (width - bannerW) / 2;
		}
		bannerX += workingConfig.alertOffsetX();
		int bannerY = 8 + workingConfig.alertOffsetY();

		graphics.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, PREVIEW_BG);
		graphics.renderOutline(bannerX, bannerY, bannerW, bannerH, PREVIEW_BORDER);
		graphics.renderOutline(bannerX + 1, bannerY + 1, bannerW - 2, bannerH - 2, PREVIEW_BORDER_INNER);

		AlertSprite sprite = workingConfig.alertSprite();
		Identifier spriteId = Identifier.fromNamespaceAndPath("pingspikeindicator", "alert/" + sprite.spritePath());
		int spriteX = bannerX + 4;
		int spriteY = bannerY + (bannerH - PREVIEW_SPRITE_SIZE) / 2;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, spriteX, spriteY, PREVIEW_SPRITE_SIZE, PREVIEW_SPRITE_SIZE);

		int textX = spriteX + PREVIEW_SPRITE_SIZE + 5;
		int textY = bannerY + 4;
		graphics.drawString(font, titleStr, textX, textY, PREVIEW_TITLE_COLOR, true);
		graphics.drawString(font, detailStr, textX, textY + font.lineHeight + 1, PREVIEW_DETAIL_COLOR, true);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private Component accentOption() {
		return Component.translatable("option.pingspikeindicator.accent", workingConfig.accentTheme().label());
	}

	private Component alertSpriteOption() {
		return Component.translatable("option.pingspikeindicator.alert_sprite", workingConfig.alertSprite().label());
	}

	private Component hudPositionOption() {
		return Component.translatable("option.pingspikeindicator.hud_position", workingConfig.hudPosition().label());
	}

	private Component tabListPingOption() {
		return Component.translatable("option.pingspikeindicator.tab_list_ping", workingConfig.tabListPingMode().label());
	}

	private Component currentPingStyleOption() {
		return Component.translatable("option.pingspikeindicator.current_ping_style", workingConfig.currentPingStyle().label());
	}

	private Component durationOption() {
		int seconds = workingConfig.alertDurationMillis() / 1_000;
		return Component.translatable(
				"option.pingspikeindicator.duration.short",
				Component.translatable("value.pingspikeindicator.seconds.short", seconds)
		);
	}

	private final class BlockyButton extends AbstractButton {
		private final boolean isDone;
		private final BooleanSupplier activeSupplier;
		private final Consumer<BlockyButton> clickAction;

		BlockyButton(
				int x,
				int y,
				int width,
				int height,
				Component message,
				Consumer<BlockyButton> clickAction,
				boolean isDone,
				BooleanSupplier activeSupplier
		) {
			super(x, y, width, height, message);
			this.clickAction = clickAction;
			this.isDone = isDone;
			this.activeSupplier = activeSupplier;
		}

		@Override
		public void onPress(InputWithModifiers input) {
			clickAction.accept(this);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			defaultButtonNarrationText(narrationElementOutput);
		}

		@Override
		protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int x = getX();
			int y = getY();
			int w = getWidth();
			int h = getHeight();
			boolean hovered = isHoveredOrFocused();

			int bgColor = hovered ? BUTTON_HOVER_BG_COLOR : BUTTON_BG_COLOR;
			graphics.fill(x, y, x + w, y + h, bgColor);

			int borderColor;
			if (isDone) {
				borderColor = hovered ? 0xFFFFFFFF : workingConfig.accentTheme().accent();
			} else if (hovered) {
				borderColor = workingConfig.accentTheme().accent();
			} else {
				borderColor = 0x80282D3B;
			}
			graphics.renderOutline(x, y, w, h, borderColor);

			
			if (activeSupplier != null) {
				boolean active = activeSupplier.getAsBoolean();

				int switchW = 46;
				int switchH = 16;
				int switchX = x + w - switchW - 6;
				int switchY = y + (h - switchH) / 2;

				int pipW = 12;
				int pipH = 12;
				int pipY = switchY + (switchH - pipH) / 2;

				if (active) {
						int trackBg = 0x55000000 | (workingConfig.accentTheme().accent() & 0x00FFFFFF);
					graphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, trackBg);
					graphics.renderOutline(switchX, switchY, switchW, switchH, workingConfig.accentTheme().accent());

					int pipX = switchX + switchW - pipW - 2;
					graphics.fill(pipX, pipY, pipX + pipW, pipY + pipH, 0xFFFFFFFF);
					graphics.renderOutline(pipX, pipY, pipW, pipH, workingConfig.accentTheme().accent());

						String text = "ON";
					int textX = switchX + 6;
					int textY = switchY + (switchH - font.lineHeight) / 2 + 1;
					graphics.drawString(font, text, textX, textY, 0xFFFFFFFF, true);
				} else {
						graphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, 0x600C0E14);
					graphics.renderOutline(switchX, switchY, switchW, switchH, 0x80353A47);

					int pipX = switchX + 2;
					graphics.fill(pipX, pipY, pipX + pipW, pipY + pipH, 0xFF4B5364);
					graphics.renderOutline(pipX, pipY, pipW, pipH, 0xFF2A2E3A);

						String text = "OFF";
					int textX = switchX + switchW - font.width(text) - 6;
					int textY = switchY + (switchH - font.lineHeight) / 2 + 1;
					graphics.drawString(font, text, textX, textY, 0xFF6B7282, false);
				}

				int labelColor = active ? 0xFFFFFFFF : 0xFFA0A6B4;
				graphics.drawString(font, getMessage(), x + 8, y + (h - font.lineHeight) / 2 + 1, labelColor, true);
			} else {
				int textColor;
				if (isDone) {
					textColor = hovered ? 0xFFFFFFFF : workingConfig.accentTheme().accent();
				} else if (hovered) {
					textColor = workingConfig.accentTheme().accent();
				} else {
					textColor = 0xFFE0E5EE;
				}
				graphics.drawCenteredString(font, getMessage(), x + w / 2, y + (h - font.lineHeight) / 2 + 1, textColor);
			}
		}
	}

	private final class ThresholdSlider extends AbstractSliderButton {
		private ThresholdSlider(int x, int y) {
			super(
					x,
					y,
					CONTROL_WIDTH,
					CONTROL_HEIGHT,
					Component.translatable(
							"option.pingspikeindicator.threshold",
							workingConfig.spikeThresholdMillis()
					),
					normalizedThreshold(workingConfig.spikeThresholdMillis())
			);
		}

		@Override
		protected void updateMessage() {
			setMessage(Component.translatable(
					"option.pingspikeindicator.threshold",
					workingConfig.spikeThresholdMillis()
			));
		}

		@Override
		protected void applyValue() {
			workingConfig = workingConfig.withSpikeThresholdMillis(thresholdFromNormalized(value));
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int x = getX();
			int y = getY();
			int w = getWidth();
			int h = getHeight();
			boolean hovered = isHoveredOrFocused();

			// Slider track
			graphics.fill(x, y, x + w, y + h, SLIDER_TRACK_BG_COLOR);

			graphics.renderOutline(x, y, w, h, hovered ? workingConfig.accentTheme().accent() : 0x80232734);

			int thumbWidth = 8;
			int fillWidth = (int) (value * (w - thumbWidth));
			if (fillWidth > 0) {
				int progressColor = 0x55000000 | (workingConfig.accentTheme().accent() & 0x00FFFFFF);
				graphics.fill(x + 1, y + 1, x + fillWidth + thumbWidth / 2, y + h - 1, progressColor);
			}

			int thumbX = x + fillWidth;
			int thumbBorder = hovered ? 0xFFFFFFFF : workingConfig.accentTheme().accent();
			graphics.fill(thumbX, y + 1, thumbX + thumbWidth, y + h - 1, 0xFFFFFFFF);
			graphics.renderOutline(thumbX, y + 1, thumbWidth, h - 2, thumbBorder);

			int textColor = hovered ? 0xFFFFFFFF : 0xFFE0E5EE;
			graphics.drawCenteredString(font, getMessage(), x + w / 2, y + (h - font.lineHeight) / 2 + 1, textColor);
		}

		private static double normalizedThreshold(int thresholdMillis) {
			return (thresholdMillis - 50.0) / (250.0 - 50.0);
		}

		private static int thresholdFromNormalized(double normalized) {
			return (int) Math.round(50.0 + normalized * 200.0);
		}
	}
}
