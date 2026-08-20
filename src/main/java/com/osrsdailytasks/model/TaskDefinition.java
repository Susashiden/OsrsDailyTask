package com.osrsdailytasks.model;

import java.util.Objects;
import com.osrsdailytasks.TaskDifficulty;

public final class TaskDefinition
{
	private final String id;
	private final TaskType type;
	private final String subjectId;
	private final String title;
	private final double minimumTarget;
	private final double maximumTarget;
	private final TaskDifficulty minimumDifficulty;
	private final boolean scaleTargetsWithDifficulty;

	public TaskDefinition(
		String id,
		TaskType type,
		String subjectId,
		String title,
		double minimumTarget,
		double maximumTarget)
	{
		this(
			id,
			type,
			subjectId,
			title,
			minimumTarget,
			maximumTarget,
			TaskDifficulty.EASY,
			true);
	}

	public TaskDefinition(
		String id,
		TaskType type,
		String subjectId,
		String title,
		double minimumTarget,
		double maximumTarget,
		TaskDifficulty minimumDifficulty,
		boolean scaleTargetsWithDifficulty)
	{
		this.id = requireText(id, "id");
		this.type = Objects.requireNonNull(type, "type");
		this.subjectId = requireText(subjectId, "subjectId");
		this.title = requireText(title, "title");

		if (!Double.isFinite(minimumTarget) || minimumTarget <= 0)
		{
			throw new IllegalArgumentException("minimumTarget must be finite and positive");
		}

		if (!Double.isFinite(maximumTarget) || maximumTarget < minimumTarget)
		{
			throw new IllegalArgumentException("maximumTarget must not be less than minimumTarget");
		}

		this.minimumTarget = minimumTarget;
		this.maximumTarget = maximumTarget;
		this.minimumDifficulty = Objects.requireNonNull(minimumDifficulty, "minimumDifficulty");
		this.scaleTargetsWithDifficulty = scaleTargetsWithDifficulty;
	}

	public String getId()
	{
		return id;
	}

	public TaskType getType()
	{
		return type;
	}

	public String getSubjectId()
	{
		return subjectId;
	}

	public String getTitle()
	{
		return title;
	}

	public double getMinimumTarget()
	{
		return minimumTarget;
	}

	public double getMaximumTarget()
	{
		return maximumTarget;
	}

	public TaskDifficulty getMinimumDifficulty()
	{
		return minimumDifficulty;
	}

	public boolean isScaleTargetsWithDifficulty()
	{
		return scaleTargetsWithDifficulty;
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
