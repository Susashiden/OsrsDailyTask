package com.osrsdailytasks.ui;

import net.runelite.client.ui.NavigationButton;

interface NavigationHost
{
	void add(NavigationButton navigationButton);

	void remove(NavigationButton navigationButton);
}
