package com.osrsdailytasks.ui;

import javax.inject.Singleton;
import javax.swing.SwingUtilities;

@Singleton
public class SwingUiExecutor implements UiExecutor
{
	@Override
	public void execute(Runnable action)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			action.run();
		}
		else
		{
			SwingUtilities.invokeLater(action);
		}
	}
}
