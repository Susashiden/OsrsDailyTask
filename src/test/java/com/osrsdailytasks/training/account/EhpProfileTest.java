package com.osrsdailytasks.training.account;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class EhpProfileTest
{
	@Test
	public void mapsEveryOfficialJagexAccountType()
	{
		for (JagexAccountType accountType : JagexAccountType.values())
		{
			assertEquals(accountType, JagexAccountType.fromVarbitValue(
				accountType.getVarbitValue()).orElse(null));
		}
		assertEquals(EhpProfile.MAIN, JagexAccountType.NORMAL.getEhpProfile());
		assertEquals(EhpProfile.IRONMAN, JagexAccountType.IRONMAN.getEhpProfile());
		assertEquals(EhpProfile.IRONMAN, JagexAccountType.HARDCORE_IRONMAN.getEhpProfile());
		assertEquals(EhpProfile.IRONMAN, JagexAccountType.GROUP_IRONMAN.getEhpProfile());
		assertEquals(EhpProfile.IRONMAN, JagexAccountType.HARDCORE_GROUP_IRONMAN.getEhpProfile());
		assertEquals(EhpProfile.IRONMAN, JagexAccountType.UNRANKED_GROUP_IRONMAN.getEhpProfile());
		assertEquals(EhpProfile.ULTIMATE, JagexAccountType.ULTIMATE_IRONMAN.getEhpProfile());
		assertEquals(false, JagexAccountType.fromVarbitValue(99).isPresent());
	}
}
