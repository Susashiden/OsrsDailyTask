package com.osrsdailytasks.tracking;

import javax.inject.Singleton;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class BrimhavenArenaTagDetector
{
	public static final String SUBJECT_ID = "BRIMHAVEN_ARENA_TAGS";

	private static final int AGILITY_ARENA_REGION_ID = 11157;

	private WorldPoint lastTicketPosition;

	public boolean update(WorldPoint playerLocation, WorldPoint ticketPosition)
	{
		if (playerLocation == null || playerLocation.getRegionID() != AGILITY_ARENA_REGION_ID)
		{
			reset();
			return false;
		}

		if (ticketPosition == null)
		{
			return false;
		}

		WorldPoint previousPosition = lastTicketPosition;
		lastTicketPosition = ticketPosition;
		return previousPosition != null
			&& (previousPosition.getX() != ticketPosition.getX()
				|| previousPosition.getY() != ticketPosition.getY());
	}

	public void reset()
	{
		lastTicketPosition = null;
	}
}
