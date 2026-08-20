package com.osrsdailytasks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(OsrsDailyTasksConfig.GROUP)
public interface OsrsDailyTasksConfig extends Config
{
	String GROUP = "osrsdailytasks";
	String DEVELOPMENT_SECTION = "developmentControlsSection";

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	@ConfigSection(
		name = "DEVELOPMENT CONTROLS — REMOVE BEFORE RELEASE",
		description = "Temporary controls for selecting and cancelling tasks during manual testing",
		position = 5,
		closedByDefault = true
	)
	String developmentControlsSection = DEVELOPMENT_SECTION;

	@ConfigItem(
		keyName = "difficulty",
		name = "Difficulty",
		description = "Controls the target range used when the next daily task is generated",
		position = 0
	)
	default TaskDifficulty difficulty()
	{
		return TaskDifficulty.NORMAL;
	}

	@ConfigItem(
		keyName = "pvmDifficulty",
		name = "Maximum PvM difficulty",
		description = "Limits boss tasks to encounters at or below this PvM tier when the next task is generated",
		position = 1
	)
	default PvmDifficulty pvmDifficulty()
	{
		return PvmDifficulty.ANY;
	}

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show task overlay",
		description = "Show the current daily task in a compact in-game overlay",
		position = 2
	)
	default boolean showOverlay()
	{
		return false;
	}

	@ConfigItem(
		keyName = "notifyOnCompletion",
		name = "Notify on completion",
		description = "Send a RuneLite notification when the daily task is completed",
		position = 3
	)
	default boolean notifyOnCompletion()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showRolloverTime",
		name = "Show rollover time",
		description = "Show when the next task becomes available in local time",
		position = 4
	)
	default boolean showRolloverTime()
	{
		return true;
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	@ConfigItem(
		keyName = "developmentControls",
		name = "Enable development controls",
		description = "REMOVE BEFORE RELEASE: show temporary task assignment and cancellation controls",
		position = 0,
		section = DEVELOPMENT_SECTION
	)
	default boolean developmentControls()
	{
		return false;
	}
}
