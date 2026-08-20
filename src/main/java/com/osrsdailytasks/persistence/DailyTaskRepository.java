package com.osrsdailytasks.persistence;

import java.util.Optional;
import com.osrsdailytasks.model.ActiveTask;

public interface DailyTaskRepository
{
	Optional<ActiveTask> load();

	void save(ActiveTask activeTask);

	void clear();
}
