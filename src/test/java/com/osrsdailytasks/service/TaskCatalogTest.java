package com.osrsdailytasks.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.model.ClueTier;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import net.runelite.api.Skill;
import net.runelite.client.hiscore.HiscoreSkill;
import net.runelite.client.hiscore.HiscoreSkillType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskCatalogTest
{
	@Test
	public void containsOneXpTaskForEveryRealSkill()
	{
		List<TaskDefinition> definitions = new TaskCatalog().getDefinitions();
		Set<String> xpSubjects = new HashSet<>();
		for (TaskDefinition definition : definitions)
		{
			if (definition.getType() == TaskType.XP)
			{
				xpSubjects.add(definition.getSubjectId());
			}
		}

		int expectedSkills = 0;
		for (Skill skill : Skill.values())
		{
			if (!"OVERALL".equals(skill.name()))
			{
				expectedSkills++;
				assertTrue(xpSubjects.contains(skill.name()));
			}
		}
		assertEquals(expectedSkills, xpSubjects.size());
	}

	@Test
	public void taskIdsAreUnique()
	{
		List<TaskDefinition> definitions = new TaskCatalog().getDefinitions();
		Set<String> ids = new HashSet<>();
		for (TaskDefinition definition : definitions)
		{
			assertTrue("Duplicate task id: " + definition.getId(), ids.add(definition.getId()));
		}
	}

	@Test
	public void containsGenericLapAndSeparateBrimhavenTasks()
	{
		List<TaskDefinition> definitions = new TaskCatalog().getDefinitions();
		int lapTasks = 0;
		int brimhavenTasks = 0;
		for (TaskDefinition definition : definitions)
		{
			if ("AGILITY_COURSE_LAPS".equals(definition.getSubjectId()))
			{
				lapTasks++;
			}
			if ("BRIMHAVEN_ARENA_TAGS".equals(definition.getSubjectId()))
			{
				brimhavenTasks++;
			}
		}

		assertEquals(1, lapTasks);
		assertEquals(1, brimhavenTasks);
	}

	@Test
	public void generatesEveryHiscoreBossFromTheSharedBossCatalog()
	{
		List<TaskDefinition> definitions = new TaskCatalog().getDefinitions();
		HiscoreBossCatalog bossCatalog = new HiscoreBossCatalog();
		int expectedBosses = 0;
		int actualBosses = 0;
		for (HiscoreSkill skill : HiscoreSkill.values())
		{
			if (skill.getType() == HiscoreSkillType.BOSS
				&& bossCatalog.findBySubjectId(skill.name()).get().isClassified())
			{
				expectedBosses++;
			}
		}
		for (TaskDefinition definition : definitions)
		{
			if (definition.getType() == TaskType.BOSS)
			{
				actualBosses++;
				assertTrue(definition.getId().startsWith("boss-"));
				assertTrue(bossCatalog.findBySubjectId(definition.getSubjectId()).isPresent());
			}
		}
		assertEquals(expectedBosses, actualBosses);
	}

	@Test
	public void containsOnlyTierSpecificClueTasksWithHighTierRestrictions()
	{
		List<TaskDefinition> definitions = new TaskCatalog().getDefinitions();
		Set<String> clueSubjects = new HashSet<>();
		int clueDefinitions = 0;
		for (TaskDefinition definition : definitions)
		{
			if (definition.getId().startsWith("activity-clue-scroll-"))
			{
				clueDefinitions++;
				assertTrue(definition.getSubjectId().startsWith("CLUE_SCROLL_"));
				clueSubjects.add(definition.getSubjectId());
				boolean highTier = definition.getSubjectId().equals("CLUE_SCROLL_ELITE")
					|| definition.getSubjectId().equals("CLUE_SCROLL_MASTER");
				assertEquals(
					highTier ? TaskDifficulty.HARD : TaskDifficulty.EASY,
					definition.getMinimumDifficulty());
				assertTrue(definition.isScaleTargetsWithDifficulty());
				if (definition.getSubjectId().equals("CLUE_SCROLL_ELITE"))
				{
					assertEquals(0.5, definition.getMinimumTarget(), 0.0);
					assertEquals(1.5, definition.getMaximumTarget(), 0.0);
				}
				if (definition.getSubjectId().equals("CLUE_SCROLL_MASTER"))
				{
					assertEquals(0.5, definition.getMinimumTarget(), 0.0);
					assertEquals(1.0, definition.getMaximumTarget(), 0.0);
				}
			}
		}

		assertEquals(ClueTier.values().length, clueDefinitions);
		assertEquals(ClueTier.values().length, clueSubjects.size());
		for (ClueTier tier : ClueTier.values())
		{
			assertTrue(clueSubjects.contains(tier.getSubjectId()));
		}
	}

	@Test
	public void clueTargetsUseTheApprovedDecimalRangesAndScaledIntegerBoundaries()
	{
		double[][] baseTargets = {
			{4.0, 6.0},
			{3.0, 5.0},
			{2.0, 4.0},
			{1.5, 3.0},
			{0.5, 1.5},
			{0.5, 1.0}
		};
		int[][] easyTargets = {{2, 3}, {1, 2}, {1, 2}, {1, 1}, {1, 1}, {1, 1}};
		int[][] normalTargets = {{4, 6}, {3, 5}, {2, 4}, {1, 3}, {1, 1}, {1, 1}};
		int[][] hardTargets = {{8, 12}, {6, 10}, {4, 8}, {3, 6}, {1, 3}, {1, 2}};

		List<TaskDefinition> definitions = new TaskCatalog().getDefinitions();
		ClueTier[] tiers = ClueTier.values();
		for (int index = 0; index < tiers.length; index++)
		{
			TaskDefinition definition = findDefinition(
				definitions,
				"activity-clue-scroll-" + tiers[index].getMessageName());
			assertEquals(baseTargets[index][0], definition.getMinimumTarget(), 0.0);
			assertEquals(baseTargets[index][1], definition.getMaximumTarget(), 0.0);
			assertScaledTargets(definition, TaskDifficulty.EASY, easyTargets[index]);
			assertScaledTargets(definition, TaskDifficulty.NORMAL, normalTargets[index]);
			assertScaledTargets(definition, TaskDifficulty.HARD, hardTargets[index]);
		}
	}

	private static TaskDefinition findDefinition(List<TaskDefinition> definitions, String id)
	{
		for (TaskDefinition definition : definitions)
		{
			if (definition.getId().equals(id))
			{
				return definition;
			}
		}
		throw new AssertionError("Missing task definition: " + id);
	}

	private static void assertScaledTargets(
		TaskDefinition definition,
		TaskDifficulty difficulty,
		int[] expected)
	{
		assertEquals(expected[0], difficulty.scaleTarget(definition.getMinimumTarget()));
		assertEquals(expected[1], difficulty.scaleTarget(definition.getMaximumTarget()));
	}
}
