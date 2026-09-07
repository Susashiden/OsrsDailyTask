package com.osrsdailytasks.task.generation;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.boss.catalog.BossEfficiencyCatalog;
import com.osrsdailytasks.boss.catalog.HiscoreBossCatalog;
import com.osrsdailytasks.boss.efficiency.BossEfficiencyResolver;
import com.osrsdailytasks.boss.target.BossTargetCalculator;
import com.osrsdailytasks.boss.target.BossTargetService;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.catalog.TrainingMethodCatalog;
import com.osrsdailytasks.training.target.XpTargetCalculator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TaskGeneratorTest
{
	private static final LocalDate TEST_DATE = LocalDate.of(2026, 8, 18);

	@Test
	public void filtersDefinitionsBeforeSelection()
	{
		TaskDefinition xp = definition("xp", TaskType.XP, 10, 20);
		TaskDefinition boss = definition("boss", TaskType.BOSS, 1, 3);
		TaskGenerator generator = new TaskGenerator(Arrays.asList(xp, boss), new SequenceRandom(0, 0, 2));

		ActiveTask task = generator.generate(TEST_DATE, definition -> definition.getType() == TaskType.BOSS);

		assertEquals("boss", task.getTaskId());
		assertEquals(TaskType.BOSS, task.getTaskType());
		assertEquals(3, task.getTargetAmount());
		assertEquals(TEST_DATE, task.getGenerationDate());
	}

	@Test
	public void generatesInclusiveTargetBoundaries()
	{
		TaskDefinition definition = definition("xp", TaskType.XP, 10, 20);

		ActiveTask minimum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 0)).generate(TEST_DATE, ignored -> true);
		ActiveTask maximum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 10)).generate(TEST_DATE, ignored -> true);

		assertEquals(10, minimum.getTargetAmount());
		assertEquals(20, maximum.getTargetAmount());
	}

	@Test
	public void appliesEasyDifficultyToBothBoundaries()
	{
		TaskDefinition definition = definition("activity", TaskType.ACTIVITY, 3, 7);

		ActiveTask minimum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.EASY).generate(TEST_DATE, ignored -> true);
		ActiveTask maximum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 2),
			TaskDifficulty.EASY).generate(TEST_DATE, ignored -> true);

		assertEquals(1, minimum.getTargetAmount());
		assertEquals(3, maximum.getTargetAmount());
	}

	@Test
	public void appliesHardDifficultyToBothBoundaries()
	{
		TaskDefinition definition = definition("xp", TaskType.XP, 10, 20);

		ActiveTask minimum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.HARD).generate(TEST_DATE, ignored -> true);
		ActiveTask maximum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 20),
			TaskDifficulty.HARD).generate(TEST_DATE, ignored -> true);

		assertEquals(20, minimum.getTargetAmount());
		assertEquals(40, maximum.getTargetAmount());
	}

	@Test
	public void floorsDecimalBoundariesAfterScalingAndKeepsAtLeastOne()
	{
		TaskDefinition definition = definition("clue", TaskType.ACTIVITY, 1.5, 4);

		ActiveTask easyMinimum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.EASY).generate(TEST_DATE, ignored -> true);
		ActiveTask hardMinimum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.HARD).generate(TEST_DATE, ignored -> true);
		ActiveTask hardMaximum = new TaskGenerator(
			Collections.singletonList(definition),
			new SequenceRandom(0, 0, 5),
			TaskDifficulty.HARD).generate(TEST_DATE, ignored -> true);

		assertEquals(1, easyMinimum.getTargetAmount());
		assertEquals(3, hardMinimum.getTargetAmount());
		assertEquals(8, hardMaximum.getTargetAmount());
	}

	@Test
	public void excludesDefinitionsAboveConfiguredDifficulty()
	{
		TaskDefinition regular = definition("regular", TaskType.ACTIVITY, 1, 1);
		TaskDefinition hardOnly = new TaskDefinition(
			"hard-only",
			TaskType.ACTIVITY,
			"HARD_ONLY",
			"Hard only",
			1,
			1,
			TaskDifficulty.HARD,
			false);
		TaskGenerator generator = new TaskGenerator(
			Arrays.asList(regular, hardOnly),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.NORMAL);

		ActiveTask task = generator.generate(TEST_DATE, ignored -> true);

		assertEquals("regular", task.getTaskId());
	}

	@Test
	public void hardDifficultyAllowsHardOnlyFixedTargetsWithoutScaling()
	{
		TaskDefinition hardOnly = new TaskDefinition(
			"master-clue",
			TaskType.ACTIVITY,
			"CLUE_SCROLL_MASTER",
			"Complete master clue scrolls",
			1,
			1,
			TaskDifficulty.HARD,
			false);
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(hardOnly),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.HARD);

		ActiveTask task = generator.generate(TEST_DATE, ignored -> true);

		assertEquals(1, task.getTargetAmount());
	}

	@Test
	public void selectsCategoriesBeforeDefinitionsSoCategoryWeightIsIndependentOfSize()
	{
		TaskDefinition xp = definition("xp", TaskType.XP, 1, 1);
		TaskDefinition bossOne = definition("boss-one", TaskType.BOSS, 1, 1);
		TaskDefinition bossTwo = definition("boss-two", TaskType.BOSS, 1, 1);
		TaskDefinition activity = definition("activity", TaskType.ACTIVITY, 1, 1);
		TaskGenerator generator = new TaskGenerator(
			Arrays.asList(xp, bossOne, bossTwo, activity),
			new SequenceRandom(1, 1, 0));

		ActiveTask task = generator.generate(TEST_DATE, ignored -> true);

		assertEquals(TaskType.BOSS, task.getTaskType());
		assertEquals("boss-two", task.getTaskId());
	}

	@Test
	public void rejectsAnEmptyEligiblePool()
	{
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(definition("xp", TaskType.XP, 10, 20)),
			new SequenceRandom());

		try
		{
			generator.generate(TEST_DATE, ignored -> false);
			fail("Expected an empty eligible pool to be rejected");
		}
		catch (IllegalStateException expected)
		{
			assertEquals("No eligible daily task definitions are available", expected.getMessage());
		}
	}

	@Test
	public void generatesTheSelectedDevelopmentTaskUsingConfiguredDifficulty()
	{
		TaskDefinition xp = definition("xp", TaskType.XP, 10, 20);
		TaskDefinition boss = definition("boss", TaskType.BOSS, 1, 3);
		TaskGenerator generator = new TaskGenerator(
			Arrays.asList(xp, boss),
			new SequenceRandom(2),
			TaskDifficulty.NORMAL);

		ActiveTask task = generator.generateSpecific(TEST_DATE, "boss", ignored -> true);

		assertEquals("boss", task.getTaskId());
		assertEquals(3, task.getTargetAmount());
	}

	@Test
	public void rejectsASelectedDevelopmentTaskAboveConfiguredDifficulty()
	{
		TaskDefinition hardOnly = new TaskDefinition(
			"master-clue",
			TaskType.ACTIVITY,
			"CLUE_SCROLL_MASTER",
			"Complete master clue scrolls",
			1,
			1,
			TaskDifficulty.HARD,
			false);
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(hardOnly),
			new SequenceRandom(),
			TaskDifficulty.NORMAL);

		try
		{
			generator.generateSpecific(TEST_DATE, "master-clue", ignored -> true);
			fail("Expected the ineligible selected task to be rejected");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(
				"Selected task is not eligible at the current difficulty: master-clue",
				expected.getMessage());
		}
	}

	@Test
	public void rejectsAnUnknownSelectedDevelopmentTask()
	{
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(definition("xp", TaskType.XP, 10, 20)),
			new SequenceRandom());

		try
		{
			generator.generateSpecific(TEST_DATE, "missing", ignored -> true);
			fail("Expected the unknown selected task to be rejected");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals("Unknown task definition: missing", expected.getMessage());
		}
	}

	@Test
	public void productionXpTargetUsesTheSelectedProfilesTrainingMethodRate()
	{
		TaskDefinition magic = new TaskDefinition(
			"xp-magic",
			TaskType.XP,
			"MAGIC",
			"Gain Magic XP",
			1,
			1);
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(magic),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.NORMAL,
			new XpTargetCalculator(new TrainingMethodCatalog(), EhpProfile.MAIN));

		ActiveTask task = generator.generate(TEST_DATE, ignored -> true);

		assertEquals(155_250, task.getTargetAmount());
	}

	@Test
	public void unsupportedAccountProfileFailsClosedForXpWithoutBlockingOtherCategories()
	{
		TaskDefinition magic = new TaskDefinition(
			"xp-magic", TaskType.XP, "MAGIC", "Gain Magic XP", 1, 1);
		TaskDefinition activity = new TaskDefinition(
			"activity", TaskType.ACTIVITY, "ACTIVITY", "Complete activity", 1, 1);
		XpTargetCalculator calculator = new XpTargetCalculator(
			new TrainingMethodCatalog(),
			(EhpProfile) null);
		TaskGenerator generator = new TaskGenerator(
			Arrays.asList(magic, activity),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.NORMAL,
			calculator);

		ActiveTask task = generator.generate(TEST_DATE, ignored -> true);

		assertEquals("activity", task.getTaskId());
		try
		{
			generator.generateSpecific(TEST_DATE, "xp-magic", ignored -> true);
			fail("Expected XP assignment to fail closed for an unsupported account profile");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(
				"Selected task is not eligible at the current difficulty: xp-magic",
				expected.getMessage());
		}
	}

	@Test
	public void productionBossTargetUsesTheSelectedProfilesEhbRate()
	{
		TaskDefinition vorkath = new TaskDefinition(
			"boss-vorkath", TaskType.BOSS, "VORKATH", "Defeat Vorkath", 3, 6);
		TaskGenerator mainGenerator = new TaskGenerator(
			Collections.singletonList(vorkath),
			new SequenceRandom(0),
			TaskDifficulty.NORMAL,
			null,
			bossTargetService(EhpProfile.MAIN));
		TaskGenerator ultimateGenerator = new TaskGenerator(
			Collections.singletonList(vorkath),
			new SequenceRandom(3),
			TaskDifficulty.NORMAL,
			null,
			bossTargetService(EhpProfile.ULTIMATE));

		ActiveTask main = mainGenerator.generateSpecific(
			TEST_DATE, "boss-vorkath", ignored -> true);
		ActiveTask ultimate = ultimateGenerator.generateSpecific(
			TEST_DATE, "boss-vorkath", ignored -> true);

		assertEquals(6, main.getTargetAmount());
		assertEquals(8, ultimate.getTargetAmount());
	}

	@Test
	public void productionBossTargetUsesTheReviewedFallbackRange()
	{
		TaskDefinition tempoross = new TaskDefinition(
			"boss-tempoross", TaskType.BOSS, "TEMPOROSS", "Complete Tempoross", 4, 8);
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(tempoross),
			new SequenceRandom(8),
			TaskDifficulty.HARD,
			null,
			bossTargetService(EhpProfile.MAIN));

		ActiveTask task = generator.generateSpecific(
			TEST_DATE, "boss-tempoross", ignored -> true);

		assertEquals(16, task.getTargetAmount());
	}

	@Test
	public void unsupportedAccountProfileFailsClosedForBossWithoutBlockingOtherCategories()
	{
		TaskDefinition boss = new TaskDefinition(
			"boss-vorkath", TaskType.BOSS, "VORKATH", "Defeat Vorkath", 3, 6);
		TaskDefinition activity = new TaskDefinition(
			"activity", TaskType.ACTIVITY, "ACTIVITY", "Complete activity", 1, 1);
		TaskGenerator generator = new TaskGenerator(
			Arrays.asList(boss, activity),
			new SequenceRandom(0, 0, 0),
			TaskDifficulty.NORMAL,
			null,
			bossTargetService(null));

		ActiveTask task = generator.generate(TEST_DATE, ignored -> true);

		assertEquals("activity", task.getTaskId());
		try
		{
			generator.generateSpecific(TEST_DATE, "boss-vorkath", ignored -> true);
			fail("Expected boss assignment to fail closed for an unsupported account profile");
		}
		catch (IllegalArgumentException expected)
		{
			assertEquals(
				"Selected task is not eligible at the current difficulty: boss-vorkath",
				expected.getMessage());
		}
	}

	private static BossTargetService bossTargetService(EhpProfile profile)
	{
		HiscoreBossCatalog bossCatalog = new HiscoreBossCatalog();
		return new BossTargetService(
			bossCatalog,
			new BossEfficiencyResolver(new BossEfficiencyCatalog()),
			new BossTargetCalculator(),
			profile);
	}

	private static TaskDefinition definition(String id, TaskType type, double minimum, double maximum)
	{
		return new TaskDefinition(id, type, id + "-subject", id + " title", minimum, maximum);
	}

	private static final class SequenceRandom extends Random
	{
		private final int[] values;
		private int index;

		private SequenceRandom(int... values)
		{
			this.values = values;
		}

		@Override
		public int nextInt(int bound)
		{
			if (index >= values.length)
			{
				throw new AssertionError("No random value configured for call " + index);
			}
			return Math.floorMod(values[index++], bound);
		}
	}
}
