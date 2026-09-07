package com.osrsdailytasks.boss.target;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Random;
import com.osrsdailytasks.TaskDifficulty;
import org.junit.Test;

public class BossTargetCalculatorTest
{
	private final BossTargetCalculator calculator = new BossTargetCalculator();

	@Test
	public void calculatesApprovedRangesAcrossEveryDifficulty()
	{
		assertRanges("45", new int[][]{{4, 7}, {8, 14}, {16, 28}});
		assertRanges("30", new int[][]{{2, 4}, {5, 9}, {11, 18}});
		assertRanges("37", new int[][]{{3, 5}, {6, 11}, {13, 23}});
		assertRanges("60", new int[][]{{5, 9}, {11, 18}, {22, 37}});
		assertRanges("22", new int[][]{{2, 3}, {4, 6}, {8, 13}});
	}

	@Test
	public void floorsOnlyAfterApplyingTheDifficultyMultiplier()
	{
		assertRange(calculator.calculateForRate(
			new BigDecimal("23.5"), TaskDifficulty.EASY), 2, 3);
		assertRange(calculator.calculateForRate(
			new BigDecimal("23.5"), TaskDifficulty.NORMAL), 4, 7);
		assertRange(calculator.calculateForRate(
			new BigDecimal("23.5"), TaskDifficulty.HARD), 8, 14);
	}

	@Test
	public void clampsSmallRatesToOne()
	{
		for (TaskDifficulty difficulty : TaskDifficulty.values())
		{
			assertRange(calculator.calculateForRate(
				new BigDecimal("0.8"), difficulty), 1, 1);
		}
	}

	@Test
	public void clampsOverflowToIntegerMaximum()
	{
		BossTargetRange range = calculator.calculateForRate(
			new BigDecimal("1E100"), TaskDifficulty.HARD);
		assertRange(range, Integer.MAX_VALUE, Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, range.select(new Random(1)));
	}

	@Test
	public void rejectsInvalidRates()
	{
		assertInvalid(() -> calculator.calculateForRate(
			BigDecimal.ZERO, TaskDifficulty.NORMAL));
		assertInvalid(() -> calculator.calculateForRate(
			BigDecimal.ONE.negate(), TaskDifficulty.NORMAL));
	}

	@Test
	public void selectionIncludesBothBoundaries()
	{
		BossTargetRange range = new BossTargetRange(4, 7);
		assertEquals(4, range.select(new BoundRandom(false)));
		assertEquals(7, range.select(new BoundRandom(true)));
	}

	@Test
	public void fixedSeedSelectionIsDeterministicAndWithinRange()
	{
		BossTargetRange range = new BossTargetRange(8, 14);
		Random first = new Random(42);
		Random second = new Random(42);
		for (int index = 0; index < 20; index++)
		{
			int selected = range.select(first);
			assertEquals(selected, range.select(second));
			assertTrue(selected >= range.getMinimum());
			assertTrue(selected <= range.getMaximum());
		}
	}

	@Test
	public void rangeRejectsInvalidBoundaries()
	{
		assertInvalid(() -> new BossTargetRange(0, 1));
		assertInvalid(() -> new BossTargetRange(2, 1));
	}

	private void assertRanges(String rate, int[][] expected)
	{
		for (int index = 0; index < TaskDifficulty.values().length; index++)
		{
			assertRange(calculator.calculateForRate(
				new BigDecimal(rate), TaskDifficulty.values()[index]),
				expected[index][0], expected[index][1]);
		}
	}

	private static void assertRange(BossTargetRange range, int minimum, int maximum)
	{
		assertEquals(minimum, range.getMinimum());
		assertEquals(maximum, range.getMaximum());
	}

	private static void assertInvalid(Runnable operation)
	{
		try
		{
			operation.run();
			fail("Expected invalid value rejection");
		}
		catch (IllegalArgumentException expected)
		{
			// Expected.
		}
	}

	private static final class BoundRandom extends Random
	{
		private final boolean maximum;

		private BoundRandom(boolean maximum)
		{
			this.maximum = maximum;
		}

		@Override
		public int nextInt(int bound)
		{
			return maximum ? bound - 1 : 0;
		}
	}
}
