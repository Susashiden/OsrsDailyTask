package com.osrsdailytasks.ui;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskType;

@Singleton
public class TaskUiModelFactory
{
	private final Clock clock;
	private final OsrsDailyTasksConfig config;

	@Inject
	public TaskUiModelFactory(Clock clock, OsrsDailyTasksConfig config)
	{
		this.clock = Objects.requireNonNull(clock, "clock");
		this.config = Objects.requireNonNull(config, "config");
	}

	public TaskUiModel create(ActiveTask task)
	{
		Objects.requireNonNull(task, "task");
		int displayProgress = clampProgress(task.getProgress(), task.getTargetAmount());
		ZonedDateTime completedAt = task.getCompletedAt() == null
			? null
			: task.getCompletedAt().atZone(clock.getZone());

		return new TaskUiModel(
			task.getTitle(),
			categoryLabel(task.getTaskType()),
			task.getTargetAmount(),
			displayProgress,
			calculateProgressPercentage(displayProgress, task.getTargetAmount()),
			config.difficulty(),
			task.getGenerationDate(),
			task.isComplete(),
			completedAt,
			task.getGenerationDate().plusDays(1).atStartOfDay(clock.getZone()));
	}

	static int clampProgress(int progress, int targetAmount)
	{
		if (targetAmount <= 0)
		{
			return 0;
		}
		return Math.min(targetAmount, Math.max(0, progress));
	}

	static int calculateProgressPercentage(int progress, int targetAmount)
	{
		if (targetAmount <= 0)
		{
			return 0;
		}
		return (int) ((long) clampProgress(progress, targetAmount) * 100L / targetAmount);
	}

	private static String categoryLabel(TaskType taskType)
	{
		switch (taskType)
		{
			case XP:
				return "XP";
			case BOSS:
				return "Boss";
			case ACTIVITY:
				return "Activity";
			default:
				throw new IllegalArgumentException("Unsupported task type: " + taskType);
		}
	}
}
