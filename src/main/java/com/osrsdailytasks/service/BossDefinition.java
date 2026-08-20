package com.osrsdailytasks.service;

import java.util.Locale;
import java.util.Objects;
import com.osrsdailytasks.PvmDifficulty;
import net.runelite.client.hiscore.HiscoreSkill;

public final class BossDefinition
{
	private final HiscoreSkill hiscoreSkill;
	private final PvmDifficulty pvmDifficulty;
	private final String title;
	private final boolean trackerSupported;

	BossDefinition(
		HiscoreSkill hiscoreSkill,
		PvmDifficulty pvmDifficulty,
		String title,
		boolean trackerSupported)
	{
		this.hiscoreSkill = Objects.requireNonNull(hiscoreSkill, "hiscoreSkill");
		this.pvmDifficulty = pvmDifficulty;
		this.title = Objects.requireNonNull(title, "title");
		this.trackerSupported = trackerSupported;
	}

	public String getTaskId()
	{
		return "boss-" + hiscoreSkill.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
	}

	public String getSubjectId()
	{
		return hiscoreSkill.name();
	}

	public String getDisplayName()
	{
		return hiscoreSkill.getName();
	}

	public PvmDifficulty getPvmDifficulty()
	{
		if (pvmDifficulty == null)
		{
			throw new IllegalStateException("Boss is not classified: " + getSubjectId());
		}
		return pvmDifficulty;
	}

	public boolean isClassified()
	{
		return pvmDifficulty != null;
	}

	public String getTitle()
	{
		return title;
	}

	public boolean isTrackerSupported()
	{
		return trackerSupported;
	}
}
