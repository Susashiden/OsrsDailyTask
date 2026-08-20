package com.osrsdailytasks.ui;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Singleton
public class RuneLiteNavigationHost implements NavigationHost
{
	private final ClientToolbar clientToolbar;

	@Inject
	public RuneLiteNavigationHost(ClientToolbar clientToolbar)
	{
		this.clientToolbar = clientToolbar;
	}

	@Override
	public void add(NavigationButton navigationButton)
	{
		clientToolbar.addNavigation(navigationButton);
	}

	@Override
	public void remove(NavigationButton navigationButton)
	{
		clientToolbar.removeNavigation(navigationButton);
	}
}
