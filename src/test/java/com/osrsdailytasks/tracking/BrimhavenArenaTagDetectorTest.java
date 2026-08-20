package com.osrsdailytasks.tracking;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BrimhavenArenaTagDetectorTest
{
	private static final WorldPoint ARENA_LOCATION = new WorldPoint(2760, 9570, 0);
	private static final WorldPoint FIRST_DISPENSER = new WorldPoint(2761, 9571, 0);
	private static final WorldPoint SECOND_DISPENSER = new WorldPoint(2770, 9580, 0);

	private final BrimhavenArenaTagDetector detector = new BrimhavenArenaTagDetector();

	@Test
	public void countsOnlyTicketDispenserMovementAfterInitialization()
	{
		assertFalse(detector.update(ARENA_LOCATION, FIRST_DISPENSER));
		assertFalse(detector.update(ARENA_LOCATION, FIRST_DISPENSER));
		assertTrue(detector.update(ARENA_LOCATION, SECOND_DISPENSER));
	}

	@Test
	public void ignoresMissingTicketPositionsWithoutLosingState()
	{
		assertFalse(detector.update(ARENA_LOCATION, FIRST_DISPENSER));
		assertFalse(detector.update(ARENA_LOCATION, null));
		assertTrue(detector.update(ARENA_LOCATION, SECOND_DISPENSER));
	}

	@Test
	public void leavingArenaAndExplicitResetRequireReinitialization()
	{
		assertFalse(detector.update(ARENA_LOCATION, FIRST_DISPENSER));
		assertFalse(detector.update(new WorldPoint(3200, 3200, 0), SECOND_DISPENSER));
		assertFalse(detector.update(ARENA_LOCATION, SECOND_DISPENSER));

		detector.reset();
		assertFalse(detector.update(ARENA_LOCATION, FIRST_DISPENSER));
	}
}
