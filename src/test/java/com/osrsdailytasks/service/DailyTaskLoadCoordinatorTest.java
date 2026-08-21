package com.osrsdailytasks.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import com.osrsdailytasks.persistence.DailyTaskRepository;
import com.osrsdailytasks.task.generation.TaskGenerator;
import com.osrsdailytasks.tracking.ProgressUpdateResult;
import com.osrsdailytasks.tracking.TaskProgressService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DailyTaskLoadCoordinatorTest
{
	private static final Clock TEST_CLOCK = Clock.fixed(
		Instant.parse("2026-08-20T01:00:00Z"),
		ZoneOffset.UTC);

	@Test
	public void loginDefersBaselineUntilFirstReliableStatEvent()
	{
		Fixture fixture = fixture();
		AtomicInteger immediateInitializations = new AtomicInteger();
		DailyTaskLoadCoordinator coordinator = new DailyTaskLoadCoordinator(
			fixture.dailyTaskService,
			fixture.taskProgressService,
			task ->
			{
				immediateInitializations.incrementAndGet();
				return ProgressUpdateResult.UPDATED;
			});

		assertEquals(ProgressUpdateResult.IGNORED, coordinator.load(false));
		assertEquals(0, immediateInitializations.get());
		assertEquals(null, fixture.currentTask().getStartingXp());

		assertEquals(
			ProgressUpdateResult.UPDATED,
			fixture.taskProgressService.recordXp("AGILITY", 50_000));
		assertEquals(Integer.valueOf(50_000), fixture.currentTask().getStartingXp());
		assertEquals(0, fixture.currentTask().getProgress());

		assertEquals(
			ProgressUpdateResult.UPDATED,
			fixture.taskProgressService.recordXp("AGILITY", 50_250));
		assertEquals(250, fixture.currentTask().getProgress());

		fixture.dailyTaskService.clearCachedTask();
		assertEquals(ProgressUpdateResult.IGNORED, coordinator.load(false));
		assertEquals(Integer.valueOf(50_000), fixture.currentTask().getStartingXp());
		assertEquals(0, fixture.currentTask().getProgress());
	}

	@Test
	public void alreadyLoadedSessionInitializesImmediately()
	{
		Fixture fixture = fixture();
		AtomicInteger immediateInitializations = new AtomicInteger();
		DailyTaskLoadCoordinator coordinator = new DailyTaskLoadCoordinator(
			fixture.dailyTaskService,
			fixture.taskProgressService,
			task ->
			{
				immediateInitializations.incrementAndGet();
				return fixture.taskProgressService.recordXp(task.getSubjectId(), 75_000);
			});

		assertEquals(ProgressUpdateResult.UPDATED, coordinator.load(true));
		assertEquals(1, immediateInitializations.get());
		assertEquals(Integer.valueOf(75_000), fixture.currentTask().getStartingXp());
		assertEquals(0, fixture.currentTask().getProgress());
	}

	private static Fixture fixture()
	{
		InMemoryRepository repository = new InMemoryRepository();
		TaskDefinition definition = new TaskDefinition(
			"xp-agility",
			TaskType.XP,
			"AGILITY",
			"Gain Agility XP",
			1_000,
			1_000);
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(definition),
			new Random(1L));
		DailyTaskService service = new DailyTaskService(
			repository,
			generator,
			ignored -> true,
			TEST_CLOCK);
		return new Fixture(service, new TaskProgressService(service, TEST_CLOCK));
	}

	private static final class Fixture
	{
		private final DailyTaskService dailyTaskService;
		private final TaskProgressService taskProgressService;

		private Fixture(
			DailyTaskService dailyTaskService,
			TaskProgressService taskProgressService)
		{
			this.dailyTaskService = dailyTaskService;
			this.taskProgressService = taskProgressService;
		}

		private ActiveTask currentTask()
		{
			return dailyTaskService.getActiveTask().get();
		}
	}

	private static final class InMemoryRepository implements DailyTaskRepository
	{
		private ActiveTask task;

		@Override
		public Optional<ActiveTask> load()
		{
			return Optional.ofNullable(task);
		}

		@Override
		public void save(ActiveTask activeTask)
		{
			task = activeTask;
		}

		@Override
		public void clear()
		{
			task = null;
		}
	}
}
