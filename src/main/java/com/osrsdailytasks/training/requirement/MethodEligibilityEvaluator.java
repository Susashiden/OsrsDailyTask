package com.osrsdailytasks.training.requirement;

public interface MethodEligibilityEvaluator
{
	MethodEligibility evaluate(MethodRequirementNode requirements, MethodRequirementStatusProvider statusProvider);
}
