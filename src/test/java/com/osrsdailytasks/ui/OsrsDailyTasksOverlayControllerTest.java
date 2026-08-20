package com.osrsdailytasks.ui;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import net.runelite.client.ui.overlay.Overlay;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class OsrsDailyTasksOverlayControllerTest
{
	@Test
	public void registersRefreshesClearsAndRemovesOverlay()
	{
		OsrsDailyTasksOverlay overlay = new OsrsDailyTasksOverlay(new OsrsDailyTasksConfig()
		{
		});
		FakeOverlayHost overlayHost = new FakeOverlayHost();
		AtomicReference<Optional<TaskUiModel>> model = new AtomicReference<>(
			Optional.of(OsrsDailyTasksPanelTest.model(false)));
		OsrsDailyTasksOverlayController controller = new OsrsDailyTasksOverlayController(
			overlay,
			overlayHost,
			model::get);

		controller.start();
		assertSame(overlay, overlayHost.added);
		assertSame(model.get().get(), overlay.getModel());

		TaskUiModel completed = OsrsDailyTasksPanelTest.model(true);
		model.set(Optional.of(completed));
		controller.refresh();
		assertSame(completed, overlay.getModel());

		controller.stop();
		assertSame(overlay, overlayHost.removed);
		assertNull(overlay.getModel());
	}

	private static final class FakeOverlayHost implements OverlayHost
	{
		private Overlay added;
		private Overlay removed;

		@Override
		public void add(Overlay overlay)
		{
			added = overlay;
		}

		@Override
		public void remove(Overlay overlay)
		{
			removed = overlay;
		}
	}
}
