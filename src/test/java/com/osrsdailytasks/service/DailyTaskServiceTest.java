package com.osrsdailytasks.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import com.osrsdailytasks.persistence.DailyTaskRepository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DailyTaskServiceTest
{
	private static final LocalDate TODAY = LocalDate.of(2026, 8, 18);
	private static final Instant TODAY_AT_NOON = Instant.parse("2026-08-18T12:00:00Z");

	@Test
	public void generatesAndPersistsWhenNoTaskExists()
	{
		InMemoryRepository repository = new InMemoryRepository();
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON));

		ActiveTask generated = service.loadOrGenerate();

		assertEquals(TODAY, generated.getGenerationDate());
		assertEquals(1, repository.saveCount);
		assertEquals(generated, repository.task);
		assertEquals(generated, service.getActiveTask().get());
	}

	@Test
	public void retainsTodaysTaskWithoutSavingAgain()
	{
		ActiveTask existing = taskFor(TODAY, "existing");
		InMemoryRepository repository = new InMemoryRepository(existing);
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON));

		ActiveTask loaded = service.loadOrGenerate();

		assertEquals(existing, loaded);
		assertEquals(0, repository.saveCount);
	}

	@Test
	public void difficultyDoesNotRewriteTodaysStoredTarget()
	{
		ActiveTask existing = taskFor(TODAY, "existing");
		InMemoryRepository repository = new InMemoryRepository(existing);
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON), TaskDifficulty.HARD);

		ActiveTask loaded = service.loadOrGenerate();

		assertEquals(5_000, loaded.getTargetAmount());
		assertEquals(0, repository.saveCount);
	}

	@Test
	public void replacesAnOlderTaskExactlyOnce()
	{
		InMemoryRepository repository = new InMemoryRepository(taskFor(TODAY.minusDays(1), "old"));
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON));

		ActiveTask first = service.loadOrGenerate();
		ActiveTask second = service.loadOrGenerate();

		assertEquals(TODAY, first.getGenerationDate());
		assertEquals(first, second);
		assertEquals(1, repository.saveCount);
	}

	@Test
	public void retainsFutureDatedTaskWhenClockMovesBackward()
	{
		ActiveTask future = taskFor(TODAY.plusDays(1), "future");
		InMemoryRepository repository = new InMemoryRepository(future);
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON));

		assertEquals(future, service.loadOrGenerate());
		assertEquals(0, repository.saveCount);
	}

	@Test
	public void clearsOnlyCachedState()
	{
		InMemoryRepository repository = new InMemoryRepository();
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON));
		service.loadOrGenerate();

		service.clearCachedTask();

		assertFalse(service.getActiveTask().isPresent());
		assertTrue(repository.load().isPresent());
	}

	@Test
	public void assignsAndPersistsASelectedDevelopmentTask()
	{
		InMemoryRepository repository = new InMemoryRepository(taskFor(TODAY, "existing"));
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON));
		service.loadOrGenerate();

		ActiveTask assigned = service.assignForDevelopment("xp-agility");

		assertEquals("xp-agility", assigned.getTaskId());
		assertEquals(0, assigned.getProgress());
		assertFalse(assigned.isComplete());
		assertEquals(assigned, repository.task);
		assertEquals(assigned, service.getActiveTask().get());
		assertEquals(1, repository.saveCount);
	}

	@Test
	public void cancelsThePersistedDevelopmentTask()
	{
		InMemoryRepository repository = new InMemoryRepository();
		DailyTaskService service = service(repository, clock(TODAY_AT_NOON));
		service.loadOrGenerate();

		service.cancelForDevelopment();

		assertFalse(service.getActiveTask().isPresent());
		assertFalse(repository.load().isPresent());
		assertEquals(1, repository.clearCount);
	}

	private static DailyTaskService service(InMemoryRepository repository, Clock clock)
	{
		return service(repository, clock, TaskDifficulty.NORMAL);
	}

	private static DailyTaskService service(
		InMemoryRepository repository,
		Clock clock,
		TaskDifficulty difficulty)
	{
		TaskDefinition definition = new TaskDefinition(
			"xp-agility",
			TaskType.XP,
			"AGILITY",
			"Gain Agility XP",
			5_000,
			5_000);
		TaskGenerator generator = new TaskGenerator(
			Collections.singletonList(definition),
			new Random(1L),
			difficulty);
		return new DailyTaskService(repository, generator, ignored -> true, clock);
	}

	private static Clock clock(Instant instant)
	{
		return Clock.fixed(instant, ZoneOffset.UTC);
	}

	private static ActiveTask taskFor(LocalDate date, String id)
	{
		return new ActiveTask(
			date,
			id,
			TaskType.XP,
			"AGILITY",
			"Gain Agility XP",
			5_000,
			0,
			null,
			null,
			0);
	}

	private static final class InMemoryRepository implements DailyTaskRepository
	{
		private ActiveTask task;
		private int saveCount;
		private int clearCount;

		private InMemoryRepository()
		{
		}

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
			clearCount++;
		}
	}
}
