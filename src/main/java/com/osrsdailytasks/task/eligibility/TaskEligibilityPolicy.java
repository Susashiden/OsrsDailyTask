package com.osrsdailytasks.task.eligibility;

import com.osrsdailytasks.model.TaskDefinition;

public interface TaskEligibilityPolicy
{
	boolean isEligible(TaskDefinition definition);
}
