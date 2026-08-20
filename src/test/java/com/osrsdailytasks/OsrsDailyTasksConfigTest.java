package com.osrsdailytasks;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OsrsDailyTasksConfigTest
{
	private final OsrsDailyTasksConfig config = new OsrsDailyTasksConfig()
	{
	};

	@Test
	public void usesStablePhaseFourDefaults()
	{
		assertSame(TaskDifficulty.NORMAL, config.difficulty());
		assertSame(PvmDifficulty.ANY, config.pvmDifficulty());
		assertFalse(config.showOverlay());
		assertTrue(config.notifyOnCompletion());
		assertTrue(config.showRolloverTime());
		assertFalse(config.developmentControls());
	}
}
