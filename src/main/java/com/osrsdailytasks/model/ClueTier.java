package com.osrsdailytasks.model;

import java.util.Locale;
import java.util.Optional;

public enum ClueTier
{
	BEGINNER("beginner", 4, 6),
	EASY("easy", 3, 5),
	MEDIUM("medium", 2, 4),
	HARD("hard", 1.5, 3),
	ELITE("elite", 0.5, 1.5),
	MASTER("master", 0.5, 1);

	private final String messageName;
	private final double minimumTarget;
	private final double maximumTarget;

	ClueTier(String messageName, double minimumTarget, double maximumTarget)
	{
		this.messageName = messageName;
		this.minimumTarget = minimumTarget;
		this.maximumTarget = maximumTarget;
	}

	public String getMessageName()
	{
		return messageName;
	}

	public String getSubjectId()
	{
		return "CLUE_SCROLL_" + name();
	}

	public double getMinimumTarget()
	{
		return minimumTarget;
	}

	public double getMaximumTarget()
	{
		return maximumTarget;
	}

	public static Optional<ClueTier> fromMessageName(String value)
	{
		if (value == null)
		{
			return Optional.empty();
		}

		String normalized = value.toLowerCase(Locale.ENGLISH);
		for (ClueTier tier : values())
		{
			if (tier.messageName.equals(normalized))
			{
				return Optional.of(tier);
			}
		}
		return Optional.empty();
	}
}
