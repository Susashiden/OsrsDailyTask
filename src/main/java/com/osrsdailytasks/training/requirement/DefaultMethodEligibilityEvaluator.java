package com.osrsdailytasks.training.requirement;

import java.util.Objects;

public final class DefaultMethodEligibilityEvaluator implements MethodEligibilityEvaluator
{
	@Override
	public MethodEligibility evaluate(
		MethodRequirementNode requirements,
		MethodRequirementStatusProvider statusProvider)
	{
		Objects.requireNonNull(requirements, "requirements");
		Objects.requireNonNull(statusProvider, "statusProvider");
		if (requirements instanceof MethodRequirement)
		{
			MethodRequirement requirement = (MethodRequirement) requirements;
			if (requirement.getClassification() == MethodRequirementClassification.RECOMMENDED
				|| requirement.getClassification() == MethodRequirementClassification.SUPPLY)
			{
				return MethodEligibility.AVAILABLE;
			}
			return Objects.requireNonNull(statusProvider.evaluate(requirement), "requirement status");
		}

		MethodRequirementGroup group = (MethodRequirementGroup) requirements;
		return group.getMatchMode() == MethodRequirementGroup.MatchMode.ALL_OF
			? evaluateAll(group, statusProvider)
			: evaluateAny(group, statusProvider);
	}

	private MethodEligibility evaluateAll(
		MethodRequirementGroup group,
		MethodRequirementStatusProvider statusProvider)
	{
		boolean unknown = false;
		for (MethodRequirementNode child : group.getChildren())
		{
			MethodEligibility result = evaluate(child, statusProvider);
			if (result == MethodEligibility.LOCKED)
			{
				return MethodEligibility.LOCKED;
			}
			unknown |= result == MethodEligibility.UNKNOWN;
		}
		return unknown ? MethodEligibility.UNKNOWN : MethodEligibility.AVAILABLE;
	}

	private MethodEligibility evaluateAny(
		MethodRequirementGroup group,
		MethodRequirementStatusProvider statusProvider)
	{
		boolean unknown = false;
		for (MethodRequirementNode child : group.getChildren())
		{
			MethodEligibility result = evaluate(child, statusProvider);
			if (result == MethodEligibility.AVAILABLE)
			{
				return MethodEligibility.AVAILABLE;
			}
			unknown |= result == MethodEligibility.UNKNOWN;
		}
		return unknown ? MethodEligibility.UNKNOWN : MethodEligibility.LOCKED;
	}
}
