package com.osrsdailytasks.tracking;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import com.osrsdailytasks.persistence.DailyTaskRepository;
import com.osrsdailytasks.service.DailyTaskService;
import com.osrsdailytasks.task.generation.TaskGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaskProgressServiceTest
{
	private static final LocalDate TEST_DATE = LocalDate.of(2026, 8, 18);
	private static final Instant COMPLETION_TIME = Instant.parse("2026-08-18T12:00:00Z");
	private static final Clock TEST_CLOCK = Clock.fixed(COMPLETION_TIME, ZoneOffset.UTC);

	@Test
	public void initializesXpBaselineWithoutAwardingProgress()
	{
		Fixture fixture = fixture(task(TaskType.XP, "AGILITY", 1_000, null));

		assertEquals(ProgressUpdateResult.UPDATED, fixture.progressService.recordXp("AGILITY", 50_000));
		assertEquals(Integer.valueOf(50_000), fixture.currentTask().getStartingXp());
		assertEquals(0, fixture.currentTask().getProgress());
		assertEquals(1, fixture.repository.saveCount);
	}

	@Test
	public void tracksXpMonotonicallyAndCompletesExactlyOnce()
	{
		Fixture fixture = fixture(task(TaskType.XP, "AGILITY", 1_000, 50_000));

		assertEquals(ProgressUpdateResult.UPDATED, fixture.progressService.recordXp("AGILITY", 50_600));
		assertEquals(600, fixture.currentTask().getProgress());
		assertEquals(ProgressUpdateResult.IGNORED, fixture.progressService.recordXp("AGILITY", 50_500));
		assertEquals(600, fixture.currentTask().getProgress());
		assertEquals(ProgressUpdateResult.COMPLETED, fixture.progressService.recordXp("AGILITY", 51_500));
		assertEquals(1_000, fixture.currentTask().getProgress());
		assertEquals(COMPLETION_TIME, fixture.currentTask().getCompletedAt());
		assertEquals(ProgressUpdateResult.IGNORED, fixture.progressService.recordXp("AGILITY", 52_000));
		assertEquals(1, fixture.repository.saveCount);
	}

	@Test
	public void ignoresXpForAnUnassignedSkill()
	{
		Fixture fixture = fixture(task(TaskType.XP, "AGILITY", 1_000, 50_000));

		assertEquals(ProgressUpdateResult.IGNORED, fixture.progressService.recordXp("MAGIC", 60_000));
		assertEquals(0, fixture.repository.saveCount);
	}

	@Test
	public void incrementsDiscreteProgressAndRejectsSameTickDuplicate()
	{
		Fixture fixture = fixture(task(TaskType.BOSS, "GIANT_MOLE", 2, null));

		assertEquals(
			ProgressUpdateResult.UPDATED,
			fixture.progressService.recordDiscreteCompletion(TaskType.BOSS, "GIANT_MOLE", 100));
		assertEquals(1, fixture.currentTask().getProgress());
		assertEquals(
			ProgressUpdateResult.IGNORED,
			fixture.progressService.recordDiscreteCompletion(TaskType.BOSS, "GIANT_MOLE", 100));
		assertEquals(1, fixture.currentTask().getProgress());
		assertEquals(
			ProgressUpdateResult.COMPLETED,
			fixture.progressService.recordDiscreteCompletion(TaskType.BOSS, "GIANT_MOLE", 101));
		assertEquals(2, fixture.currentTask().getProgress());
		assertEquals(COMPLETION_TIME, fixture.currentTask().getCompletedAt());
		assertEquals(2, fixture.repository.saveCount);
	}

	@Test
	public void clueProgressRequiresTheAssignedTierAndTaskType()
	{
		Fixture fixture = fixture(task(TaskType.ACTIVITY, "CLUE_SCROLL_HARD", 1, null));

		assertEquals(
			ProgressUpdateResult.IGNORED,
			fixture.progressService.recordDiscreteCompletion(TaskType.BOSS, "CLUE_SCROLL_HARD", 100));
		assertEquals(
			ProgressUpdateResult.IGNORED,
			fixture.progressService.recordDiscreteCompletion(TaskType.ACTIVITY, "CLUE_SCROLL_EASY", 100));
		assertEquals(
			ProgressUpdateResult.COMPLETED,
			fixture.progressService.recordDiscreteCompletion(TaskType.ACTIVITY, "CLUE_SCROLL_HARD", 100));
		assertEquals(1, fixture.repository.saveCount);
	}

	private static Fixture fixture(ActiveTask activeTask)
	{
		InMemoryRepository repository = new InMemoryRepository(activeTask);
		TaskDefinition fallbackDefinition = new TaskDefinition(
			"fallback",
			TaskType.XP,
			"AGILITY",
			"Fallback",
			1,
			1);
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(fallbackDefinition),
			new Random(1L));
		DailyTaskService dailyTaskService = new DailyTaskService(
			repository,
			generator,
			ignored -> true,
			TEST_CLOCK);
		dailyTaskService.loadOrGenerate();
		repository.saveCount = 0;
		return new Fixture(repository, dailyTaskService, new TaskProgressService(dailyTaskService, TEST_CLOCK));
	}

	private static ActiveTask task(TaskType type, String subjectId, int target, Integer startingXp)
	{
		return new ActiveTask(
			TEST_DATE,
			"test-task",
			type,
			subjectId,
			"Test task",
			target,
			0,
			startingXp,
			null,
			0);
	}

	private static final class Fixture
	{
		private final InMemoryRepository repository;
		private final DailyTaskService dailyTaskService;
		private final TaskProgressService progressService;

		private Fixture(
			InMemoryRepository repository,
			DailyTaskService dailyTaskService,
			TaskProgressService progressService)
		{
			this.repository = repository;
			this.dailyTaskService = dailyTaskService;
			this.progressService = progressService;
		}

		private ActiveTask currentTask()
		{
			return dailyTaskService.getActiveTask().get();
		}
	}

	private static final class InMemoryRepository implements DailyTaskRepository
	{
		private ActiveTask task;
		private int saveCount;

		private InMemoryRepository(ActiveTask task)
		{
			this.task = task;
		}

		@Override
		public Optional<ActiveTask> load()
		{
			return Optional.ofNullable(task);
		}

		@Override
		public void save(ActiveTask activeTask)
		{
			task = activeTask;
			saveCount++;
		}

		@Override
		public void clear()
		{
			task = null;
		}
	}
}
