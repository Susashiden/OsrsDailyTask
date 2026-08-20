package com.osrsdailytasks.ui;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayManager;

@Singleton
public class RuneLiteOverlayHost implements OverlayHost
{
	private final OverlayManager overlayManager;

	@Inject
	public RuneLiteOverlayHost(OverlayManager overlayManager)
	{
		this.overlayManager = overlayManager;
	}

	@Override
	public void add(Overlay overlay)
	{
		overlayManager.add(overlay);
	}

	@Override
	public void remove(Overlay overlay)
	{
		overlayManager.remove(overlay);
	}
}
