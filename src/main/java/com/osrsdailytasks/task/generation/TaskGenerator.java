package com.osrsdailytasks.task.generation;

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
import com.osrsdailytasks.boss.target.BossTargetRange;
import com.osrsdailytasks.boss.target.BossTargetService;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import com.osrsdailytasks.task.catalog.TaskCatalog;
import com.osrsdailytasks.training.target.XpTargetCalculator;
import com.osrsdailytasks.training.target.XpTargetRange;

@Singleton
public class TaskGenerator
{
	private final List<TaskDefinition> definitions;
	private final Random random;
	private final Supplier<TaskDifficulty> difficultySupplier;
	private final XpTargetCalculator xpTargetCalculator;
	private final BossTargetService bossTargetService;

	@Inject
	public TaskGenerator(
		TaskCatalog catalog,
		Random random,
		OsrsDailyTasksConfig config,
		XpTargetCalculator xpTargetCalculator,
		BossTargetService bossTargetService)
	{
		this(
			catalog.getDefinitions(),
			random,
			config::difficulty,
			xpTargetCalculator,
			bossTargetService);
	}

	public TaskGenerator(List<TaskDefinition> definitions, Random random)
	{
		this(definitions, random, () -> TaskDifficulty.NORMAL, null, null);
	}

	public TaskGenerator(List<TaskDefinition> definitions, Random random, TaskDifficulty difficulty)
	{
		this(definitions, random, () -> difficulty, null, null);
	}

	TaskGenerator(
		List<TaskDefinition> definitions,
		Random random,
		TaskDifficulty difficulty,
		XpTargetCalculator xpTargetCalculator)
	{
		this(definitions, random, () -> difficulty, xpTargetCalculator, null);
	}

	TaskGenerator(
		List<TaskDefinition> definitions,
		Random random,
		TaskDifficulty difficulty,
		XpTargetCalculator xpTargetCalculator,
		BossTargetService bossTargetService)
	{
		this(
			definitions,
			random,
			() -> difficulty,
			xpTargetCalculator,
			bossTargetService);
	}

	private TaskGenerator(
		List<TaskDefinition> definitions,
		Random random,
		Supplier<TaskDifficulty> difficultySupplier,
		XpTargetCalculator xpTargetCalculator,
		BossTargetService bossTargetService)
	{
		this.definitions = new ArrayList<>(Objects.requireNonNull(definitions, "definitions"));
		this.random = Objects.requireNonNull(random, "random");
		this.difficultySupplier = Objects.requireNonNull(difficultySupplier, "difficultySupplier");
		this.xpTargetCalculator = xpTargetCalculator;
		this.bossTargetService = bossTargetService;
	}

	public ActiveTask generate(LocalDate generationDate, Predicate<TaskDefinition> eligibility)
	{
		Objects.requireNonNull(generationDate, "generationDate");
		Objects.requireNonNull(eligibility, "eligibility");
		TaskDifficulty difficulty = getConfiguredDifficulty();
		boolean xpTargetsAvailable = xpTargetCalculator == null
			|| xpTargetCalculator.isCurrentProfileSupported();
		boolean bossTargetsAvailable = bossTargetService == null
			|| bossTargetService.isCurrentProfileSupported();

		Map<TaskType, List<TaskDefinition>> eligibleByType = new EnumMap<>(TaskType.class);
		for (TaskDefinition definition : definitions)
		{
			if (difficulty.isAtLeast(definition.getMinimumDifficulty())
				&& (definition.getType() != TaskType.XP || xpTargetsAvailable)
				&& (definition.getType() != TaskType.BOSS || bossTargetsAvailable)
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
		boolean xpTargetsAvailable = xpTargetCalculator == null
			|| xpTargetCalculator.isCurrentProfileSupported();
		boolean bossTargetsAvailable = bossTargetService == null
			|| bossTargetService.isCurrentProfileSupported();

		for (TaskDefinition definition : definitions)
		{
			if (!definition.getId().equals(taskId))
			{
				continue;
			}
			if (!difficulty.isAtLeast(definition.getMinimumDifficulty())
				|| (definition.getType() == TaskType.XP && !xpTargetsAvailable)
				|| (definition.getType() == TaskType.BOSS && !bossTargetsAvailable)
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
		int minimumTarget;
		int maximumTarget;
		if (selected.getType() == TaskType.XP && xpTargetCalculator != null)
		{
			XpTargetRange range = xpTargetCalculator.calculateCurrent(
				selected.getSubjectId(),
				targetDifficulty);
			minimumTarget = range.getMinimum();
			maximumTarget = range.getMaximum();
		}
		else if (selected.getType() == TaskType.BOSS && bossTargetService != null)
		{
			BossTargetRange range = bossTargetService.calculateCurrent(
				selected.getSubjectId(),
				targetDifficulty);
			int targetAmount = range.select(random);
			return ActiveTask.create(generationDate, selected, targetAmount);
		}
		else
		{
			minimumTarget = targetDifficulty.scaleTarget(selected.getMinimumTarget());
			maximumTarget = targetDifficulty.scaleTarget(selected.getMaximumTarget());
		}
		int targetRange = maximumTarget - minimumTarget + 1;
		int targetAmount = minimumTarget + random.nextInt(targetRange);
		return ActiveTask.create(generationDate, selected, targetAmount);
	}
}
