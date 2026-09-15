package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;

/**
 * Visual sprite styles available for the in-game ping spike alert HUD banner.
 */
public enum AlertSprite {
	SIGNAL_BARS("signal_bars_alert", "Signal Bars"),
	SATELLITE("satellite_alert", "Satellite"),
	PLUG("plug_disconnect", "Plug"),
	WIFI("wifi_alert", "Wi-Fi Wave");

	private final String spritePath;
	private final String label;

	AlertSprite(String spritePath, String label) {
		this.spritePath = Objects.requireNonNull(spritePath, "spritePath");
		this.label = Objects.requireNonNull(label, "label");
	}

	public String spritePath() {
		return spritePath;
	}

	public String label() {
		return label;
	}

	public AlertSprite next() {
		AlertSprite[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	public static AlertSprite fromNameOrDefault(String name) {
		if (name == null) {
			return SIGNAL_BARS;
		}
		for (AlertSprite sprite : values()) {
			if (sprite.name().equalsIgnoreCase(name) || sprite.spritePath.equalsIgnoreCase(name)) {
				return sprite;
			}
		}
		return SIGNAL_BARS;
	}
}
