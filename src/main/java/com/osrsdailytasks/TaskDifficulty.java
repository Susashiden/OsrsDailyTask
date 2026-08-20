package com.osrsdailytasks;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum TaskDifficulty
{
	EASY(1, 2),
	NORMAL(1, 1),
	HARD(2, 1);

	private final int multiplier;
	private final int divisor;

	TaskDifficulty(int multiplier, int divisor)
	{
		this.multiplier = multiplier;
		this.divisor = divisor;
	}

	public int scaleTarget(double target)
	{
		if (!Double.isFinite(target) || target <= 0)
		{
			throw new IllegalArgumentException("target must be finite and positive");
		}

		BigDecimal scaled = BigDecimal.valueOf(target)
			.multiply(BigDecimal.valueOf(multiplier))
			.divide(BigDecimal.valueOf(divisor), 0, RoundingMode.FLOOR);
		if (scaled.compareTo(BigDecimal.ONE) < 0)
		{
			return 1;
		}
		if (scaled.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0)
		{
			return Integer.MAX_VALUE;
		}
		return scaled.intValue();
	}

	public boolean isAtLeast(TaskDifficulty minimumDifficulty)
	{
		if (minimumDifficulty == null)
		{
			throw new NullPointerException("minimumDifficulty");
		}
		return ordinal() >= minimumDifficulty.ordinal();
	}
}
