package com.osrsdailytasks.task.eligibility;

import java.util.Objects;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.PvmDifficulty;
import com.osrsdailytasks.boss.catalog.HiscoreBossCatalog;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.model.TaskType;

@Singleton
public class DefaultTaskEligibilityPolicy implements TaskEligibilityPolicy
{
	private final HiscoreBossCatalog bossCatalog;
	private final Supplier<PvmDifficulty> pvmDifficultySupplier;

	@Inject
	public DefaultTaskEligibilityPolicy(
		HiscoreBossCatalog bossCatalog,
		OsrsDailyTasksConfig config)
	{
		this(bossCatalog, config::pvmDifficulty);
	}

	DefaultTaskEligibilityPolicy(
		HiscoreBossCatalog bossCatalog,
		Supplier<PvmDifficulty> pvmDifficultySupplier)
	{
		this.bossCatalog = Objects.requireNonNull(bossCatalog, "bossCatalog");
		this.pvmDifficultySupplier = Objects.requireNonNull(
			pvmDifficultySupplier,
			"pvmDifficultySupplier");
	}

	@Override
	public boolean isEligible(TaskDefinition definition)
	{
		Objects.requireNonNull(definition, "definition");
		if (definition.getType() != TaskType.BOSS)
		{
			return true;
		}

		PvmDifficulty configuredDifficulty = Objects.requireNonNull(
			pvmDifficultySupplier.get(),
			"configured PvM difficulty");
		return bossCatalog.findBySubjectId(definition.getSubjectId())
			.map(boss -> boss.isClassified()
				&& boss.isTrackerSupported()
				&& configuredDifficulty.allows(boss.getPvmDifficulty()))
			.orElse(false);
	}
}
