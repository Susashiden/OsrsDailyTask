package com.osrsdailytasks.notification;

import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.model.ActiveTask;

@Singleton
public class TaskCompletionNotifier
{
	private final OsrsDailyTasksConfig config;
	private final NotificationSink notificationSink;

	private String lastCompletionKey;

	@Inject
	public TaskCompletionNotifier(
		OsrsDailyTasksConfig config,
		RuneLiteNotificationSink notificationSink)
	{
		this(config, (NotificationSink) notificationSink);
	}

	TaskCompletionNotifier(OsrsDailyTasksConfig config, NotificationSink notificationSink)
	{
		this.config = Objects.requireNonNull(config, "config");
		this.notificationSink = Objects.requireNonNull(notificationSink, "notificationSink");
	}

	public boolean notifyCompletion(ActiveTask task)
	{
		Objects.requireNonNull(task, "task");
		if (!task.isComplete())
		{
			return false;
		}

		String completionKey = task.getGenerationDate()
			+ ":" + task.getTaskId()
			+ ":" + task.getSubjectId()
			+ ":" + task.getCompletedAt();
		if (completionKey.equals(lastCompletionKey))
		{
			return false;
		}
		lastCompletionKey = completionKey;

		if (!config.notifyOnCompletion())
		{
			return false;
		}

		notificationSink.notify("Daily task completed: " + task.getTitle());
		return true;
	}
}
