package com.osrsdailytasks.boss.efficiency;

import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.boss.catalog.BossEfficiencyCatalog;
import com.osrsdailytasks.boss.model.BossDefinition;
import com.osrsdailytasks.boss.model.BossEfficiencyDefinition;
import com.osrsdailytasks.training.account.EhpProfile;

@Singleton
public class BossEfficiencyResolver
{
	private final BossEfficiencyCatalog catalog;

	@Inject
	public BossEfficiencyResolver(BossEfficiencyCatalog catalog)
	{
		this.catalog = Objects.requireNonNull(catalog, "catalog");
	}

	public BossEfficiencyResolution resolve(EhpProfile profile, BossDefinition boss)
	{
		Objects.requireNonNull(profile, "profile");
		Objects.requireNonNull(boss, "boss");
		if (!boss.isTrackerSupported())
		{
			return BossEfficiencyResolution.unsupported(profile, boss);
		}

		BossEfficiencyDefinition definition = catalog.find(profile, boss.getSubjectId())
			.orElse(null);
		return definition == null
			? BossEfficiencyResolution.fallback(profile, boss)
			: BossEfficiencyResolution.womRate(profile, boss, definition);
	}
}
