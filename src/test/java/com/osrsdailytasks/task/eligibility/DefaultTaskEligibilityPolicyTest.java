package com.osrsdailytasks.task.eligibility;

import com.osrsdailytasks.PvmDifficulty;
import com.osrsdailytasks.boss.catalog.HiscoreBossCatalog;
import com.osrsdailytasks.boss.model.BossDefinition;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultTaskEligibilityPolicyTest
{
	private final HiscoreBossCatalog bossCatalog = new HiscoreBossCatalog();

	@Test
	public void anyAllowsEverySupportedClassifiedBoss()
	{
		DefaultTaskEligibilityPolicy policy = policy(PvmDifficulty.ANY);
		for (BossDefinition boss : bossCatalog.getDefinitions())
		{
			assertTrue(policy.isEligible(taskFor(boss)) == boss.isTrackerSupported());
		}
	}

	@Test
	public void configuredMaximumRejectsBossesAboveIt()
	{
		DefaultTaskEligibilityPolicy policy = policy(PvmDifficulty.MEDIUM);
		for (BossDefinition boss : bossCatalog.getDefinitions())
		{
			assertTrue(policy.isEligible(taskFor(boss))
				== (boss.isTrackerSupported()
					&& PvmDifficulty.MEDIUM.allows(boss.getPvmDifficulty())));
		}
	}

	@Test
	public void nonBossTasksRemainEligibleAndUnknownBossesFailClosed()
	{
		DefaultTaskEligibilityPolicy policy = policy(PvmDifficulty.ANY);
		assertTrue(policy.isEligible(new TaskDefinition("xp", TaskType.XP, "XP", "XP", 1, 1)));
		assertFalse(policy.isEligible(new TaskDefinition("boss-new", TaskType.BOSS, "NEW", "New", 1, 1)));
	}

	private DefaultTaskEligibilityPolicy policy(PvmDifficulty difficulty)
	{
		return new DefaultTaskEligibilityPolicy(bossCatalog, () -> difficulty);
	}

	private static TaskDefinition taskFor(BossDefinition boss)
	{
		return new TaskDefinition(
			boss.getTaskId(),
			TaskType.BOSS,
			boss.getSubjectId(),
			boss.getTitle(),
			boss.getPvmDifficulty().getMinimumTarget(),
			boss.getPvmDifficulty().getMaximumTarget());
	}
}
