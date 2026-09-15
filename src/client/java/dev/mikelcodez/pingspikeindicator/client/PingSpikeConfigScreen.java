package dev.mikelcodez.pingspikeindicator.client;

import java.util.function.Consumer;

import dev.mikelcodez.pingspikeindicator.PingSpikeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class PingSpikeConfigScreen extends Screen {
	private static final int CONTROL_WIDTH = 220;
	private static final int CONTROL_HEIGHT = 20;
	private static final int ROW_GAP = 4;
	private static final int ROW_STEP = CONTROL_HEIGHT + ROW_GAP;

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
		int left = (width - CONTROL_WIDTH) / 2;
		int top = Math.max(32, height / 2 - 72);

		addRenderableWidget(Button.builder(booleanOption("alerts", workingConfig.alertsEnabled()), button -> {
			workingConfig = workingConfig.withAlertsEnabled(!workingConfig.alertsEnabled());
			button.setMessage(booleanOption("alerts", workingConfig.alertsEnabled()));
		}).bounds(left, top, CONTROL_WIDTH, CONTROL_HEIGHT).build());

		addRenderableWidget(new ThresholdSlider(left, top + ROW_STEP));

		addRenderableWidget(Button.builder(durationOption(), button -> {
			int nextDuration = switch (workingConfig.alertDurationMillis()) {
				case 2_000 -> 3_000;
				case 3_000 -> 5_000;
				default -> 2_000;
			};
			workingConfig = workingConfig.withAlertDurationMillis(nextDuration);
			button.setMessage(durationOption());
		}).bounds(left, top + ROW_STEP * 2, CONTROL_WIDTH, CONTROL_HEIGHT).build());

		addRenderableWidget(Button.builder(booleanOption("sound", workingConfig.soundEnabled()), button -> {
			workingConfig = workingConfig.withSoundEnabled(!workingConfig.soundEnabled());
			button.setMessage(booleanOption("sound", workingConfig.soundEnabled()));
		}).bounds(left, top + ROW_STEP * 3, CONTROL_WIDTH, CONTROL_HEIGHT).build());

		addRenderableWidget(Button.builder(booleanOption("current_ping", workingConfig.currentPingVisible()), button -> {
			workingConfig = workingConfig.withCurrentPingVisible(!workingConfig.currentPingVisible());
			button.setMessage(booleanOption("current_ping", workingConfig.currentPingVisible()));
		}).bounds(left, top + ROW_STEP * 4, CONTROL_WIDTH, CONTROL_HEIGHT).build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
				.bounds(left, top + ROW_STEP * 5 + 8, CONTROL_WIDTH, CONTROL_HEIGHT)
				.build());
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
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(font, title, width / 2, Math.max(12, height / 2 - 94), 0xFFFFFFFF);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private Component booleanOption(String key, boolean enabled) {
		return Component.translatable(
				"option.pingspikeindicator." + key,
				Component.translatable(enabled ? "options.on" : "options.off")
		);
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
	}
}
