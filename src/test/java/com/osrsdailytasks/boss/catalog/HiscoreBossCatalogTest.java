package com.osrsdailytasks.boss.catalog;

import java.util.HashSet;
import java.util.Set;
import com.osrsdailytasks.boss.model.BossDefinition;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HiscoreBossCatalogTest
{
	@Test
	public void snapshotsEveryCurrentHiscoreBossWithStableUniqueIdentifiers()
	{
		HiscoreBossCatalog catalog = new HiscoreBossCatalog();
		Set<String> taskIds = new HashSet<>();
		Set<String> subjects = new HashSet<>();
		int expectedBosses = 0;
		for (HiscoreSkill skill : HiscoreSkill.values())
		{
			if (skill.getType() == HiscoreSkillType.BOSS)
			{
				expectedBosses++;
				assertTrue(catalog.findBySubjectId(skill.name()).isPresent());
			}
		}

		assertEquals(expectedBosses, catalog.getDefinitions().size());
		for (BossDefinition definition : catalog.getDefinitions())
		{
			assertTrue(taskIds.add(definition.getTaskId()));
			assertTrue(subjects.add(definition.getSubjectId()));
			assertFalse(definition.getTitle().trim().isEmpty());
		}
		assertEquals("boss-giant-mole", catalog.findBySubjectId("GIANT_MOLE").get().getTaskId());
		assertFalse(catalog.findBySubjectId("DOOM_OF_MOKHAIOTL").get().isTrackerSupported());
		assertTrue(catalog.findBySubjectId("GIANT_MOLE").get().isTrackerSupported());
	}

	@Test
	public void catalogIsImmutable()
	{
		try
		{
			new HiscoreBossCatalog().getDefinitions().clear();
			fail("Expected immutable definitions");
		}
		catch (UnsupportedOperationException expected)
		{
			// Expected.
		}
	}

	@Test
	public void normalizesBarrowsDisplayAlias()
	{
		HiscoreBossCatalog catalog = new HiscoreBossCatalog();
		assertEquals("BARROWS_CHESTS", catalog.findByDisplayName("Barrows chest").get().getSubjectId());
		assertEquals("KRIL_TSUTSAROTH", catalog.findByDisplayName("K'ril Tsutsaroth").get().getSubjectId());
	}
}
