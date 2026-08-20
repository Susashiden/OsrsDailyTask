package com.osrsdailytasks.ui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OsrsDailyTasksOverlayTest
{
	@Test
	public void rendersOnlyWhenEnabledWithAnActiveModel()
	{
		AtomicBoolean enabled = new AtomicBoolean(false);
		OsrsDailyTasksOverlay overlay = new OsrsDailyTasksOverlay(config(enabled));
		overlay.setModel(Optional.of(OsrsDailyTasksPanelTest.model(false)));
		Graphics2D graphics = graphics();

		assertNull(overlay.render(graphics));
		assertTrue(overlay.getPanelComponent().getChildren().isEmpty());

		enabled.set(true);
		Dimension rendered = overlay.render(graphics);

		assertNotNull(rendered);
		assertEquals(3, overlay.getPanelComponent().getChildren().size());
		assertTrue(overlay.getPanelComponent().getChildren().get(0) instanceof TitleComponent);
		assertTrue(overlay.getPanelComponent().getChildren().get(1) instanceof LineComponent);
		assertTrue(overlay.getPanelComponent().getChildren().get(2) instanceof ProgressBarComponent);
		graphics.dispose();
	}

	@Test
	public void configurationChangesApplyWithoutRecreatingOverlay()
	{
		AtomicBoolean enabled = new AtomicBoolean(true);
		OsrsDailyTasksOverlay overlay = new OsrsDailyTasksOverlay(config(enabled));
		TaskUiModel model = OsrsDailyTasksPanelTest.model(true);
		overlay.setModel(Optional.of(model));
		Graphics2D graphics = graphics();

		assertNotNull(overlay.render(graphics));
		assertSame(model, overlay.getModel());

		enabled.set(false);
		assertNull(overlay.render(graphics));
		assertTrue(overlay.getPanelComponent().getChildren().isEmpty());
		graphics.dispose();
	}

	@Test
	public void enabledOverlayStillHidesWithoutATask()
	{
		OsrsDailyTasksOverlay overlay = new OsrsDailyTasksOverlay(config(new AtomicBoolean(true)));
		overlay.setModel(Optional.empty());
		Graphics2D graphics = graphics();

		assertNull(overlay.render(graphics));
		graphics.dispose();
	}

	private static OsrsDailyTasksConfig config(AtomicBoolean enabled)
	{
		return new OsrsDailyTasksConfig()
		{
			@Override
			public boolean showOverlay()
			{
				return enabled.get();
			}
		};
	}

	private static Graphics2D graphics()
	{
		return new BufferedImage(300, 150, BufferedImage.TYPE_INT_ARGB).createGraphics();
	}
}
