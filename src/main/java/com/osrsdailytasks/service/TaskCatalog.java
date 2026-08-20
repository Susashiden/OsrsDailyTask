package com.osrsdailytasks.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.inject.Singleton;
import javax.inject.Inject;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.model.ClueTier;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import net.runelite.api.Skill;

@Singleton
public class TaskCatalog
{
	private final List<TaskDefinition> definitions;

	@Inject
	public TaskCatalog(HiscoreBossCatalog bossCatalog)
	{
		this(bossCatalog.getDefinitions());
	}

	public TaskCatalog()
	{
		this(new HiscoreBossCatalog().getDefinitions());
	}

	private TaskCatalog(List<BossDefinition> bosses)
	{
		List<TaskDefinition> taskDefinitions = new ArrayList<>();
		for (Skill skill : Skill.values())
		{
			if ("OVERALL".equals(skill.name()))
			{
				continue;
			}

			taskDefinitions.add(new TaskDefinition(
				"xp-" + skill.name().toLowerCase(Locale.ENGLISH),
				TaskType.XP,
				skill.name(),
				"Gain " + skill.getName() + " XP",
				5_000,
				20_000));
		}

		for (BossDefinition boss : bosses)
		{
			if (!boss.isClassified())
			{
				continue;
			}
			taskDefinitions.add(new TaskDefinition(
				boss.getTaskId(),
				TaskType.BOSS,
				boss.getSubjectId(),
				boss.getTitle(),
				boss.getPvmDifficulty().getMinimumTarget(),
				boss.getPvmDifficulty().getMaximumTarget()));
		}
		taskDefinitions.add(new TaskDefinition(
			"activity-agility-course-laps",
			TaskType.ACTIVITY,
			"AGILITY_COURSE_LAPS",
			"Complete Agility course laps",
			5,
			15));
		taskDefinitions.add(new TaskDefinition(
			"activity-brimhaven-arena-tags",
			TaskType.ACTIVITY,
			"BRIMHAVEN_ARENA_TAGS",
			"Tag Brimhaven Agility Arena pillars",
			5,
			15));
		for (ClueTier tier : ClueTier.values())
		{
			boolean highTier = tier == ClueTier.ELITE || tier == ClueTier.MASTER;
			taskDefinitions.add(new TaskDefinition(
				"activity-clue-scroll-" + tier.getMessageName(),
				TaskType.ACTIVITY,
				tier.getSubjectId(),
				"Complete " + tier.getMessageName() + " clue scrolls",
				tier.getMinimumTarget(),
				tier.getMaximumTarget(),
				highTier ? TaskDifficulty.HARD : TaskDifficulty.EASY,
				true));
		}

		definitions = Collections.unmodifiableList(taskDefinitions);
	}

	public List<TaskDefinition> getDefinitions()
	{
		return definitions;
	}
}
