package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;

/**
 * Visual presentation styles for the on-screen Current Ping HUD element.
 */
public enum CurrentPingStyle {
	TRANSLUCENT("Translucent"),
	TEXT_ONLY("Text Only"),
	FANCY_ACCENT("Fancy Accent");

	private final String label;

	CurrentPingStyle(String label) {
		this.label = Objects.requireNonNull(label, "label");
	}

	public String label() {
		return label;
	}

	public CurrentPingStyle next() {
		CurrentPingStyle[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	public static CurrentPingStyle fromStringOrDefault(String raw) {
		if (raw == null) {
			return TRANSLUCENT;
		}
		for (CurrentPingStyle style : values()) {
			if (style.name().equalsIgnoreCase(raw.trim()) || style.label.equalsIgnoreCase(raw.trim())) {
				return style;
			}
		}
		return TRANSLUCENT;
	}
}
