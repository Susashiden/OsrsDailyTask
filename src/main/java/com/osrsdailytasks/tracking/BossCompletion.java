package com.osrsdailytasks.tracking;

import java.util.Objects;

public final class BossCompletion
{
	private final String subjectId;
	private final int killCount;

	BossCompletion(String subjectId, int killCount)
	{
		this.subjectId = Objects.requireNonNull(subjectId, "subjectId");
		this.killCount = killCount;
	}

	public String getSubjectId()
	{
		return subjectId;
	}

	public int getKillCount()
	{
		return killCount;
	}
}
