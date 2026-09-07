package com.osrsdailytasks.boss.target;

import java.util.Objects;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.boss.catalog.HiscoreBossCatalog;
import com.osrsdailytasks.boss.efficiency.BossEfficiencyResolution;
import com.osrsdailytasks.boss.efficiency.BossEfficiencyResolver;
import com.osrsdailytasks.boss.model.BossDefinition;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.account.RuneLiteEhpProfileProvider;

@Singleton
public class BossTargetService
{
	private final HiscoreBossCatalog bossCatalog;
	private final BossEfficiencyResolver efficiencyResolver;
	private final BossTargetCalculator calculator;
	private final Supplier<EhpProfile> profileSupplier;

	@Inject
	public BossTargetService(
		HiscoreBossCatalog bossCatalog,
		BossEfficiencyResolver efficiencyResolver,
		BossTargetCalculator calculator,
		RuneLiteEhpProfileProvider profileProvider)
	{
		this(
			bossCatalog,
			efficiencyResolver,
			calculator,
			() -> profileProvider.findCurrentProfile().orElse(null));
	}

	public BossTargetService(
		HiscoreBossCatalog bossCatalog,
		BossEfficiencyResolver efficiencyResolver,
		BossTargetCalculator calculator,
		EhpProfile profile)
	{
		this(bossCatalog, efficiencyResolver, calculator, () -> profile);
	}

	private BossTargetService(
		HiscoreBossCatalog bossCatalog,
		BossEfficiencyResolver efficiencyResolver,
		BossTargetCalculator calculator,
		Supplier<EhpProfile> profileSupplier)
	{
		this.bossCatalog = Objects.requireNonNull(bossCatalog, "bossCatalog");
		this.efficiencyResolver = Objects.requireNonNull(
			efficiencyResolver, "efficiencyResolver");
		this.calculator = Objects.requireNonNull(calculator, "calculator");
		this.profileSupplier = Objects.requireNonNull(profileSupplier, "profileSupplier");
	}

	public boolean isCurrentProfileSupported()
	{
		return profileSupplier.get() != null;
	}

	public BossTargetRange calculateCurrent(
		String subjectId,
		TaskDifficulty difficulty)
	{
		return calculate(
			Objects.requireNonNull(profileSupplier.get(), "current EHB profile"),
			subjectId,
			difficulty);
	}

	public BossTargetRange calculate(
		EhpProfile profile,
		String subjectId,
		TaskDifficulty difficulty)
	{
		Objects.requireNonNull(profile, "profile");
		Objects.requireNonNull(difficulty, "difficulty");
		BossDefinition boss = bossCatalog.findBySubjectId(subjectId)
			.orElseThrow(() -> new IllegalArgumentException(
				"Unknown boss subject: " + subjectId));
		BossEfficiencyResolution resolution = efficiencyResolver.resolve(profile, boss);
		switch (resolution.getType())
		{
			case WOM_RATE:
				return calculator.calculate(
					resolution.getEfficiencyDefinition().orElseThrow(
						() -> new IllegalStateException("WOM resolution has no rate")),
					difficulty);
			case PVM_TIER_FALLBACK:
				int minimum = difficulty.scaleTarget(
					boss.getPvmDifficulty().getMinimumTarget());
				int maximum = difficulty.scaleTarget(
					boss.getPvmDifficulty().getMaximumTarget());
				return new BossTargetRange(minimum, Math.max(minimum, maximum));
			case TRACKER_UNSUPPORTED:
				throw new IllegalArgumentException(
					"Boss tracker is unsupported: " + boss.getSubjectId());
			default:
				throw new IllegalStateException(
					"Unknown boss efficiency resolution: " + resolution.getType());
		}
	}
}
