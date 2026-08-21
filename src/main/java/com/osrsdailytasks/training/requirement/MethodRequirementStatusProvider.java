package com.osrsdailytasks.training.requirement;

public interface MethodRequirementStatusProvider
{
	MethodEligibility evaluate(MethodRequirement requirement);
}
