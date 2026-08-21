package com.osrsdailytasks.training.account;

import java.util.Objects;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

@Singleton
public class RuneLiteEhpProfileProvider
{
	private final Client client;

	@Inject
	public RuneLiteEhpProfileProvider(Client client)
	{
		this.client = Objects.requireNonNull(client, "client");
	}

	public Optional<EhpProfile> findCurrentProfile()
	{
		int accountTypeValue = client.getVarbitValue(VarbitID.IRONMAN);
		return JagexAccountType.fromVarbitValue(accountTypeValue)
			.map(JagexAccountType::getEhpProfile);
	}
}
