package com.osrsdailytasks.service;

import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.tracking.ProgressUpdateResult;
import com.osrsdailytasks.tracking.TaskProgressService;
import com.osrsdailytasks.tracking.XpBaselineInitializer;

@Singleton
public class DailyTaskLoadCoordinator
{
	private final DailyTaskService dailyTaskService;
	private final TaskProgressService taskProgressService;
	private final Function<ActiveTask, ProgressUpdateResult> loadedClientXpInitializer;

	@Inject
	public DailyTaskLoadCoordinator(
		DailyTaskService dailyTaskService,
		TaskProgressService taskProgressService,
		XpBaselineInitializer xpBaselineInitializer)
	{
		this(
			dailyTaskService,
			taskProgressService,
			xpBaselineInitializer::initializeFromLoadedClient);
	}

	DailyTaskLoadCoordinator(
		DailyTaskService dailyTaskService,
		TaskProgressService taskProgressService,
		Function<ActiveTask, ProgressUpdateResult> loadedClientXpInitializer)
	{
		this.dailyTaskService = dailyTaskService;
		this.taskProgressService = taskProgressService;
		this.loadedClientXpInitializer = loadedClientXpInitializer;
	}

	public ProgressUpdateResult load(boolean clientSkillDataAlreadyLoaded)
	{
		taskProgressService.resetTransientState();
		ActiveTask activeTask = dailyTaskService.loadOrGenerate();
		return clientSkillDataAlreadyLoaded
			? loadedClientXpInitializer.apply(activeTask)
			: ProgressUpdateResult.IGNORED;
	}
}
