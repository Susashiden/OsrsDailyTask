package com.osrsdailytasks.boss.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.osrsdailytasks.boss.model.BossEfficiencyDefinition;
import com.osrsdailytasks.training.account.EhpProfile;
import org.junit.Test;

public class BossEfficiencyCatalogTest
{
	private final BossEfficiencyCatalog catalog = new BossEfficiencyCatalog();

	@Test
	public void containsTheCompleteCompiledSnapshot()
	{
		assertEquals(203, catalog.getDefinitions().size());
		int[] expectedCounts = {68, 68, 67};
		for (int index = 0; index < EhpProfile.values().length; index++)
		{
			EhpProfile profile = EhpProfile.values()[index];
			int count = 0;
			for (BossEfficiencyDefinition definition : catalog.getDefinitions())
			{
				if (definition.getProfile() == profile)
				{
					count++;
				}
			}
			assertEquals(expectedCounts[index], count);
		}
	}

	@Test
	public void findsProfileSpecificRatesByStableSubject()
	{
		assertRate(EhpProfile.MAIN, "VORKATH", "34");
		assertRate(EhpProfile.IRONMAN, "vorkath", "34");
		assertRate(EhpProfile.ULTIMATE, " VORKATH ", "28");
		assertFalse(catalog.find(EhpProfile.ULTIMATE, "SKOTIZO").isPresent());
	}

	@Test
	public void definitionsHaveUniqueKeysAndCompleteSourceMetadata()
	{
		Set<String> keys = new HashSet<>();
		for (BossEfficiencyDefinition definition : catalog.getDefinitions())
		{
			assertTrue(keys.add(
				definition.getProfile() + "/" + definition.getSubjectId()));
			assertTrue(definition.getKillsPerHour().compareTo(BigDecimal.ZERO) > 0);
			assertEquals(WomEhbSnapshot.SNAPSHOT_VERSION, definition.getSourceVersion());
			assertEquals(
				WomEhbSnapshot.SOURCE_TEMPLATE.replace(
					"{profile}", definition.getProfile().name().toLowerCase()),
				definition.getSourceId());
			assertNotNull(definition.getWomBossId());
		}
	}

	@Test
	public void definitionsAreDeterministicallyOrdered()
	{
		int previousProfile = -1;
		String previousBoss = null;
		for (BossEfficiencyDefinition definition : catalog.getDefinitions())
		{
			int profile = definition.getProfile().ordinal();
			assertTrue(profile >= previousProfile);
			if (profile == previousProfile)
			{
				assertTrue(previousBoss.compareTo(definition.getWomBossId()) < 0);
			}
			else
			{
				previousProfile = profile;
			}
			previousBoss = definition.getWomBossId();
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void definitionsAreImmutable()
	{
		catalog.getDefinitions().clear();
	}

	@Test
	public void rejectsDuplicateProfileAndSubjectKeys()
	{
		List<BossEfficiencyDefinition> definitions = new ArrayList<>();
		definitions.add(definition(EhpProfile.MAIN, "vorkath", "VORKATH", "34"));
		definitions.add(definition(EhpProfile.MAIN, "other_vorkath", "VORKATH", "35"));

		try
		{
			new BossEfficiencyCatalog(definitions);
			fail("Expected duplicate key rejection");
		}
		catch (IllegalArgumentException expected)
		{
			assertTrue(expected.getMessage().contains("MAIN/VORKATH"));
		}
	}

	@Test
	public void definitionRejectsInvalidValues()
	{
		assertInvalid(() -> definition(EhpProfile.MAIN, "", "VORKATH", "34"));
		assertInvalid(() -> definition(EhpProfile.MAIN, "vorkath", "Vorkath", "34"));
		assertInvalid(() -> definition(EhpProfile.MAIN, "vorkath", "VORKATH", "0"));
		assertInvalid(() -> new BossEfficiencyDefinition(
			EhpProfile.MAIN,
			"vorkath",
			"VORKATH",
			BigDecimal.ONE,
			" ",
			"2026-08-27"));
	}

	private void assertRate(EhpProfile profile, String subjectId, String expectedRate)
	{
		BossEfficiencyDefinition definition = catalog.find(profile, subjectId).orElse(null);
		assertNotNull(definition);
		assertEquals(0, new BigDecimal(expectedRate).compareTo(definition.getKillsPerHour()));
	}

	private static BossEfficiencyDefinition definition(
		EhpProfile profile,
		String womBossId,
		String subjectId,
		String rate)
	{
		return new BossEfficiencyDefinition(
			profile,
			womBossId,
			subjectId,
			new BigDecimal(rate),
			"https://example.test",
			"2026-08-27");
	}

	private static void assertInvalid(Runnable operation)
	{
		try
		{
			operation.run();
			fail("Expected invalid definition rejection");
		}
		catch (IllegalArgumentException expected)
		{
			// Expected.
		}
	}
}
