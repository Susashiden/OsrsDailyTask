package com.osrsdailytasks.ui;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;
import com.osrsdailytasks.TaskDifficulty;

public final class TaskUiModel
{
	private final String title;
	private final String category;
	private final int targetAmount;
	private final int progress;
	private final int progressPercentage;
	private final TaskDifficulty difficulty;
	private final LocalDate assignmentDate;
	private final boolean complete;
	private final ZonedDateTime completedAt;
	private final ZonedDateTime nextRolloverAt;

	TaskUiModel(
		String title,
		String category,
		int targetAmount,
		int progress,
		int progressPercentage,
		TaskDifficulty difficulty,
		LocalDate assignmentDate,
		boolean complete,
		ZonedDateTime completedAt,
		ZonedDateTime nextRolloverAt)
	{
		this.title = Objects.requireNonNull(title, "title");
		this.category = Objects.requireNonNull(category, "category");
		this.targetAmount = targetAmount;
		this.progress = progress;
		this.progressPercentage = progressPercentage;
		this.difficulty = Objects.requireNonNull(difficulty, "difficulty");
		this.assignmentDate = Objects.requireNonNull(assignmentDate, "assignmentDate");
		this.complete = complete;
		this.completedAt = completedAt;
		this.nextRolloverAt = Objects.requireNonNull(nextRolloverAt, "nextRolloverAt");
	}

	public String getTitle()
	{
		return title;
	}

	public String getCategory()
	{
		return category;
	}

	public int getTargetAmount()
	{
		return targetAmount;
	}

	public int getProgress()
	{
		return progress;
	}

	public int getProgressPercentage()
	{
		return progressPercentage;
	}

	public TaskDifficulty getDifficulty()
	{
		return difficulty;
	}

	public LocalDate getAssignmentDate()
	{
		return assignmentDate;
	}

	public boolean isComplete()
	{
		return complete;
	}

	public ZonedDateTime getCompletedAt()
	{
		return completedAt;
	}

	public ZonedDateTime getNextRolloverAt()
	{
		return nextRolloverAt;
	}

	public String getProgressText()
	{
		return progress + " / " + targetAmount;
	}
}
