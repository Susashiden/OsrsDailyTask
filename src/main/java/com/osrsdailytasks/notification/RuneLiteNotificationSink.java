package com.osrsdailytasks.notification;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.Notifier;

@Singleton
public class RuneLiteNotificationSink implements NotificationSink
{
	private final Notifier notifier;

	@Inject
	public RuneLiteNotificationSink(Notifier notifier)
	{
		this.notifier = notifier;
	}

	@Override
	public void notify(String message)
	{
		notifier.notify(message);
	}
}
