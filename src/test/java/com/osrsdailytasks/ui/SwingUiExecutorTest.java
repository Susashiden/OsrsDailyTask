package com.osrsdailytasks.ui;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SwingUiExecutorTest
{
	@Test
	public void dispatchesBackgroundCallsToEventDispatchThread() throws Exception
	{
		CompletableFuture<Boolean> result = new CompletableFuture<>();

		new SwingUiExecutor().execute(() -> result.complete(SwingUtilities.isEventDispatchThread()));

		assertTrue(result.get(5, TimeUnit.SECONDS));
	}
}
