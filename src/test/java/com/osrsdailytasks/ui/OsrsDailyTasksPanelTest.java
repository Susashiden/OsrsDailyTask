package com.osrsdailytasks.ui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OsrsDailyTasksPanelTest
{
	private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Singapore");

	@Test
	public void displaysActiveTaskAndOptionalRollover() throws Exception
	{
		OsrsDailyTasksPanel panel = new OsrsDailyTasksPanel();
		TaskUiModel model = model(false);

		SwingUtilities.invokeAndWait(() -> panel.showTask(model, true));

		assertEquals("Complete hard clue scrolls", panel.getDisplayedTitle());
		assertEquals("Category: Activity", panel.getDisplayedCategory());
		assertEquals("Difficulty: Hard", panel.getDisplayedDifficulty());
		assertEquals("Assigned: 18 Aug 2026", panel.getDisplayedAssignment());
		assertEquals("Next task: 19 Aug 2026, 00:00 SGT", panel.getDisplayedRollover());
		assertTrue(panel.isRolloverVisible());
		assertEquals("3 / 8 (37%)", panel.getDisplayedProgress());
		assertEquals(37, panel.getDisplayedProgressPercentage());
		assertEquals("In progress", panel.getDisplayedCompletion());

		SwingUtilities.invokeAndWait(() -> panel.showTask(model, false));
		assertFalse(panel.isRolloverVisible());
	}

	@Test
	public void displaysCompletionAndClearsToNoTask() throws Exception
	{
		OsrsDailyTasksPanel panel = new OsrsDailyTasksPanel();

		SwingUtilities.invokeAndWait(() -> panel.showTask(model(true), true));
		assertEquals("Completed: 18 Aug 2026, 18:15 SGT", panel.getDisplayedCompletion());
		assertEquals(100, panel.getDisplayedProgressPercentage());

		SwingUtilities.invokeAndWait(panel::showNoTask);
		assertEquals("No active daily task", panel.getDisplayedTitle());
		assertEquals("Log in to load today's task.", panel.getDisplayedCategory());
		assertEquals("0 / 0 (0%)", panel.getDisplayedProgress());
		assertEquals(0, panel.getDisplayedProgressPercentage());
		assertFalse(panel.isRolloverVisible());
	}

	@Test
	public void developmentControlsAreHiddenSelectableAndCleanedUp() throws Exception
	{
		OsrsDailyTasksPanel panel = new OsrsDailyTasksPanel();
		AtomicReference<String> assignedTaskId = new AtomicReference<>();
		AtomicBoolean cancelled = new AtomicBoolean();
		DevelopmentTaskOption xp = option("xp-agility", "Gain Agility XP");
		DevelopmentTaskOption boss = option("boss-giant-mole", "Defeat the Giant Mole");

		SwingUtilities.invokeAndWait(() -> panel.configureDevelopmentControls(
			Arrays.asList(xp, boss),
			assignedTaskId::set,
			() -> cancelled.set(true)));

		assertFalse(panel.isDevelopmentControlsVisible());
		assertEquals(2, panel.getDevelopmentTaskCount());

		SwingUtilities.invokeAndWait(() ->
		{
			panel.setDevelopmentControlsVisible(true);
			panel.selectDevelopmentTask("boss-giant-mole");
			panel.clickAssignDevelopmentTask();
			panel.clickCancelDevelopmentTask();
			panel.showDevelopmentStatus("Development status");
		});

		assertTrue(panel.isDevelopmentControlsVisible());
		assertEquals("boss-giant-mole", assignedTaskId.get());
		assertTrue(cancelled.get());
		assertEquals("<html>Development status</html>", panel.getDevelopmentStatus());

		SwingUtilities.invokeAndWait(panel::clearDevelopmentControls);
		assertFalse(panel.isDevelopmentControlsVisible());
		assertEquals(0, panel.getDevelopmentTaskCount());
	}

	private static DevelopmentTaskOption option(String id, String title)
	{
		return new DevelopmentTaskOption(new TaskDefinition(
			id,
			TaskType.ACTIVITY,
			id.toUpperCase(),
			title,
			1,
			1));
	}

	static TaskUiModel model(boolean complete)
	{
		return new TaskUiModel(
			"Complete hard clue scrolls",
			"Activity",
			8,
			complete ? 8 : 3,
			complete ? 100 : 37,
			TaskDifficulty.HARD,
			LocalDate.of(2026, 8, 18),
			complete,
			complete ? ZonedDateTime.of(2026, 8, 18, 18, 15, 0, 0, TEST_ZONE) : null,
			ZonedDateTime.of(2026, 8, 19, 0, 0, 0, 0, TEST_ZONE));
	}
}
