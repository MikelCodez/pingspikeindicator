package dev.mikelcodez.pingspikeindicator.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import dev.mikelcodez.pingspikeindicator.AlertSprite;
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
	private static final int DONE_BUTTON_WIDTH = CONTROL_WIDTH - TEST_BUTTON_WIDTH - HALF_GAP; // 206
	private static final int ROW_GAP = 5;
	private static final int ROW_STEP = CONTROL_HEIGHT + ROW_GAP; // 29

	// 50% opacity background fills (0x80 = 128/255 ≈ 50%)
	private static final int MODAL_BG_COLOR = 0x800C0E14;
	private static final int HEADER_BG_COLOR = 0x80141720;
	private static final int BUTTON_BG_COLOR = 0x8012151D;
	private static final int BUTTON_HOVER_BG_COLOR = 0xAA1E2432;
	private static final int SLIDER_TRACK_BG_COLOR = 0x8010131A;

	// In-screen live alert preview box styling
	private static final int PREVIEW_BORDER = 0xFFD95721;
	private static final int PREVIEW_BORDER_INNER = 0xFF801A0A;
	private static final int PREVIEW_BG = 0xEE0C0E14;
	private static final int PREVIEW_TITLE_COLOR = 0xFFFFAA00;
	private static final int PREVIEW_DETAIL_COLOR = 0xFFFFFFFF;
	private static final int PREVIEW_SPRITE_SIZE = 20;

	private static AccentTheme currentTheme = AccentTheme.CYAN_BLUE;

	private final Screen parent;
	private final Consumer<PingSpikeConfig> saveAction;
	private PingSpikeConfig workingConfig;
	private boolean testAlertActive = false;
	private long testAlertEndTime = 0;

	// Scroll state
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

		// Row 0: Accent Theme Preview (Full Width)
		addScrollable(new BlockyButton(
				left,
				y,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				accentOption(),
				button -> {
					currentTheme = currentTheme.next();
					button.setMessage(accentOption());
				},
				false,
				null
		));
		y += ROW_STEP;

		// Row 1: Visual Alerts Toggle (Full Width)
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

		// Row 2: Alert Icon (Left Half) + HUD Anchor Position (Right Half)
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

		// Row 3: Spike Threshold Slider (Full Width)
		addScrollable(new ThresholdSlider(left, y));
		y += ROW_STEP;

		// Row 4: Alert Duration (Left Half) + Sound Toggle (Right Half)
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

		// Row 5: Current Ping HUD (Separate Full Row)
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

		// Row 6: Tab List Ping Mode (Separate Full Row)
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

		int totalContentHeight = y - contentTop;
		int visibleHeight = contentBottom - contentTop;
		maxScroll = Math.max(0, totalContentHeight - visibleHeight);

		// Pinned Bottom Controls: Done (206px) + Test (56px)
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
		// Layer 1: 50% opacity translucent dark slate/obsidian backdrop
		graphics.fill(modalLeft, modalTop, modalLeft + MODAL_WIDTH, modalTop + MODAL_HEIGHT, MODAL_BG_COLOR);

		// Layer 2: Sharp, blocky 2-pixel accent border (no rounded corners)
		graphics.renderOutline(modalLeft, modalTop, MODAL_WIDTH, MODAL_HEIGHT, currentTheme.accent());
		graphics.renderOutline(modalLeft + 1, modalTop + 1, MODAL_WIDTH - 2, MODAL_HEIGHT - 2, currentTheme.borderDark());

		// Layer 3: Inset blocky header bar (50% opacity)
		int headerHeight = 24;
		graphics.fill(modalLeft + 4, modalTop + 4, modalLeft + MODAL_WIDTH - 4, modalTop + 4 + headerHeight, HEADER_BG_COLOR);
		graphics.renderOutline(modalLeft + 4, modalTop + 4, MODAL_WIDTH - 8, headerHeight, 0x80232734);
		graphics.drawCenteredString(font, title, modalLeft + MODAL_WIDTH / 2, modalTop + 12, currentTheme.accent());

		// Layer 4: Render scrollable controls inside scissor box
		graphics.enableScissor(modalLeft + 2, contentTop - 2, modalLeft + MODAL_WIDTH - 2, contentBottom + 2);
		for (AbstractWidget widget : scrollableWidgets) {
			widget.render(graphics, mouseX, mouseY, partialTick);
		}
		graphics.disableScissor();

		// Layer 5: Sleek scrollbar
		if (maxScroll > 0) {
			int scrollBarX = modalLeft + MODAL_WIDTH - 7;
			int visibleHeight = contentBottom - contentTop;
			int trackHeight = visibleHeight;
			int thumbHeight = Math.max(16, (int) ((double) visibleHeight / (visibleHeight + maxScroll) * trackHeight));
			int thumbY = contentTop + (int) ((scrollAmount / maxScroll) * (trackHeight - thumbHeight));

			graphics.fill(scrollBarX, contentTop, scrollBarX + 3, contentBottom, 0x600C0E14);
			graphics.fill(scrollBarX, thumbY, scrollBarX + 3, thumbY + thumbHeight, currentTheme.accent());
		}

		// Layer 6: Render pinned controls (Done and Test buttons) outside the scissor box!
		for (AbstractWidget widget : pinnedWidgets) {
			widget.render(graphics, mouseX, mouseY, partialTick);
		}

		// Layer 7: Live In-Screen Alert Preview (when test button is clicked)
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
		int bannerY = 8;

		// Tactical alert frame
		graphics.fill(bannerX, bannerY, bannerX + bannerW, bannerY + bannerH, PREVIEW_BG);
		graphics.renderOutline(bannerX, bannerY, bannerW, bannerH, PREVIEW_BORDER);
		graphics.renderOutline(bannerX + 1, bannerY + 1, bannerW - 2, bannerH - 2, PREVIEW_BORDER_INNER);

		// Sprite
		AlertSprite sprite = workingConfig.alertSprite();
		Identifier spriteId = Identifier.fromNamespaceAndPath("pingspikeindicator", "alert/" + sprite.spritePath());
		int spriteX = bannerX + 4;
		int spriteY = bannerY + (bannerH - PREVIEW_SPRITE_SIZE) / 2;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, spriteX, spriteY, PREVIEW_SPRITE_SIZE, PREVIEW_SPRITE_SIZE);

		// Text
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
		return Component.translatable("option.pingspikeindicator.accent", currentTheme.label());
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

	private Component durationOption() {
		int seconds = workingConfig.alertDurationMillis() / 1_000;
		return Component.translatable(
				"option.pingspikeindicator.duration.short",
				Component.translatable("value.pingspikeindicator.seconds.short", seconds)
		);
	}

	enum AccentTheme {
		CYAN_BLUE("Cyan Blue", 0xFF00D2FF, 0xFF005577),
		EMERALD_GREEN("Emerald Green", 0xFF2ECC71, 0xFF145A32),
		CRIMSON_RED("Crimson Red", 0xFFFF3344, 0xFF7B1113),
		GOLD_YELLOW("Gold Yellow", 0xFFFFCC00, 0xFF7E6000),
		PURE_WHITE("Pure White", 0xFFFFFFFF, 0xFF555555),
		OBSIDIAN_BLACK("Obsidian Black", 0xFF2D3139, 0xFF12141A);

		private final String label;
		private final int accent;
		private final int borderDark;

		AccentTheme(String label, int accent, int borderDark) {
			this.label = label;
			this.accent = accent;
			this.borderDark = borderDark;
		}

		public String label() {
			return label;
		}

		public int accent() {
			return accent;
		}

		public int borderDark() {
			return borderDark;
		}

		public AccentTheme next() {
			return values()[(ordinal() + 1) % values().length];
		}
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

			// 1. Layered translucent dark gray / obsidian button body (50% opacity, 66% on hover)
			int bgColor = hovered ? BUTTON_HOVER_BG_COLOR : BUTTON_BG_COLOR;
			graphics.fill(x, y, x + w, y + h, bgColor);

			// 2. Crisp 1-pixel blocky border
			int borderColor;
			if (isDone) {
				borderColor = hovered ? 0xFFFFFFFF : currentTheme.accent();
			} else if (hovered) {
				borderColor = currentTheme.accent();
			} else {
				borderColor = 0x80282D3B;
			}
			graphics.renderOutline(x, y, w, h, borderColor);

			// 3. Render content
			if (activeSupplier != null) {
				boolean active = activeSupplier.getAsBoolean();

				// Blocky switch track (width 46, height 16)
				int switchW = 46;
				int switchH = 16;
				int switchX = x + w - switchW - 6;
				int switchY = y + (h - switchH) / 2;

				int pipW = 12;
				int pipH = 12;
				int pipY = switchY + (switchH - pipH) / 2;

				if (active) {
					// Glowing high-opacity ON state: glowing accent track with white pip on right
					int trackBg = 0x55000000 | (currentTheme.accent() & 0x00FFFFFF);
					graphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, trackBg);
					graphics.renderOutline(switchX, switchY, switchW, switchH, currentTheme.accent());

					int pipX = switchX + switchW - pipW - 2;
					graphics.fill(pipX, pipY, pipX + pipW, pipY + pipH, 0xFFFFFFFF);
					graphics.renderOutline(pipX, pipY, pipW, pipH, currentTheme.accent());

					// Glowing ON label
					String text = "ON";
					int textX = switchX + 6;
					int textY = switchY + (switchH - font.lineHeight) / 2 + 1;
					graphics.drawString(font, text, textX, textY, 0xFFFFFFFF, true);
				} else {
					// Dimmed OFF state: dark muted track with dimmed slate pip on left
					graphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, 0x600C0E14);
					graphics.renderOutline(switchX, switchY, switchW, switchH, 0x80353A47);

					int pipX = switchX + 2;
					graphics.fill(pipX, pipY, pipX + pipW, pipY + pipH, 0xFF4B5364);
					graphics.renderOutline(pipX, pipY, pipW, pipH, 0xFF2A2E3A);

					// Dimmed OFF label
					String text = "OFF";
					int textX = switchX + switchW - font.width(text) - 6;
					int textY = switchY + (switchH - font.lineHeight) / 2 + 1;
					graphics.drawString(font, text, textX, textY, 0xFF6B7282, false);
				}

				// Left-aligned button label
				int labelColor = active ? 0xFFFFFFFF : 0xFFA0A6B4;
				graphics.drawString(font, getMessage(), x + 8, y + (h - font.lineHeight) / 2 + 1, labelColor, true);
			} else {
				// Centered text for cyclers / actions
				int textColor;
				if (isDone) {
					textColor = hovered ? 0xFFFFFFFF : currentTheme.accent();
				} else if (hovered) {
					textColor = currentTheme.accent();
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

			// 1. Layered translucent track body (50% opacity)
			graphics.fill(x, y, x + w, y + h, SLIDER_TRACK_BG_COLOR);

			// 2. Track border
			graphics.renderOutline(x, y, w, h, hovered ? currentTheme.accent() : 0x80232734);

			// 3. Filled slider progress (accent colored with 33% alpha)
			int thumbWidth = 8;
			int fillWidth = (int) (value * (w - thumbWidth));
			if (fillWidth > 0) {
				int progressColor = 0x55000000 | (currentTheme.accent() & 0x00FFFFFF);
				graphics.fill(x + 1, y + 1, x + fillWidth + thumbWidth / 2, y + h - 1, progressColor);
			}

			// 4. Slider blocky thumb handle (crisp 8px rectangular handle)
			int thumbX = x + fillWidth;
			int thumbBorder = hovered ? 0xFFFFFFFF : currentTheme.accent();
			graphics.fill(thumbX, y + 1, thumbX + thumbWidth, y + h - 1, 0xFFFFFFFF);
			graphics.renderOutline(thumbX, y + 1, thumbWidth, h - 2, thumbBorder);

			// 5. Slider centered label
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
