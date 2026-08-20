package com.osrsdailytasks.ui;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DevelopmentTaskControllerTest
{
	@Test
	public void controlsVisibilityAssignmentCancellationAndCleanup()
	{
		OsrsDailyTasksPanel panel = new OsrsDailyTasksPanel();
		AtomicBoolean visible = new AtomicBoolean(false);
		AtomicReference<String> assignedId = new AtomicReference<>();
		AtomicInteger cancellationCount = new AtomicInteger();
		AtomicInteger resetCount = new AtomicInteger();
		AtomicInteger refreshCount = new AtomicInteger();
		AtomicReference<ActiveTask> initializedTask = new AtomicReference<>();
		ActiveTask assignedTask = activeTask("boss-giant-mole", TaskType.BOSS, "GIANT_MOLE");
		DevelopmentTaskController controller = new DevelopmentTaskController(
			panel,
			Runnable::run,
			visible::get,
			Arrays.asList(option("xp-agility"), option("boss-giant-mole")),
			Runnable::run,
			() -> true,
			taskId ->
			{
				assignedId.set(taskId);
				return assignedTask;
			},
			cancellationCount::incrementAndGet,
			resetCount::incrementAndGet,
			refreshCount::incrementAndGet,
			initializedTask::set);

		controller.start();
		assertFalse(panel.isDevelopmentControlsVisible());
		assertEquals(2, panel.getDevelopmentTaskCount());

		visible.set(true);
		controller.refreshVisibility();
		assertTrue(panel.isDevelopmentControlsVisible());

		panel.selectDevelopmentTask("boss-giant-mole");
		panel.clickAssignDevelopmentTask();
		assertEquals("boss-giant-mole", assignedId.get());
		assertEquals(assignedTask, initializedTask.get());
		assertEquals(1, resetCount.get());
		assertEquals(1, refreshCount.get());

		panel.clickCancelDevelopmentTask();
		assertEquals(1, cancellationCount.get());
		assertEquals(2, resetCount.get());
		assertEquals(2, refreshCount.get());

		controller.stop();
		assertFalse(panel.isDevelopmentControlsVisible());
		assertEquals(0, panel.getDevelopmentTaskCount());
	}

	@Test
	public void assignmentRequiresALoggedInClient()
	{
		OsrsDailyTasksPanel panel = new OsrsDailyTasksPanel();
		AtomicInteger assignmentCount = new AtomicInteger();
		AtomicInteger cancellationCount = new AtomicInteger();
		DevelopmentTaskController controller = new DevelopmentTaskController(
			panel,
			Runnable::run,
			() -> true,
			Arrays.asList(option("xp-agility")),
			Runnable::run,
			() -> false,
			taskId ->
			{
				assignmentCount.incrementAndGet();
				return activeTask(taskId, TaskType.XP, "AGILITY");
			},
			cancellationCount::incrementAndGet,
			() -> { },
			() -> { },
			task -> { });

		controller.start();
		panel.clickAssignDevelopmentTask();

		assertEquals(0, assignmentCount.get());
		assertEquals(
			"<html>Log in before assigning a development task.</html>",
			panel.getDevelopmentStatus());

		panel.clickCancelDevelopmentTask();
		assertEquals(0, cancellationCount.get());
		assertEquals(
			"<html>Log in before cancelling a development task.</html>",
			panel.getDevelopmentStatus());
	}

	private static DevelopmentTaskOption option(String id)
	{
		return new DevelopmentTaskOption(new TaskDefinition(
			id,
			TaskType.ACTIVITY,
			id.toUpperCase(),
			id,
			1,
			1));
	}

	private static ActiveTask activeTask(String id, TaskType type, String subjectId)
	{
		return new ActiveTask(
			LocalDate.of(2026, 8, 20),
			id,
			type,
			subjectId,
			id,
			1,
			0,
			null,
			null,
			0);
	}
}
