package com.osrsdailytasks.training.model;

import java.util.Objects;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.requirement.MethodRequirementNode;

public final class TrainingMethodDefinition
{
	private final String methodId;
	private final EhpProfile profile;
	private final String skillId;
	private final long startXp;
	private final String description;
	private final double ehpRate;
	private final double actualSkillRate;
	private final TrainingMethodSource source;
	private final MethodRequirementNode extraRequirements;

	public TrainingMethodDefinition(
		String methodId,
		EhpProfile profile,
		String skillId,
		long startXp,
		String description,
		double ehpRate,
		double actualSkillRate,
		TrainingMethodSource source,
		MethodRequirementNode extraRequirements)
	{
		this.methodId = Objects.requireNonNull(methodId, "methodId");
		this.profile = Objects.requireNonNull(profile, "profile");
		this.skillId = Objects.requireNonNull(skillId, "skillId");
		this.startXp = startXp;
		this.description = Objects.requireNonNull(description, "description");
		this.ehpRate = ehpRate;
		this.actualSkillRate = actualSkillRate;
		this.source = Objects.requireNonNull(source, "source");
		this.extraRequirements = extraRequirements;
	}

	public String getMethodId()
	{
		return methodId;
	}

	public EhpProfile getProfile()
	{
		return profile;
	}

	public String getSkillId()
	{
		return skillId;
	}

	public long getStartXp()
	{
		return startXp;
	}

	public String getDescription()
	{
		return description;
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

	public MethodRequirementNode getExtraRequirements()
	{
		return extraRequirements;
	}
}
