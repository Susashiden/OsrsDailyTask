package com.osrsdailytasks.ui;

import net.runelite.client.ui.overlay.Overlay;

interface OverlayHost
{
	void add(Overlay overlay);

	void remove(Overlay overlay);
}
