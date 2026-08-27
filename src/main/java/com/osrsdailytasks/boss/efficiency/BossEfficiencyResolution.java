package com.osrsdailytasks.boss.efficiency;

import java.util.Objects;
import java.util.Optional;
import com.osrsdailytasks.boss.model.BossDefinition;
import com.osrsdailytasks.boss.model.BossEfficiencyDefinition;
import com.osrsdailytasks.training.account.EhpProfile;

public final class BossEfficiencyResolution
{
	private final EhpProfile profile;
	private final BossDefinition boss;
	private final BossEfficiencyResolutionType type;
	private final BossEfficiencyDefinition efficiencyDefinition;

	private BossEfficiencyResolution(
		EhpProfile profile,
		BossDefinition boss,
		BossEfficiencyResolutionType type,
		BossEfficiencyDefinition efficiencyDefinition)
	{
		this.profile = Objects.requireNonNull(profile, "profile");
		this.boss = Objects.requireNonNull(boss, "boss");
		this.type = Objects.requireNonNull(type, "type");
		this.efficiencyDefinition = efficiencyDefinition;
		validate();
	}

	static BossEfficiencyResolution womRate(
		EhpProfile profile,
		BossDefinition boss,
		BossEfficiencyDefinition definition)
	{
		return new BossEfficiencyResolution(
			profile,
			boss,
			BossEfficiencyResolutionType.WOM_RATE,
			Objects.requireNonNull(definition, "definition"));
	}

	static BossEfficiencyResolution fallback(EhpProfile profile, BossDefinition boss)
	{
		return new BossEfficiencyResolution(
			profile,
			boss,
			BossEfficiencyResolutionType.PVM_TIER_FALLBACK,
			null);
	}

	static BossEfficiencyResolution unsupported(EhpProfile profile, BossDefinition boss)
	{
		return new BossEfficiencyResolution(
			profile,
			boss,
			BossEfficiencyResolutionType.TRACKER_UNSUPPORTED,
			null);
	}

	public EhpProfile getProfile()
	{
		return profile;
	}

	public BossDefinition getBoss()
	{
		return boss;
	}

	public BossEfficiencyResolutionType getType()
	{
		return type;
	}

	public Optional<BossEfficiencyDefinition> getEfficiencyDefinition()
	{
		return Optional.ofNullable(efficiencyDefinition);
	}

	private void validate()
	{
		if (type == BossEfficiencyResolutionType.WOM_RATE)
		{
			if (efficiencyDefinition == null
				|| efficiencyDefinition.getProfile() != profile
				|| !efficiencyDefinition.getSubjectId().equals(boss.getSubjectId())
				|| !boss.isTrackerSupported())
			{
				throw new IllegalArgumentException("WOM rate does not match the resolved boss");
			}
			return;
		}

		if (efficiencyDefinition != null)
		{
			throw new IllegalArgumentException("Non-WOM resolution must not contain a rate");
		}
		if (type == BossEfficiencyResolutionType.PVM_TIER_FALLBACK
			&& !boss.isTrackerSupported())
		{
			throw new IllegalArgumentException("Unsupported tracker cannot use a fallback");
		}
		if (type == BossEfficiencyResolutionType.TRACKER_UNSUPPORTED
			&& boss.isTrackerSupported())
		{
			throw new IllegalArgumentException("Supported tracker cannot be marked unsupported");
		}
	}
}
