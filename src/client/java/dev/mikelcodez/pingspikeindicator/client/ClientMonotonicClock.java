package dev.mikelcodez.pingspikeindicator.client;

final class ClientMonotonicClock {
	private final long originNanos = System.nanoTime();

	long millis() {
		return (System.nanoTime() - originNanos) / 1_000_000L;
	}
}
