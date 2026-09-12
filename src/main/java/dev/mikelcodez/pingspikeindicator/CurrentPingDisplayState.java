package dev.mikelcodez.pingspikeindicator;

/**
 * Caches the optional current-ping text outside the per-frame render path.
 */
public final class CurrentPingDisplayState {
	private String text = "";

	public void accept(int pingMillis) {
		if (pingMillis < 0) {
			throw new IllegalArgumentException("pingMillis must not be negative");
		}
		text = pingMillis + " ms";
	}

	public String text() {
		return text;
	}

	public boolean available() {
		return !text.isEmpty();
	}

	public void clear() {
		text = "";
	}
}
