package com.osrsdailytasks.boss.efficiency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.osrsdailytasks.boss.catalog.BossEfficiencyCatalog;
import com.osrsdailytasks.boss.catalog.HiscoreBossCatalog;
import com.osrsdailytasks.boss.model.BossDefinition;
import com.osrsdailytasks.boss.model.BossEfficiencyDefinition;
import com.osrsdailytasks.training.account.EhpProfile;
import org.junit.Test;

public class BossEfficiencyResolverTest
{
	private final BossEfficiencyCatalog efficiencyCatalog = new BossEfficiencyCatalog();
	private final HiscoreBossCatalog bossCatalog = new HiscoreBossCatalog();
	private final BossEfficiencyResolver resolver =
		new BossEfficiencyResolver(efficiencyCatalog);

	@Test
	public void everyWomSubjectMatchesTheRuneLiteCatalogWithoutAnAlias()
	{
		Set<String> runeLiteSubjects = runeLiteSubjects();
		for (BossEfficiencyDefinition definition : efficiencyCatalog.getDefinitions())
		{
			assertTrue(
				definition.getProfile() + "/" + definition.getWomBossId(),
				runeLiteSubjects.contains(definition.getSubjectId()));
		}
	}

	@Test
	public void reportsTheReviewedProfileCoverage()
	{
		Map<EhpProfile, Set<String>> expectedFallbacks = new EnumMap<>(EhpProfile.class);
		expectedFallbacks.put(EhpProfile.MAIN, setOf("TEMPOROSS", "WINTERTODT", "ZALCANO"));
		expectedFallbacks.put(EhpProfile.IRONMAN, setOf("TEMPOROSS", "WINTERTODT", "ZALCANO"));
		expectedFallbacks.put(EhpProfile.ULTIMATE,
			setOf("SKOTIZO", "TEMPOROSS", "WINTERTODT", "ZALCANO"));

		for (EhpProfile profile : EhpProfile.values())
		{
			Set<String> fallbacks = new HashSet<>();
			Set<String> unsupported = new HashSet<>();
			int womRates = 0;
			for (BossDefinition boss : bossCatalog.getDefinitions())
			{
				BossEfficiencyResolution resolution = resolver.resolve(profile, boss);
				switch (resolution.getType())
				{
					case WOM_RATE:
						womRates++;
						break;
					case PVM_TIER_FALLBACK:
						fallbacks.add(boss.getSubjectId());
						assertTrue(boss.isClassified());
						boss.getPvmDifficulty().getMinimumTarget();
						boss.getPvmDifficulty().getMaximumTarget();
						break;
					case TRACKER_UNSUPPORTED:
						unsupported.add(boss.getSubjectId());
						break;
					default:
						throw new AssertionError(resolution.getType());
				}
			}

			assertEquals(expectedFallbacks.get(profile), fallbacks);
			assertEquals(setOf("DOOM_OF_MOKHAIOTL"), unsupported);
			assertEquals(70 - fallbacks.size(), womRates);
		}
	}

	@Test
	public void womContainsNoBossAbsentFromRuneLite()
	{
		Set<String> runeLiteSubjects = runeLiteSubjects();
		Set<String> womOnly = new HashSet<>();
		for (BossEfficiencyDefinition definition : efficiencyCatalog.getDefinitions())
		{
			if (!runeLiteSubjects.contains(definition.getSubjectId()))
			{
				womOnly.add(definition.getSubjectId());
			}
		}
		assertTrue(womOnly.isEmpty());
	}

	@Test
	public void trackerUnsupportedBossNeverUsesItsAvailableWomRate()
	{
		BossDefinition doom = bossCatalog.findBySubjectId("DOOM_OF_MOKHAIOTL").orElse(null);
		for (EhpProfile profile : EhpProfile.values())
		{
			assertTrue(efficiencyCatalog.find(profile, doom.getSubjectId()).isPresent());
			BossEfficiencyResolution resolution = resolver.resolve(profile, doom);
			assertEquals(
				BossEfficiencyResolutionType.TRACKER_UNSUPPORTED,
				resolution.getType());
			assertFalse(resolution.getEfficiencyDefinition().isPresent());
		}
	}

	@Test
	public void matchedBossRetainsItsProfileSpecificDefinition()
	{
		BossDefinition vorkath = bossCatalog.findBySubjectId("VORKATH").orElse(null);
		BossEfficiencyResolution resolution = resolver.resolve(EhpProfile.ULTIMATE, vorkath);

		assertEquals(BossEfficiencyResolutionType.WOM_RATE, resolution.getType());
		assertEquals(EhpProfile.ULTIMATE, resolution.getProfile());
		assertEquals(vorkath, resolution.getBoss());
		assertEquals("28", resolution.getEfficiencyDefinition().get()
			.getKillsPerHour().toPlainString());
	}

	private Set<String> runeLiteSubjects()
	{
		Set<String> subjects = new HashSet<>();
		for (BossDefinition boss : bossCatalog.getDefinitions())
		{
			subjects.add(boss.getSubjectId());
		}
		return subjects;
	}

	private static Set<String> setOf(String first, String... remaining)
	{
		Set<String> values = new HashSet<>();
		values.add(first);
		for (String value : remaining)
		{
			values.add(value);
		}
		return values;
	}
}
