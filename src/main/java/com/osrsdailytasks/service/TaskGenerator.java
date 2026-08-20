package com.osrsdailytasks.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;

@Singleton
public class TaskGenerator
{
	private final List<TaskDefinition> definitions;
	private final Random random;
	private final Supplier<TaskDifficulty> difficultySupplier;

	@Inject
	public TaskGenerator(TaskCatalog catalog, Random random, OsrsDailyTasksConfig config)
	{
		this(catalog.getDefinitions(), random, config::difficulty);
	}

	TaskGenerator(List<TaskDefinition> definitions, Random random)
	{
		this(definitions, random, () -> TaskDifficulty.NORMAL);
	}

	TaskGenerator(List<TaskDefinition> definitions, Random random, TaskDifficulty difficulty)
	{
		this(definitions, random, () -> difficulty);
	}

	private TaskGenerator(
		List<TaskDefinition> definitions,
		Random random,
		Supplier<TaskDifficulty> difficultySupplier)
	{
		this.definitions = new ArrayList<>(Objects.requireNonNull(definitions, "definitions"));
		this.random = Objects.requireNonNull(random, "random");
		this.difficultySupplier = Objects.requireNonNull(difficultySupplier, "difficultySupplier");
	}

	public ActiveTask generate(LocalDate generationDate, Predicate<TaskDefinition> eligibility)
	{
		Objects.requireNonNull(generationDate, "generationDate");
		Objects.requireNonNull(eligibility, "eligibility");
		TaskDifficulty difficulty = getConfiguredDifficulty();

		Map<TaskType, List<TaskDefinition>> eligibleByType = new EnumMap<>(TaskType.class);
		for (TaskDefinition definition : definitions)
		{
			if (difficulty.isAtLeast(definition.getMinimumDifficulty())
				&& eligibility.test(definition))
			{
				eligibleByType.computeIfAbsent(definition.getType(), ignored -> new ArrayList<>())
					.add(definition);
			}
		}

		if (eligibleByType.isEmpty())
		{
			throw new IllegalStateException("No eligible daily task definitions are available");
		}

		List<TaskType> eligibleTypes = new ArrayList<>(eligibleByType.keySet());
		TaskType selectedType = eligibleTypes.get(random.nextInt(eligibleTypes.size()));
		List<TaskDefinition> eligibleDefinitions = eligibleByType.get(selectedType);
		TaskDefinition selected = eligibleDefinitions.get(random.nextInt(eligibleDefinitions.size()));
		return createTask(generationDate, selected, difficulty);
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE): deterministic task selection for manual testing.
	public ActiveTask generateSpecific(
		LocalDate generationDate,
		String taskId,
		Predicate<TaskDefinition> eligibility)
	{
		Objects.requireNonNull(generationDate, "generationDate");
		Objects.requireNonNull(taskId, "taskId");
		Objects.requireNonNull(eligibility, "eligibility");
		TaskDifficulty difficulty = getConfiguredDifficulty();

		for (TaskDefinition definition : definitions)
		{
			if (!definition.getId().equals(taskId))
			{
				continue;
			}
			if (!difficulty.isAtLeast(definition.getMinimumDifficulty())
				|| !eligibility.test(definition))
			{
				throw new IllegalArgumentException(
					"Selected task is not eligible at the current difficulty: " + taskId);
			}
			return createTask(generationDate, definition, difficulty);
		}

		throw new IllegalArgumentException("Unknown task definition: " + taskId);
	}

	private TaskDifficulty getConfiguredDifficulty()
	{
		return Objects.requireNonNull(difficultySupplier.get(), "configured difficulty");
	}

	private ActiveTask createTask(
		LocalDate generationDate,
		TaskDefinition selected,
		TaskDifficulty difficulty)
	{
		TaskDifficulty targetDifficulty = selected.isScaleTargetsWithDifficulty()
			? difficulty
			: TaskDifficulty.NORMAL;
		int minimumTarget = targetDifficulty.scaleTarget(selected.getMinimumTarget());
		int maximumTarget = targetDifficulty.scaleTarget(selected.getMaximumTarget());
		int targetRange = maximumTarget - minimumTarget + 1;
		int targetAmount = minimumTarget + random.nextInt(targetRange);
		return ActiveTask.create(generationDate, selected, targetAmount);
	}
}
