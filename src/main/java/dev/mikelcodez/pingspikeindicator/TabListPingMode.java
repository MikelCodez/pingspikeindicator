package dev.mikelcodez.pingspikeindicator;

/**
 * Display modes for player latency in the Tab player list overlay.
 */
public enum TabListPingMode {
	BOTH("Both"),
	NUMBERS_ONLY("Numbers"),
	VANILLA_BARS("Vanilla");

	private final String label;

	TabListPingMode(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public TabListPingMode next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public static TabListPingMode fromString(String raw) {
		if (raw == null) {
			return BOTH;
		}
		for (TabListPingMode mode : values()) {
			if (mode.name().equalsIgnoreCase(raw.trim()) || mode.label.equalsIgnoreCase(raw.trim())) {
				return mode;
			}
		}
		return BOTH;
	}
}
