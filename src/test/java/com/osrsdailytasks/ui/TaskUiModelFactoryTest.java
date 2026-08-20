package com.osrsdailytasks.ui;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TaskUiModelFactoryTest
{
	private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Singapore");
	private static final Clock TEST_CLOCK = Clock.fixed(
		Instant.parse("2026-08-18T04:00:00Z"),
		TEST_ZONE);

	private final TaskUiModelFactory factory = new TaskUiModelFactory(
		TEST_CLOCK,
		new OsrsDailyTasksConfig()
		{
			@Override
			public TaskDifficulty difficulty()
			{
				return TaskDifficulty.HARD;
			}
		});

	@Test
	public void createsActiveTaskPresentation()
	{
		TaskUiModel model = factory.create(task(TaskType.BOSS, 3, 0, null));

		assertEquals("Test task", model.getTitle());
		assertEquals("Boss", model.getCategory());
		assertEquals(3, model.getTargetAmount());
		assertEquals(0, model.getProgress());
		assertEquals(0, model.getProgressPercentage());
		assertEquals("0 / 3", model.getProgressText());
		assertEquals(TaskDifficulty.HARD, model.getDifficulty());
		assertEquals(LocalDate.of(2026, 8, 18), model.getAssignmentDate());
		assertEquals(
			ZonedDateTime.of(2026, 8, 19, 0, 0, 0, 0, TEST_ZONE),
			model.getNextRolloverAt());
		assertFalse(model.isComplete());
		assertNull(model.getCompletedAt());
	}

	@Test
	public void createsPartialPresentationWithWholePercentage()
	{
		TaskUiModel model = factory.create(task(TaskType.ACTIVITY, 8, 3, null));

		assertEquals("Activity", model.getCategory());
		assertEquals(3, model.getProgress());
		assertEquals(37, model.getProgressPercentage());
		assertEquals("3 / 8", model.getProgressText());
		assertFalse(model.isComplete());
	}

	@Test
	public void createsCompletedPresentationInLocalTime()
	{
		Instant completionTime = Instant.parse("2026-08-18T10:15:30Z");
		TaskUiModel model = factory.create(task(TaskType.XP, 10_000, 10_000, completionTime));

		assertEquals("XP", model.getCategory());
		assertEquals(100, model.getProgressPercentage());
		assertTrue(model.isComplete());
		assertEquals(completionTime.atZone(TEST_ZONE), model.getCompletedAt());
	}

	@Test
	public void clampsUnsafeDisplayCalculations()
	{
		assertEquals(0, TaskUiModelFactory.clampProgress(-10, 100));
		assertEquals(100, TaskUiModelFactory.clampProgress(150, 100));
		assertEquals(0, TaskUiModelFactory.clampProgress(10, 0));
		assertEquals(0, TaskUiModelFactory.calculateProgressPercentage(-10, 100));
		assertEquals(100, TaskUiModelFactory.calculateProgressPercentage(150, 100));
		assertEquals(0, TaskUiModelFactory.calculateProgressPercentage(10, 0));
	}

	private static ActiveTask task(
		TaskType taskType,
		int target,
		int progress,
		Instant completedAt)
	{
		return new ActiveTask(
			LocalDate.of(2026, 8, 18),
			"test-task",
			taskType,
			"TEST_SUBJECT",
			"Test task",
			target,
			progress,
			null,
			completedAt,
			0);
	}
}
