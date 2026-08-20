package com.osrsdailytasks.tracking;

import net.runelite.api.gameval.NpcID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BossTaskMatcherTest
{
	private final BossTaskMatcher matcher = new BossTaskMatcher();

	@Test
	public void matchesGiantMoleByGameValueId()
	{
		assertEquals("GIANT_MOLE", matcher.subjectForNpc(NpcID.MOLE_GIANT).get());
	}

	@Test
	public void ignoresUncuratedNpcs()
	{
		assertFalse(matcher.subjectForNpc(NpcID.MOLE_BABY_01).isPresent());
	}

	@Test
	public void matchesUnambiguousCatalogDisplayNames()
	{
		assertEquals("VORKATH", matcher.subjectForNpcName("Vorkath").get());
		assertFalse(matcher.subjectForNpcName("Goblin").isPresent());
	}
}
