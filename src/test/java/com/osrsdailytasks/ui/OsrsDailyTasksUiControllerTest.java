package com.osrsdailytasks.ui;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.runelite.client.ui.NavigationButton;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OsrsDailyTasksUiControllerTest
{
	@Test
	public void registersRefreshesAndRemovesNavigation() throws Exception
	{
		OsrsDailyTasksPanel panel = new OsrsDailyTasksPanel();
		FakeNavigationHost navigationHost = new FakeNavigationHost();
		AtomicReference<Optional<TaskUiModel>> model = new AtomicReference<>(
			Optional.of(OsrsDailyTasksPanelTest.model(false)));
		AtomicBoolean showRollover = new AtomicBoolean(true);
		OsrsDailyTasksUiController controller = new OsrsDailyTasksUiController(
			panel,
			navigationHost,
			Runnable::run,
			model::get,
			showRollover::get);

		controller.start();

		NavigationButton button = controller.getNavigationButton();
		assertSame(button, navigationHost.added);
		assertEquals("OSRS Daily Tasks", button.getTooltip());
		assertSame(panel, button.getPanel());
		assertNotNull(button.getIcon());
		assertEquals(16, button.getIcon().getWidth());
		assertEquals(16, button.getIcon().getHeight());
		assertEquals("Complete hard clue scrolls", panel.getDisplayedTitle());
		assertTrue(panel.isRolloverVisible());

		showRollover.set(false);
		controller.refresh();
		assertFalse(panel.isRolloverVisible());

		model.set(Optional.empty());
		controller.refresh();
		assertEquals("No active daily task", panel.getDisplayedTitle());

		model.set(Optional.of(OsrsDailyTasksPanelTest.model(true)));
		controller.refresh();
		assertEquals("Complete hard clue scrolls", panel.getDisplayedTitle());

		controller.stop();
		assertSame(button, navigationHost.removed);
		assertEquals("No active daily task", panel.getDisplayedTitle());
	}

	private static final class FakeNavigationHost implements NavigationHost
	{
		private NavigationButton added;
		private NavigationButton removed;

		@Override
		public void add(NavigationButton navigationButton)
		{
			added = navigationButton;
		}

		@Override
		public void remove(NavigationButton navigationButton)
		{
			removed = navigationButton;
		}
	}
}
