package com.osrsdailytasks.ui;

import java.util.Objects;
import com.osrsdailytasks.model.TaskDefinition;

// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
final class DevelopmentTaskOption
{
	private final String taskId;
	private final String displayName;

	DevelopmentTaskOption(TaskDefinition definition)
	{
		Objects.requireNonNull(definition, "definition");
		this.taskId = definition.getId();
		this.displayName = definition.getTitle();
	}

	String getTaskId()
	{
		return taskId;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
