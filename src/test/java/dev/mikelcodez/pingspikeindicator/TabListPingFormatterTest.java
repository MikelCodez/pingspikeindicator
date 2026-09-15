package dev.mikelcodez.pingspikeindicator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TabListPingFormatterTest {

	@Test
	@DisplayName("Negative latency formats as question mark and unknown color")
	void negativeLatency() {
		assertEquals("?", TabListPingFormatter.formatPing(-1));
		assertEquals("?", TabListPingFormatter.formatPing(-50));
		assertEquals(TabListPingFormatter.COLOR_UNKNOWN, TabListPingFormatter.pingColor(-1));
	}

	@Test
	@DisplayName("Zero latency formats as <1ms and excellent green color")
	void zeroLatency() {
		assertEquals("<1ms", TabListPingFormatter.formatPing(0));
		assertEquals(TabListPingFormatter.COLOR_EXCELLENT, TabListPingFormatter.pingColor(0));
	}

	@Test
	@DisplayName("Positive latency formatting and tier colors")
	void positiveLatency() {
		// Tier 1: < 60ms
		assertEquals("35ms", TabListPingFormatter.formatPing(35));
		assertEquals(TabListPingFormatter.COLOR_EXCELLENT, TabListPingFormatter.pingColor(35));
		assertEquals(TabListPingFormatter.COLOR_EXCELLENT, TabListPingFormatter.pingColor(59));

		// Tier 2: 60-119ms
		assertEquals("60ms", TabListPingFormatter.formatPing(60));
		assertEquals(TabListPingFormatter.COLOR_GOOD, TabListPingFormatter.pingColor(60));
		assertEquals("119ms", TabListPingFormatter.formatPing(119));
		assertEquals(TabListPingFormatter.COLOR_GOOD, TabListPingFormatter.pingColor(119));

		// Tier 3: 120-199ms
		assertEquals("120ms", TabListPingFormatter.formatPing(120));
		assertEquals(TabListPingFormatter.COLOR_MODERATE, TabListPingFormatter.pingColor(120));
		assertEquals("199ms", TabListPingFormatter.formatPing(199));
		assertEquals(TabListPingFormatter.COLOR_MODERATE, TabListPingFormatter.pingColor(199));

		// Tier 4: >= 200ms
		assertEquals("200ms", TabListPingFormatter.formatPing(200));
		assertEquals(TabListPingFormatter.COLOR_POOR, TabListPingFormatter.pingColor(200));
		assertEquals("999ms", TabListPingFormatter.formatPing(999));
		assertEquals(TabListPingFormatter.COLOR_POOR, TabListPingFormatter.pingColor(999));
	}

	@Test
	@DisplayName("TabListPingMode enum string parsing and cycling")
	void tabListPingMode() {
		assertEquals(TabListPingMode.BOTH, TabListPingMode.fromString(null));
		assertEquals(TabListPingMode.BOTH, TabListPingMode.fromString("both"));
		assertEquals(TabListPingMode.BOTH, TabListPingMode.fromString("BOTH"));
		assertEquals(TabListPingMode.NUMBERS_ONLY, TabListPingMode.fromString("NUMBERS_ONLY"));
		assertEquals(TabListPingMode.NUMBERS_ONLY, TabListPingMode.fromString("Numbers"));
		assertEquals(TabListPingMode.VANILLA_BARS, TabListPingMode.fromString("VANILLA_BARS"));
		assertEquals(TabListPingMode.VANILLA_BARS, TabListPingMode.fromString("Vanilla"));

		assertEquals(TabListPingMode.NUMBERS_ONLY, TabListPingMode.BOTH.next());
		assertEquals(TabListPingMode.VANILLA_BARS, TabListPingMode.NUMBERS_ONLY.next());
		assertEquals(TabListPingMode.BOTH, TabListPingMode.VANILLA_BARS.next());
	}
}
