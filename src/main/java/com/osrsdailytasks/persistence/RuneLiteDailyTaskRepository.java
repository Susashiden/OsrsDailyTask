package com.osrsdailytasks.persistence;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.model.ActiveTask;
import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
public class RuneLiteDailyTaskRepository implements DailyTaskRepository
{
	private static final String WRITING_SCHEMA_VERSION = "0";

	private final ConfigManager configManager;
	private final DailyTaskStateMapper stateMapper;

	@Inject
	public RuneLiteDailyTaskRepository(ConfigManager configManager, DailyTaskStateMapper stateMapper)
	{
		this.configManager = configManager;
		this.stateMapper = stateMapper;
	}

	@Override
	public Optional<ActiveTask> load()
	{
		Map<String, String> state = new LinkedHashMap<>();
		for (String key : stateMapper.getAllKeys())
		{
			String value = configManager.getRSProfileConfiguration(OsrsDailyTasksConfig.GROUP, key);
			if (value != null)
			{
				state.put(key, value);
			}
		}

		Optional<ActiveTask> activeTask = stateMapper.fromMap(state);
		if (!state.isEmpty() && !activeTask.isPresent())
		{
			log.debug("Ignoring incomplete or unsupported daily task state");
		}
		return activeTask;
	}

	@Override
	public void save(ActiveTask activeTask)
	{
		Map<String, String> state = stateMapper.toMap(activeTask);

		// Mark the state incomplete so a partially interrupted write cannot be loaded.
		configManager.setRSProfileConfiguration(
			OsrsDailyTasksConfig.GROUP,
			DailyTaskStateMapper.KEY_SCHEMA_VERSION,
			WRITING_SCHEMA_VERSION);

		for (String key : stateMapper.getAllKeys())
		{
			if (DailyTaskStateMapper.KEY_SCHEMA_VERSION.equals(key))
			{
				continue;
			}

			String value = state.get(key);
			if (value == null)
			{
				configManager.unsetRSProfileConfiguration(OsrsDailyTasksConfig.GROUP, key);
			}
			else
			{
				configManager.setRSProfileConfiguration(OsrsDailyTasksConfig.GROUP, key, value);
			}
		}

		configManager.setRSProfileConfiguration(
			OsrsDailyTasksConfig.GROUP,
			DailyTaskStateMapper.KEY_SCHEMA_VERSION,
			state.get(DailyTaskStateMapper.KEY_SCHEMA_VERSION));
	}

	@Override
	public void clear()
	{
		for (String key : stateMapper.getAllKeys())
		{
			configManager.unsetRSProfileConfiguration(OsrsDailyTasksConfig.GROUP, key);
		}
	}
}
