package com.osrsdailytasks.training.account;

import java.util.Optional;

public enum JagexAccountType
{
	NORMAL(0, EhpProfile.MAIN),
	IRONMAN(1, EhpProfile.IRONMAN),
	ULTIMATE_IRONMAN(2, EhpProfile.ULTIMATE),
	HARDCORE_IRONMAN(3, EhpProfile.IRONMAN),
	GROUP_IRONMAN(4, EhpProfile.IRONMAN),
	HARDCORE_GROUP_IRONMAN(5, EhpProfile.IRONMAN),
	UNRANKED_GROUP_IRONMAN(6, EhpProfile.IRONMAN);

	private final int varbitValue;
	private final EhpProfile ehpProfile;

	JagexAccountType(int varbitValue, EhpProfile ehpProfile)
	{
		this.varbitValue = varbitValue;
		this.ehpProfile = ehpProfile;
	}

	public int getVarbitValue()
	{
		return varbitValue;
	}

	public EhpProfile getEhpProfile()
	{
		return ehpProfile;
	}

	public static Optional<JagexAccountType> fromVarbitValue(int value)
	{
		for (JagexAccountType accountType : values())
		{
			if (accountType.varbitValue == value)
			{
				return Optional.of(accountType);
			}
		}
		return Optional.empty();
	}
}
