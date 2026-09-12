package dev.mikelcodez.pingspikeindicator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrentPingDisplayStateTest {
	@Test
	void acceptedPingIsFormattedOnceAndCanBeCleared() {
		CurrentPingDisplayState state = new CurrentPingDisplayState();

		assertFalse(state.available());
		state.accept(123);
		assertTrue(state.available());
		assertEquals("123 ms", state.text());
		state.clear();
		assertFalse(state.available());
		assertEquals("", state.text());
	}

	@Test
	void negativePingIsRejected() {
		CurrentPingDisplayState state = new CurrentPingDisplayState();

		assertThrows(IllegalArgumentException.class, () -> state.accept(-1));
	}
}
