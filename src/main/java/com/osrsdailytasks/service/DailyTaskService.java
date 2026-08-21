package com.osrsdailytasks.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.persistence.DailyTaskRepository;
import com.osrsdailytasks.persistence.RuneLiteDailyTaskRepository;
import com.osrsdailytasks.task.eligibility.DefaultTaskEligibilityPolicy;
import com.osrsdailytasks.task.eligibility.TaskEligibilityPolicy;
import com.osrsdailytasks.task.generation.TaskGenerator;

@Singleton
public class DailyTaskService
{
	private final DailyTaskRepository repository;
	private final TaskGenerator generator;
	private final TaskEligibilityPolicy eligibilityPolicy;
	private final Clock clock;

	private ActiveTask activeTask;

	@Inject
	public DailyTaskService(
		RuneLiteDailyTaskRepository repository,
		TaskGenerator generator,
		DefaultTaskEligibilityPolicy eligibilityPolicy,
		Clock clock)
	{
		this((DailyTaskRepository) repository, generator, eligibilityPolicy, clock);
	}

	public DailyTaskService(
		DailyTaskRepository repository,
		TaskGenerator generator,
		TaskEligibilityPolicy eligibilityPolicy,
		Clock clock)
	{
		this.repository = Objects.requireNonNull(repository, "repository");
		this.generator = Objects.requireNonNull(generator, "generator");
		this.eligibilityPolicy = Objects.requireNonNull(eligibilityPolicy, "eligibilityPolicy");
		this.clock = Objects.requireNonNull(clock, "clock");
	}

	public ActiveTask loadOrGenerate()
	{
		LocalDate today = LocalDate.now(clock);
		Optional<ActiveTask> storedTask = repository.load();

		if (storedTask.isPresent() && !storedTask.get().getGenerationDate().isBefore(today))
		{
			activeTask = storedTask.get();
			return activeTask;
		}

		activeTask = generator.generate(today, eligibilityPolicy::isEligible);
		repository.save(activeTask);
		return activeTask;
	}

	public Optional<ActiveTask> getActiveTask()
	{
		return Optional.ofNullable(activeTask);
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE): replaces today's task for manual testing.
	public ActiveTask assignForDevelopment(String taskId)
	{
		activeTask = generator.generateSpecific(
			LocalDate.now(clock),
			taskId,
			eligibilityPolicy::isEligible);
		repository.save(activeTask);
		return activeTask;
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE): clears today's task for manual testing.
	public void cancelForDevelopment()
	{
		repository.clear();
		activeTask = null;
	}

	public void saveProgress(ActiveTask updatedTask)
	{
		updateProgress(updatedTask, true);
	}

	public void updateCachedProgress(ActiveTask updatedTask)
	{
		updateProgress(updatedTask, false);
	}

	private void updateProgress(ActiveTask updatedTask, boolean persist)
	{
		Objects.requireNonNull(updatedTask, "updatedTask");
		if (activeTask == null
			|| !activeTask.getGenerationDate().equals(updatedTask.getGenerationDate())
			|| !activeTask.getTaskId().equals(updatedTask.getTaskId()))
		{
			throw new IllegalArgumentException("Progress can only update the current daily task");
		}

		activeTask = updatedTask;
		if (persist)
		{
			repository.save(activeTask);
		}
	}

	public void clearCachedTask()
	{
		activeTask = null;
	}
}
