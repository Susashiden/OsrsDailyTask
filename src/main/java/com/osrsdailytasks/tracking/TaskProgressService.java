package com.osrsdailytasks.tracking;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskType;
import com.osrsdailytasks.service.DailyTaskService;

@Singleton
public class TaskProgressService
{
	private final DailyTaskService dailyTaskService;
	private final Clock clock;

	private String lastDiscreteEventKey;

	@Inject
	public TaskProgressService(DailyTaskService dailyTaskService, Clock clock)
	{
		this.dailyTaskService = dailyTaskService;
		this.clock = clock;
	}

	public ProgressUpdateResult recordXp(String skillSubjectId, int totalXp)
	{
		if (totalXp < 0)
		{
			return ProgressUpdateResult.IGNORED;
		}

		Optional<ActiveTask> activeTask = dailyTaskService.getActiveTask();
		if (!activeTask.isPresent())
		{
			return ProgressUpdateResult.IGNORED;
		}

		ActiveTask task = activeTask.get();
		if (task.isComplete()
			|| task.getTaskType() != TaskType.XP
			|| !task.getSubjectId().equals(skillSubjectId))
		{
			return ProgressUpdateResult.IGNORED;
		}

		if (task.getStartingXp() == null)
		{
			dailyTaskService.saveProgress(task.withStartingXp(totalXp));
			return ProgressUpdateResult.UPDATED;
		}

		int earnedXp = Math.max(0, totalXp - task.getStartingXp());
		int newProgress = Math.min(task.getTargetAmount(), Math.max(task.getProgress(), earnedXp));
		return applyProgress(task, newProgress, false);
	}

	public ProgressUpdateResult recordDiscreteCompletion(
		TaskType taskType,
		String subjectId,
		long eventSequence)
	{
		Objects.requireNonNull(taskType, "taskType");
		Objects.requireNonNull(subjectId, "subjectId");

		Optional<ActiveTask> activeTask = dailyTaskService.getActiveTask();
		if (!activeTask.isPresent())
		{
			return ProgressUpdateResult.IGNORED;
		}

		ActiveTask task = activeTask.get();
		if (task.isComplete()
			|| task.getTaskType() != taskType
			|| !task.getSubjectId().equals(subjectId))
		{
			return ProgressUpdateResult.IGNORED;
		}

		String eventKey = taskType.name() + ':' + subjectId + ':' + eventSequence;
		if (eventKey.equals(lastDiscreteEventKey))
		{
			return ProgressUpdateResult.IGNORED;
		}
		lastDiscreteEventKey = eventKey;

		int newProgress = Math.min(task.getTargetAmount(), task.getProgress() + 1);
		return applyProgress(task, newProgress, true);
	}

	public void resetTransientState()
	{
		lastDiscreteEventKey = null;
	}

	private ProgressUpdateResult applyProgress(ActiveTask task, int newProgress, boolean persistIntermediateProgress)
	{
		if (newProgress == task.getProgress())
		{
			return ProgressUpdateResult.IGNORED;
		}

		Instant completedAt = newProgress >= task.getTargetAmount() ? Instant.now(clock) : null;
		ActiveTask updatedTask = task.withProgress(newProgress, completedAt);
		if (updatedTask.isComplete() || persistIntermediateProgress)
		{
			dailyTaskService.saveProgress(updatedTask);
		}
		else
		{
			dailyTaskService.updateCachedProgress(updatedTask);
		}
		return updatedTask.isComplete() ? ProgressUpdateResult.COMPLETED : ProgressUpdateResult.UPDATED;
	}
}
