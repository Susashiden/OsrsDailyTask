package com.osrsdailytasks.training.target;

public final class XpTargetRange
{
	private final int minimum;
	private final int maximum;

	public XpTargetRange(int minimum, int maximum)
	{
		if (minimum <= 0 || maximum < minimum)
		{
			throw new IllegalArgumentException("XP target range is invalid");
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
}
