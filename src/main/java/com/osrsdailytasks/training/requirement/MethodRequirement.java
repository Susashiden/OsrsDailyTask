package com.osrsdailytasks.training.requirement;

import java.util.Objects;

public final class MethodRequirement implements MethodRequirementNode
{
	private final MethodRequirementType type;
	private final String subjectId;
	private final MethodRequirementComparison comparison;
	private final int requiredValue;
	private final MethodRequirementClassification classification;

	public MethodRequirement(
		MethodRequirementType type,
		String subjectId,
		MethodRequirementComparison comparison,
		int requiredValue,
		MethodRequirementClassification classification)
	{
		this.type = Objects.requireNonNull(type, "type");
		this.subjectId = requireText(subjectId, "subjectId");
		this.comparison = Objects.requireNonNull(comparison, "comparison");
		if (requiredValue < 0)
		{
			throw new IllegalArgumentException("requiredValue must not be negative");
		}
		this.requiredValue = requiredValue;
		this.classification = Objects.requireNonNull(classification, "classification");
	}

	public MethodRequirementType getType()
	{
		return type;
	}

	public String getSubjectId()
	{
		return subjectId;
	}

	public MethodRequirementComparison getComparison()
	{
		return comparison;
	}

	public int getRequiredValue()
	{
		return requiredValue;
	}

	public MethodRequirementClassification getClassification()
	{
		return classification;
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
