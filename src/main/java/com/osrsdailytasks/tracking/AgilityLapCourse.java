package com.osrsdailytasks.tracking;

import net.runelite.api.coords.WorldPoint;

public enum AgilityLapCourse
{
	DRAYNOR(12338, new WorldPoint(3103, 3261, 0)),
	AL_KHARID(13105, new WorldPoint(3299, 3194, 0)),
	VARROCK(12853, new WorldPoint(3236, 3417, 0)),
	CANIFIS(13878, new WorldPoint(3510, 3485, 0)),
	FALADOR(
		12084,
		new WorldPoint(3029, 3332, 0),
		new WorldPoint(3029, 3333, 0),
		new WorldPoint(3029, 3334, 0),
		new WorldPoint(3029, 3335, 0)),
	SEERS(10806, new WorldPoint(2704, 3464, 0)),
	POLLNIVNEACH(13358, new WorldPoint(3363, 2998, 0)),
	RELLEKKA(10553, new WorldPoint(2653, 3676, 0)),
	ARDOUGNE(10547, new WorldPoint(2668, 3297, 0)),
	APE_ATOLL(11050, new WorldPoint(2770, 2747, 0)),
	PRIFDDINAS(12895, new WorldPoint(3240, 6109, 0));

	private final int regionId;
	private final WorldPoint[] endpoints;

	AgilityLapCourse(int regionId, WorldPoint... endpoints)
	{
		this.regionId = regionId;
		this.endpoints = endpoints.clone();
	}

	public int getRegionId()
	{
		return regionId;
	}

	public WorldPoint[] getEndpoints()
	{
		return endpoints.clone();
	}

	boolean endsAt(WorldPoint location)
	{
		for (WorldPoint endpoint : endpoints)
		{
			if (endpoint.equals(location))
			{
				return true;
			}
		}
		return false;
	}
}
