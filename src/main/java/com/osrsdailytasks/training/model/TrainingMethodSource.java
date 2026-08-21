package com.osrsdailytasks.training.model;

import java.util.Objects;

public final class TrainingMethodSource
{
	private final TrainingMethodSourceType type;
	private final String sourceId;
	private final String version;
	private final String derivation;

	public TrainingMethodSource(
		TrainingMethodSourceType type,
		String sourceId,
		String version,
		String derivation)
	{
		this.type = Objects.requireNonNull(type, "type");
		this.sourceId = requireText(sourceId, "sourceId");
		this.version = requireText(version, "version");
		this.derivation = derivation == null ? null : requireText(derivation, "derivation");
	}

	public TrainingMethodSourceType getType()
	{
		return type;
	}

	public String getSourceId()
	{
		return sourceId;
	}

	public String getVersion()
	{
		return version;
	}

	public String getDerivation()
	{
		return derivation;
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
