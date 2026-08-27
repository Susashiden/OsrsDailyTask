package com.osrsdailytasks.boss.model;

import java.math.BigDecimal;
import java.util.Objects;
import com.osrsdailytasks.training.account.EhpProfile;

public final class BossEfficiencyDefinition
{
	private final EhpProfile profile;
	private final String womBossId;
	private final String subjectId;
	private final BigDecimal killsPerHour;
	private final String sourceId;
	private final String sourceVersion;

	public BossEfficiencyDefinition(
		EhpProfile profile,
		String womBossId,
		String subjectId,
		BigDecimal killsPerHour,
		String sourceId,
		String sourceVersion)
	{
		this.profile = Objects.requireNonNull(profile, "profile");
		this.womBossId = requireIdentifier(womBossId, "womBossId", "[a-z0-9_]+");
		this.subjectId = requireIdentifier(subjectId, "subjectId", "[A-Z0-9_]+");
		this.killsPerHour = requirePositive(killsPerHour);
		this.sourceId = requireText(sourceId, "sourceId");
		this.sourceVersion = requireText(sourceVersion, "sourceVersion");
	}

	public EhpProfile getProfile()
	{
		return profile;
	}

	public String getWomBossId()
	{
		return womBossId;
	}

	public String getSubjectId()
	{
		return subjectId;
	}

	public BigDecimal getKillsPerHour()
	{
		return killsPerHour;
	}

	public String getSourceId()
	{
		return sourceId;
	}

	public String getSourceVersion()
	{
		return sourceVersion;
	}

	private static String requireIdentifier(String value, String name, String pattern)
	{
		String normalized = requireText(value, name);
		if (!normalized.matches(pattern))
		{
			throw new IllegalArgumentException(name + " is invalid: " + normalized);
		}
		return normalized;
	}

	private static BigDecimal requirePositive(BigDecimal value)
	{
		Objects.requireNonNull(value, "killsPerHour");
		if (value.compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException("killsPerHour must be positive");
		}
		return value;
	}

	private static String requireText(String value, String name)
	{
		Objects.requireNonNull(value, name);
		String trimmed = value.trim();
		if (trimmed.isEmpty())
		{
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return trimmed;
	}
}
