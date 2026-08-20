package com.osrsdailytasks;

public enum PvmDifficulty
{
	LOW("Low", 4, 8),
	MEDIUM("Medium", 3, 6),
	HIGH("High", 1.5, 3),
	ENDGAME("Endgame", 0.5, 1.5),
	ANY("Any", 0, 0);

	private final String displayName;
	private final double minimumTarget;
	private final double maximumTarget;

	PvmDifficulty(String displayName, double minimumTarget, double maximumTarget)
	{
		this.displayName = displayName;
		this.minimumTarget = minimumTarget;
		this.maximumTarget = maximumTarget;
	}

	public double getMinimumTarget()
	{
		if (this == ANY)
		{
			throw new IllegalStateException("Any does not define a target range");
		}
		return minimumTarget;
	}

	public double getMaximumTarget()
	{
		if (this == ANY)
		{
			throw new IllegalStateException("Any does not define a target range");
		}
		return maximumTarget;
	}

	public boolean allows(PvmDifficulty bossDifficulty)
	{
		if (bossDifficulty == null || bossDifficulty == ANY)
		{
			return false;
		}
		return this == ANY || bossDifficulty.ordinal() <= ordinal();
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
