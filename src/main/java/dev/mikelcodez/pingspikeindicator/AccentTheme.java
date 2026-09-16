package dev.mikelcodez.pingspikeindicator;

import java.util.Objects;

/**
 * Persisted color theme used across the settings dialog and fancy HUD borders.
 */
public enum AccentTheme {
	CYAN_BLUE("Cyan Blue", 0xFF00D2FF, 0xFF005577),
	EMERALD_GREEN("Emerald Green", 0xFF2ECC71, 0xFF145A32),
	CRIMSON_RED("Crimson Red", 0xFFFF3344, 0xFF7B1113),
	GOLD_YELLOW("Gold Yellow", 0xFFFFCC00, 0xFF7E6000),
	PURE_WHITE("Pure White", 0xFFFFFFFF, 0xFF555555);

	private final String label;
	private final int accent;
	private final int borderDark;

	AccentTheme(String label, int accent, int borderDark) {
		this.label = Objects.requireNonNull(label, "label");
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
		AccentTheme[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	public static AccentTheme fromStringOrDefault(String raw) {
		if (raw == null) {
			return CYAN_BLUE;
		}
		for (AccentTheme theme : values()) {
			if (theme.name().equalsIgnoreCase(raw.trim()) || theme.label.equalsIgnoreCase(raw.trim())) {
				return theme;
			}
		}
		return CYAN_BLUE;
	}
}
