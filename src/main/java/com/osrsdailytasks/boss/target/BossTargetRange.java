package com.osrsdailytasks.boss.target;

import java.util.Objects;
import java.util.Random;

public final class BossTargetRange
{
	private final int minimum;
	private final int maximum;

	public BossTargetRange(int minimum, int maximum)
	{
		if (minimum <= 0 || maximum < minimum)
		{
			throw new IllegalArgumentException("Boss target range is invalid");
		}
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public int getMinimum()
	{
		return minimum;
	}

	public int getMaximum()
	{
		return maximum;
	}

	public int select(Random random)
	{
		Objects.requireNonNull(random, "random");
		long size = (long) maximum - minimum + 1;
		return minimum + random.nextInt((int) size);
	}
}
