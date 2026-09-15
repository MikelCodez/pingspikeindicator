package dev.mikelcodez.pingspikeindicator.client;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

final class PingSpikeConfigScreen extends Screen {
	private static final int MODAL_WIDTH = 290;
	private static final int MODAL_HEIGHT = 250;
	private static final int CONTROL_WIDTH = 266;
	private static final int CONTROL_HEIGHT = 24;
	private static final int ROW_GAP = 5;
	private static final int ROW_STEP = CONTROL_HEIGHT + ROW_GAP; // 29

	// 50% opacity background fills (0x80 = 128/255 ≈ 50%)
	private static final int MODAL_BG_COLOR = 0x800C0E14;
	private static final int HEADER_BG_COLOR = 0x80141720;
	private static final int BUTTON_BG_COLOR = 0x8012151D;
	private static final int BUTTON_HOVER_BG_COLOR = 0xAA1E2432;
	private static final int SLIDER_TRACK_BG_COLOR = 0x8010131A;

	private static AccentTheme currentTheme = AccentTheme.CYAN_BLUE;

	private final Screen parent;
	private final Consumer<PingSpikeConfig> saveAction;
	private PingSpikeConfig workingConfig;
	private boolean saved;

	PingSpikeConfigScreen(Screen parent, PingSpikeConfig config, Consumer<PingSpikeConfig> saveAction) {
		super(Component.translatable("screen.pingspikeindicator.title"));
		this.parent = parent;
		this.workingConfig = config;
		this.saveAction = saveAction;
	}

	@Override
	protected void init() {
		int modalLeft = (width - MODAL_WIDTH) / 2;
		int modalTop = Math.max(10, (height - MODAL_HEIGHT) / 2);
		int left = modalLeft + (MODAL_WIDTH - CONTROL_WIDTH) / 2;
		int top = modalTop + 34;

		addRenderableWidget(new BlockyButton(
				left,
				top,
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

		addRenderableWidget(new BlockyButton(
				left,
				top + ROW_STEP,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.alerts.label"),
				button -> workingConfig = workingConfig.withAlertsEnabled(!workingConfig.alertsEnabled()),
				false,
				() -> workingConfig.alertsEnabled()
		));

		addRenderableWidget(new ThresholdSlider(left, top + ROW_STEP * 2));

		addRenderableWidget(new BlockyButton(
				left,
				top + ROW_STEP * 3,
				CONTROL_WIDTH,
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

		addRenderableWidget(new BlockyButton(
				left,
				top + ROW_STEP * 4,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.sound.label"),
				button -> workingConfig = workingConfig.withSoundEnabled(!workingConfig.soundEnabled()),
				false,
				() -> workingConfig.soundEnabled()
		));

		addRenderableWidget(new BlockyButton(
				left,
				top + ROW_STEP * 5,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("option.pingspikeindicator.current_ping.label"),
				button -> workingConfig = workingConfig.withCurrentPingVisible(!workingConfig.currentPingVisible()),
				false,
				() -> workingConfig.currentPingVisible()
		));

		addRenderableWidget(new BlockyButton(
				left,
				top + ROW_STEP * 6 + 6,
				CONTROL_WIDTH,
				CONTROL_HEIGHT,
				Component.translatable("gui.done"),
				button -> onClose(),
				true,
				null
		));
	}

	@Override
	public void onClose() {
		if (!saved) {
			saved = true;
			saveAction.accept(workingConfig);
		}
		minecraft.setScreen(parent);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		int modalLeft = (width - MODAL_WIDTH) / 2;
		int modalTop = Math.max(10, (height - MODAL_HEIGHT) / 2);

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

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private Component accentOption() {
		return Component.translatable("option.pingspikeindicator.accent", currentTheme.label());
	}

	private Component durationOption() {
		return Component.translatable(
				"option.pingspikeindicator.duration",
				Component.translatable(
						"value.pingspikeindicator.seconds",
						workingConfig.alertDurationMillis() / 1_000
				)
		);
	}

	enum AccentTheme {
		CYAN_BLUE("Cyan Blue", 0xFF00D2FF, 0xFF005577),
		EMERALD_GREEN("Emerald Green", 0xFF2ECC71, 0xFF145A32),
		CRIMSON_RED("Crimson Red", 0xFFFF3344, 0xFF7B1113);

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

					// Glowing ON label (moved down 1px to avoid touching top border)
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

					// Dimmed OFF label (moved down 1px to avoid touching top border)
					String text = "OFF";
					int textX = switchX + switchW - font.width(text) - 6;
					int textY = switchY + (switchH - font.lineHeight) / 2 + 1;
					graphics.drawString(font, text, textX, textY, 0xFF6B7282, false);
				}

				// Left-aligned button label (moved down 1px for clean vertical centering)
				int labelColor = active ? 0xFFFFFFFF : 0xFFA0A6B4;
				graphics.drawString(font, getMessage(), x + 10, y + (h - font.lineHeight) / 2 + 1, labelColor, true);
			} else {
				// Centered text for cyclers / actions (moved down 1px for clean vertical centering)
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
					Component.empty(),
					(double) (workingConfig.spikeThresholdMillis() - PingSpikeConfig.MINIMUM_THRESHOLD_MILLIS)
							/ (PingSpikeConfig.MAXIMUM_THRESHOLD_MILLIS
							- PingSpikeConfig.MINIMUM_THRESHOLD_MILLIS)
			);
			updateMessage();
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
			int stepCount = (PingSpikeConfig.MAXIMUM_THRESHOLD_MILLIS
					- PingSpikeConfig.MINIMUM_THRESHOLD_MILLIS) / PingSpikeConfig.THRESHOLD_STEP_MILLIS;
			int threshold = PingSpikeConfig.MINIMUM_THRESHOLD_MILLIS
					+ (int) Math.round(value * stepCount) * PingSpikeConfig.THRESHOLD_STEP_MILLIS;
			workingConfig = workingConfig.withSpikeThresholdMillis(threshold);
			updateMessage();
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			int x = getX();
			int y = getY();
			int w = getWidth();
			int h = getHeight();
			boolean hovered = isHoveredOrFocused();

			// 1. Dark 50% opacity slider groove
			graphics.fill(x, y, x + w, y + h, SLIDER_TRACK_BG_COLOR);
			graphics.renderOutline(x, y, w, h, hovered ? currentTheme.accent() : 0x80282D3B);

			// 2. Blocky progress fill
			int trackInnerW = w - 4;
			int fillW = (int) Math.round(this.value * trackInnerW);
			if (fillW > 0) {
				int fillAlpha = 0x44000000 | (currentTheme.accent() & 0x00FFFFFF);
				graphics.fill(x + 2, y + 2, x + 2 + fillW, y + h - 2, fillAlpha);
			}

			// 3. Blocky handle (sharp 8px block bar)
			int handleW = 8;
			int handleX = x + 2 + (int) Math.round(this.value * (trackInnerW - handleW));
			graphics.fill(handleX, y + 2, handleX + handleW, y + h - 2, currentTheme.accent());
			graphics.renderOutline(handleX, y + 2, handleW, h - 4, 0xFFFFFFFF);

			// 4. Centered text (moved down 1px for clean vertical centering)
			int textColor = hovered ? 0xFFFFFFFF : 0xFFE0E5EE;
			graphics.drawCenteredString(font, getMessage(), x + w / 2, y + (h - font.lineHeight) / 2 + 1, textColor);
		}
	}
}
