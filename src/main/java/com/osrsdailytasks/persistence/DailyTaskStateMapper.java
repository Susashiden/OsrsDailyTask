package com.osrsdailytasks.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Singleton;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskType;

@Singleton
public class DailyTaskStateMapper
{
	public static final int CURRENT_SCHEMA_VERSION = 2;

	public static final String KEY_SCHEMA_VERSION = "state.schemaVersion";
	public static final String KEY_GENERATION_DATE = "state.generationDate";
	public static final String KEY_TASK_ID = "state.taskId";
	public static final String KEY_TASK_TYPE = "state.taskType";
	public static final String KEY_SUBJECT_ID = "state.subjectId";
	public static final String KEY_TITLE = "state.title";
	public static final String KEY_TARGET_AMOUNT = "state.targetAmount";
	public static final String KEY_PROGRESS = "state.progress";
	public static final String KEY_STARTING_XP = "state.startingXp";
	public static final String KEY_COMPLETED_AT = "state.completedAt";
	public static final String KEY_REROLLS_USED = "state.rerollsUsed";

	private static final List<String> ALL_KEYS = Collections.unmodifiableList(Arrays.asList(
		KEY_SCHEMA_VERSION,
		KEY_GENERATION_DATE,
		KEY_TASK_ID,
		KEY_TASK_TYPE,
		KEY_SUBJECT_ID,
		KEY_TITLE,
		KEY_TARGET_AMOUNT,
		KEY_PROGRESS,
		KEY_STARTING_XP,
		KEY_COMPLETED_AT,
		KEY_REROLLS_USED));

	public Map<String, String> toMap(ActiveTask activeTask)
	{
		Map<String, String> state = new LinkedHashMap<>();
		state.put(KEY_SCHEMA_VERSION, Integer.toString(CURRENT_SCHEMA_VERSION));
		state.put(KEY_GENERATION_DATE, activeTask.getGenerationDate().toString());
		state.put(KEY_TASK_ID, activeTask.getTaskId());
		state.put(KEY_TASK_TYPE, activeTask.getTaskType().name());
		state.put(KEY_SUBJECT_ID, activeTask.getSubjectId());
		state.put(KEY_TITLE, activeTask.getTitle());
		state.put(KEY_TARGET_AMOUNT, Integer.toString(activeTask.getTargetAmount()));
		state.put(KEY_PROGRESS, Integer.toString(activeTask.getProgress()));
		state.put(KEY_REROLLS_USED, Integer.toString(activeTask.getRerollsUsed()));

		if (activeTask.getStartingXp() != null)
		{
			state.put(KEY_STARTING_XP, Integer.toString(activeTask.getStartingXp()));
		}
		if (activeTask.getCompletedAt() != null)
		{
			state.put(KEY_COMPLETED_AT, activeTask.getCompletedAt().toString());
		}

		return state;
	}

	public Optional<ActiveTask> fromMap(Map<String, String> state)
	{
		try
		{
			int schemaVersion = Integer.parseInt(required(state, KEY_SCHEMA_VERSION));
			if (schemaVersion != CURRENT_SCHEMA_VERSION)
			{
				return Optional.empty();
			}

			String startingXpValue = state.get(KEY_STARTING_XP);
			Integer startingXp = startingXpValue == null ? null : Integer.valueOf(startingXpValue);
			String completedAtValue = state.get(KEY_COMPLETED_AT);
			Instant completedAt = completedAtValue == null ? null : Instant.parse(completedAtValue);

			return Optional.of(new ActiveTask(
				LocalDate.parse(required(state, KEY_GENERATION_DATE)),
				required(state, KEY_TASK_ID),
				TaskType.valueOf(required(state, KEY_TASK_TYPE)),
				required(state, KEY_SUBJECT_ID),
				required(state, KEY_TITLE),
				Integer.parseInt(required(state, KEY_TARGET_AMOUNT)),
				Integer.parseInt(required(state, KEY_PROGRESS)),
				startingXp,
				completedAt,
				Integer.parseInt(required(state, KEY_REROLLS_USED))));
		}
		catch (RuntimeException exception)
		{
			return Optional.empty();
		}
	}

	public List<String> getAllKeys()
	{
		return ALL_KEYS;
	}

	private static String required(Map<String, String> state, String key)
	{
		String value = state.get(key);
		if (value == null || value.trim().isEmpty())
		{
			throw new IllegalArgumentException("Missing state value: " + key);
		}
		return value;
	}
}
