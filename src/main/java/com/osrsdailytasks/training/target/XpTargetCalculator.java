package com.osrsdailytasks.training.target;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.TaskDifficulty;
import com.osrsdailytasks.training.account.EhpProfile;
import com.osrsdailytasks.training.account.RuneLiteEhpProfileProvider;
import com.osrsdailytasks.training.catalog.TrainingMethodCatalog;
import com.osrsdailytasks.training.model.TrainingMethod;

@Singleton
public class XpTargetCalculator
{
	private static final BigDecimal MINIMUM_NORMAL_FACTOR = new BigDecimal("0.375");
	private static final BigDecimal MAXIMUM_NORMAL_FACTOR = new BigDecimal("0.625");

	private final TrainingMethodCatalog catalog;
	private final Supplier<EhpProfile> profileSupplier;

	@Inject
	public XpTargetCalculator(
		TrainingMethodCatalog catalog,
		RuneLiteEhpProfileProvider profileProvider)
	{
		this(catalog, () -> profileProvider.findCurrentProfile().orElse(null));
	}

	public XpTargetCalculator(TrainingMethodCatalog catalog, EhpProfile profile)
	{
		this(catalog, () -> profile);
	}

	private XpTargetCalculator(TrainingMethodCatalog catalog, Supplier<EhpProfile> profileSupplier)
	{
		this.catalog = Objects.requireNonNull(catalog, "catalog");
		this.profileSupplier = Objects.requireNonNull(profileSupplier, "profileSupplier");
	}

	public XpTargetRange calculateCurrent(String skillId, TaskDifficulty difficulty)
	{
		return calculate(
			Objects.requireNonNull(profileSupplier.get(), "current EHP profile"),
			skillId,
			difficulty);
	}

	public boolean isCurrentProfileSupported()
	{
		return profileSupplier.get() != null;
	}

	public XpTargetRange calculate(
		EhpProfile profile,
		String skillId,
		TaskDifficulty difficulty)
	{
		Objects.requireNonNull(profile, "profile");
		Objects.requireNonNull(difficulty, "difficulty");
		TrainingMethod method = catalog.findTerminal(profile, skillId)
			.orElseThrow(() -> new IllegalStateException(
				"No positive EHP training method for " + profile + " " + skillId));

		return calculateForRate(method.getEhpRate(), difficulty);
	}

	public XpTargetRange calculateForRate(double ehpRate, TaskDifficulty difficulty)
	{
		if (!Double.isFinite(ehpRate) || ehpRate <= 0)
		{
			throw new IllegalArgumentException("ehpRate must be finite and positive");
		}
		Objects.requireNonNull(difficulty, "difficulty");
		BigDecimal rate = BigDecimal.valueOf(ehpRate);
		int minimum = difficulty.scaleTarget(rate.multiply(MINIMUM_NORMAL_FACTOR));
		int maximum = difficulty.scaleTarget(rate.multiply(MAXIMUM_NORMAL_FACTOR));
		return new XpTargetRange(minimum, maximum);
	}
}
