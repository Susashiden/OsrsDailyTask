package com.osrsdailytasks;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class OsrsDailyTasksPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(OsrsDailyTasksPlugin.class);
		RuneLite.main(args);
	}
}
