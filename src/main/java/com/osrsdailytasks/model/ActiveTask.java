package com.osrsdailytasks.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public final class ActiveTask
{
	private final LocalDate generationDate;
	private final String taskId;
	private final TaskType taskType;
	private final String subjectId;
	private final String title;
	private final int targetAmount;
	private final int progress;
	private final Integer startingXp;
	private final Instant completedAt;
	private final int rerollsUsed;

	public ActiveTask(
		LocalDate generationDate,
		String taskId,
		TaskType taskType,
		String subjectId,
		String title,
		int targetAmount,
		int progress,
		Integer startingXp,
		Instant completedAt,
		int rerollsUsed)
	{
		this.generationDate = Objects.requireNonNull(generationDate, "generationDate");
		this.taskId = requireText(taskId, "taskId");
		this.taskType = Objects.requireNonNull(taskType, "taskType");
		this.subjectId = requireText(subjectId, "subjectId");
		this.title = requireText(title, "title");

		if (targetAmount <= 0)
		{
			throw new IllegalArgumentException("targetAmount must be positive");
		}
		if (progress < 0 || progress > targetAmount)
		{
			throw new IllegalArgumentException("progress must be between zero and targetAmount");
		}
		if (startingXp != null && startingXp < 0)
		{
			throw new IllegalArgumentException("startingXp must not be negative");
		}
		if (rerollsUsed < 0)
		{
			throw new IllegalArgumentException("rerollsUsed must not be negative");
		}
		if ((completedAt != null) != (progress == targetAmount))
		{
			throw new IllegalArgumentException("completedAt must be set exactly when the target is reached");
		}

		this.targetAmount = targetAmount;
		this.progress = progress;
		this.startingXp = startingXp;
		this.completedAt = completedAt;
		this.rerollsUsed = rerollsUsed;
	}

	public static ActiveTask create(LocalDate generationDate, TaskDefinition definition, int targetAmount)
	{
		return new ActiveTask(
			generationDate,
			definition.getId(),
			definition.getType(),
			definition.getSubjectId(),
			definition.getTitle(),
			targetAmount,
			0,
			null,
			null,
			0);
	}

	public LocalDate getGenerationDate()
	{
		return generationDate;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public TaskType getTaskType()
	{
		return taskType;
	}

	public String getSubjectId()
	{
		return subjectId;
	}

	public String getTitle()
	{
		return title;
	}

	public int getTargetAmount()
	{
		return targetAmount;
	}

	public int getProgress()
	{
		return progress;
	}

	public Integer getStartingXp()
	{
		return startingXp;
	}

	public Instant getCompletedAt()
	{
		return completedAt;
	}

	public int getRerollsUsed()
	{
		return rerollsUsed;
	}

	public boolean isComplete()
	{
		return completedAt != null;
	}

	public ActiveTask withStartingXp(int newStartingXp)
	{
		return new ActiveTask(
			generationDate,
			taskId,
			taskType,
			subjectId,
			title,
			targetAmount,
			progress,
			newStartingXp,
			completedAt,
			rerollsUsed);
	}

	public ActiveTask withProgress(int newProgress, Instant newCompletedAt)
	{
		return new ActiveTask(
			generationDate,
			taskId,
			taskType,
			subjectId,
			title,
			targetAmount,
			newProgress,
			startingXp,
			newCompletedAt,
			rerollsUsed);
	}

	@Override
	public boolean equals(Object object)
	{
		if (this == object)
		{
			return true;
		}
		if (!(object instanceof ActiveTask))
		{
			return false;
		}
		ActiveTask that = (ActiveTask) object;
		return targetAmount == that.targetAmount
			&& progress == that.progress
			&& rerollsUsed == that.rerollsUsed
			&& generationDate.equals(that.generationDate)
			&& taskId.equals(that.taskId)
			&& taskType == that.taskType
			&& subjectId.equals(that.subjectId)
			&& title.equals(that.title)
			&& Objects.equals(startingXp, that.startingXp)
			&& Objects.equals(completedAt, that.completedAt);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(
			generationDate,
			taskId,
			taskType,
			subjectId,
			title,
			targetAmount,
			progress,
			startingXp,
			completedAt,
			rerollsUsed);
	}

	private static String requireText(String value, String name)
	{
		Objects.requireNonNull(value, name);
		String trimmed = value.trim();
		if (trimmed.isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return trimmed;
	}
}
