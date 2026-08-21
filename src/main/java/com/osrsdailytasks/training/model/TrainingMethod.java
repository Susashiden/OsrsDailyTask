package com.osrsdailytasks.training.model;

import java.util.Objects;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.requirement.MethodRequirementNode;

public final class TrainingMethod
{
	private final String methodId;
	private final EhpProfile ehpProfile;
	private final String skillId;
	private final long startXp;
	private final long endXp;
	private final String trainingMethod;
	private final double ehpRate;
	private final double actualSkillRate;
	private final TrainingMethodSource source;
	private final MethodRequirementNode requirements;

	public TrainingMethod(
		String methodId,
		EhpProfile ehpProfile,
		String skillId,
		long startXp,
		long endXp,
		String trainingMethod,
		double ehpRate,
		double actualSkillRate,
		TrainingMethodSource source,
		MethodRequirementNode requirements)
	{
		this.methodId = requireText(methodId, "methodId");
		this.ehpProfile = Objects.requireNonNull(ehpProfile, "ehpProfile");
		this.skillId = requireText(skillId, "skillId");
		if (startXp < 0 || endXp < startXp)
		{
			throw new IllegalArgumentException("XP band is invalid");
		}
		if (!Double.isFinite(ehpRate) || ehpRate < 0
			|| !Double.isFinite(actualSkillRate) || actualSkillRate < 0)
		{
			throw new IllegalArgumentException("rates must be finite and non-negative");
		}
		this.startXp = startXp;
		this.endXp = endXp;
		this.trainingMethod = requireText(trainingMethod, "trainingMethod");
		this.ehpRate = ehpRate;
		this.actualSkillRate = actualSkillRate;
		this.source = Objects.requireNonNull(source, "source");
		this.requirements = Objects.requireNonNull(requirements, "requirements");
	}

	public String getMethodId()
	{
		return methodId;
	}

	public EhpProfile getEhpProfile()
	{
		return ehpProfile;
	}

	public String getSkillId()
	{
		return skillId;
	}

	public long getStartXp()
	{
		return startXp;
	}

	public long getEndXp()
	{
		return endXp;
	}

	public String getTrainingMethod()
	{
		return trainingMethod;
	}

	public double getEhpRate()
	{
		return ehpRate;
	}

	public double getActualSkillRate()
	{
		return actualSkillRate;
	}

	public TrainingMethodSource getSource()
	{
		return source;
	}

	public MethodRequirementNode getRequirements()
	{
		return requirements;
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
