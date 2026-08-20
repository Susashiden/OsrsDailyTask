package com.osrsdailytasks.tracking;

import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskType;
import net.runelite.api.Client;
import net.runelite.api.Skill;

@Singleton
public class XpBaselineInitializer
{
	private final Client client;
	private final TaskProgressService taskProgressService;

	@Inject
	public XpBaselineInitializer(Client client, TaskProgressService taskProgressService)
	{
		this.client = client;
		this.taskProgressService = taskProgressService;
	}

	public ProgressUpdateResult initializeFromLoadedClient(ActiveTask activeTask)
	{
		if (activeTask.getTaskType() != TaskType.XP)
		{
			return ProgressUpdateResult.IGNORED;
		}

		try
		{
			Skill skill = Skill.valueOf(activeTask.getSubjectId());
			return taskProgressService.recordXp(
				activeTask.getSubjectId(),
				client.getSkillExperience(skill));
		}
		catch (IllegalArgumentException exception)
		{
			return ProgressUpdateResult.IGNORED;
		}
	}
}
