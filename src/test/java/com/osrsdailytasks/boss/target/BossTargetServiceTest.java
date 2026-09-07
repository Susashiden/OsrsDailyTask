package com.osrsdailytasks.boss.target;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.boss.catalog.BossEfficiencyCatalog;
import com.osrsdailytasks.boss.catalog.HiscoreBossCatalog;
import com.osrsdailytasks.boss.efficiency.BossEfficiencyResolver;
import com.osrsdailytasks.training.account.EhpProfile;
import org.junit.Test;

public class BossTargetServiceTest
{
	@Test
	public void usesTheCurrentProfilesWomRate()
	{
		assertRange(service(EhpProfile.MAIN).calculateCurrent(
			"VORKATH", TaskDifficulty.NORMAL), 6, 10);
		assertRange(service(EhpProfile.ULTIMATE).calculateCurrent(
			"VORKATH", TaskDifficulty.NORMAL), 5, 8);
	}

	@Test
	public void appliesTaskDifficultyWithoutApplyingPvmTierToWomRates()
	{
		BossTargetService service = service(EhpProfile.MAIN);
		assertRange(service.calculateCurrent("VORKATH", TaskDifficulty.EASY), 3, 5);
		assertRange(service.calculateCurrent("VORKATH", TaskDifficulty.NORMAL), 6, 10);
		assertRange(service.calculateCurrent("VORKATH", TaskDifficulty.HARD), 12, 21);
	}

	@Test
	public void usesTheReviewedPvmTierFallbackWhenWomHasNoBossRate()
	{
		BossTargetService service = service(EhpProfile.MAIN);
		assertRange(service.calculateCurrent("TEMPOROSS", TaskDifficulty.EASY), 2, 4);
		assertRange(service.calculateCurrent("TEMPOROSS", TaskDifficulty.NORMAL), 4, 8);
		assertRange(service.calculateCurrent("TEMPOROSS", TaskDifficulty.HARD), 8, 16);
	}

	@Test
	public void fallbackRemainsProfileSpecific()
	{
		assertRange(service(EhpProfile.MAIN).calculateCurrent(
			"SKOTIZO", TaskDifficulty.NORMAL), 8, 14);
		assertRange(service(EhpProfile.ULTIMATE).calculateCurrent(
			"SKOTIZO", TaskDifficulty.NORMAL), 4, 8);
	}

	@Test
	public void rejectsUnknownAndTrackerUnsupportedBosses()
	{
		BossTargetService service = service(EhpProfile.MAIN);
		assertRejected(
			() -> service.calculateCurrent("UNKNOWN", TaskDifficulty.NORMAL),
			"Unknown boss subject: UNKNOWN");
		assertRejected(
			() -> service.calculateCurrent("DOOM_OF_MOKHAIOTL", TaskDifficulty.NORMAL),
			"Boss tracker is unsupported: DOOM_OF_MOKHAIOTL");
	}

	@Test
	public void unsupportedCurrentAccountProfileFailsClosed()
	{
		BossTargetService service = service(null);
		assertFalse(service.isCurrentProfileSupported());
		try
		{
			service.calculateCurrent("VORKATH", TaskDifficulty.NORMAL);
			fail("Expected unsupported account profile rejection");
		}
		catch (NullPointerException expected)
		{
			assertEquals("current EHB profile", expected.getMessage());
		}
	}

	private static BossTargetService service(EhpProfile profile)
	{
		HiscoreBossCatalog bossCatalog = new HiscoreBossCatalog();
		return new BossTargetService(
			bossCatalog,
			new BossEfficiencyResolver(new BossEfficiencyCatalog()),
			new BossTargetCalculator(),
			profile);
	}

	private static void assertRange(BossTargetRange range, int minimum, int maximum)
	{
		assertEquals(minimum, range.getMinimum());
		assertEquals(maximum, range.getMaximum());
	}

	private static void assertRejected(Runnable operation, String expectedMessage)
	{
		try
		{
			operation.run();
			fail("Expected boss target rejection");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(expectedMessage, expected.getMessage());
		}
	}
}
