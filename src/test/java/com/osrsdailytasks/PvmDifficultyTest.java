package com.osrsdailytasks;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PvmDifficultyTest
{
	@Test
	public void exposesApprovedBaseRanges()
	{
		assertRange(PvmDifficulty.LOW, 4, 8);
		assertRange(PvmDifficulty.MEDIUM, 3, 6);
		assertRange(PvmDifficulty.HIGH, 1.5, 3);
		assertRange(PvmDifficulty.ENDGAME, 0.5, 1.5);
	}

	@Test
	public void maximumDifficultyAllowsTiersAtOrBelowIt()
	{
		assertTrue(PvmDifficulty.ANY.allows(PvmDifficulty.ENDGAME));
		assertTrue(PvmDifficulty.HIGH.allows(PvmDifficulty.LOW));
		assertTrue(PvmDifficulty.HIGH.allows(PvmDifficulty.HIGH));
		assertFalse(PvmDifficulty.HIGH.allows(PvmDifficulty.ENDGAME));
		assertFalse(PvmDifficulty.LOW.allows(PvmDifficulty.MEDIUM));
		assertFalse(PvmDifficulty.ANY.allows(PvmDifficulty.ANY));
	}

	@Test
	public void combinesPvmRangesWithEveryTaskDifficulty()
	{
		int[][] easy = {{2, 4}, {1, 3}, {1, 1}, {1, 1}};
		int[][] normal = {{4, 8}, {3, 6}, {1, 3}, {1, 1}};
		int[][] hard = {{8, 16}, {6, 12}, {3, 6}, {1, 3}};
		PvmDifficulty[] tiers = {
			PvmDifficulty.LOW,
			PvmDifficulty.MEDIUM,
			PvmDifficulty.HIGH,
			PvmDifficulty.ENDGAME
		};
		for (int index = 0; index < tiers.length; index++)
		{
			assertScaled(tiers[index], TaskDifficulty.EASY, easy[index]);
			assertScaled(tiers[index], TaskDifficulty.NORMAL, normal[index]);
			assertScaled(tiers[index], TaskDifficulty.HARD, hard[index]);
		}
	}

	private static void assertRange(PvmDifficulty difficulty, double minimum, double maximum)
	{
		assertEquals(minimum, difficulty.getMinimumTarget(), 0.0);
		assertEquals(maximum, difficulty.getMaximumTarget(), 0.0);
	}

	private static void assertScaled(
		PvmDifficulty pvmDifficulty,
		TaskDifficulty taskDifficulty,
		int[] expected)
	{
		assertEquals(expected[0], taskDifficulty.scaleTarget(pvmDifficulty.getMinimumTarget()));
		assertEquals(expected[1], taskDifficulty.scaleTarget(pvmDifficulty.getMaximumTarget()));
	}
}
