package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;

/**
 * Screen anchor positions for the transient ping spike alert HUD banner.
 */
public enum HudPosition {
	TOP_CENTER("Top Center"),
	TOP_LEFT("Top Left"),
	TOP_RIGHT("Top Right");

	private final String label;

	HudPosition(String label) {
		this.label = Objects.requireNonNull(label, "label");
	}

	public String label() {
		return label;
	}

	public HudPosition next() {
		HudPosition[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	public static HudPosition fromNameOrDefault(String name) {
		if (name == null) {
			return TOP_CENTER;
		}
		for (HudPosition position : values()) {
			if (position.name().equalsIgnoreCase(name)) {
				return position;
			}
		}
		return TOP_CENTER;
	}
}
