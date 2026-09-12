package dev.mikelcodez.pingspikeindicator;

final class BoundedPingHistory {
	private final int[] samples;
	private int nextIndex;
	private int size;
	private long totalPingMillis;

	BoundedPingHistory(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException("capacity must be positive");
		}

		this.samples = new int[capacity];
	}

	void add(int pingMillis) {
		if (pingMillis < 0) {
			throw new IllegalArgumentException("pingMillis must not be negative");
		}

		if (size == samples.length) {
			totalPingMillis -= samples[nextIndex];
		} else {
			size++;
		}

		samples[nextIndex] = pingMillis;
		totalPingMillis += pingMillis;
		nextIndex = (nextIndex + 1) % samples.length;
	}

	double averageMillis() {
		if (size == 0) {
			throw new IllegalStateException("cannot calculate an average without samples");
		}

		return (double) totalPingMillis / size;
	}

	int size() {
		return size;
	}

	int capacity() {
		return samples.length;
	}

	void clear() {
		nextIndex = 0;
		size = 0;
		totalPingMillis = 0;
	}
}
