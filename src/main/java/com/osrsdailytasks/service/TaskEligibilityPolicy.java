package com.osrsdailytasks.service;

import com.osrsdailytasks.model.TaskDefinition;

public interface TaskEligibilityPolicy
{
	boolean isEligible(TaskDefinition definition);
}
