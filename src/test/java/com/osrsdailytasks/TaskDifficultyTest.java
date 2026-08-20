package com.osrsdailytasks;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaskDifficultyTest
{
	@Test
	public void scalesNormalEasyAndHardTargets()
	{
		assertEquals(5, TaskDifficulty.NORMAL.scaleTarget(5));
		assertEquals(2, TaskDifficulty.EASY.scaleTarget(5));
		assertEquals(10, TaskDifficulty.HARD.scaleTarget(5));
	}

	@Test
	public void easyDifficultyNeverProducesZero()
	{
		assertEquals(1, TaskDifficulty.EASY.scaleTarget(1));
		assertEquals(1, TaskDifficulty.EASY.scaleTarget(0.5));
	}

	@Test
	public void floorsDecimalTargetsAfterApplyingDifficulty()
	{
		assertEquals(1, TaskDifficulty.EASY.scaleTarget(3));
		assertEquals(1, TaskDifficulty.NORMAL.scaleTarget(1.5));
		assertEquals(3, TaskDifficulty.HARD.scaleTarget(1.5));
		assertEquals(1, TaskDifficulty.HARD.scaleTarget(0.5));
	}

	@Test
	public void comparesMinimumDifficultyInConfiguredOrder()
	{
		assertTrue(TaskDifficulty.HARD.isAtLeast(TaskDifficulty.EASY));
		assertTrue(TaskDifficulty.NORMAL.isAtLeast(TaskDifficulty.NORMAL));
		assertFalse(TaskDifficulty.NORMAL.isAtLeast(TaskDifficulty.HARD));
		assertFalse(TaskDifficulty.EASY.isAtLeast(TaskDifficulty.NORMAL));
	}
}
