package com.osrsdailytasks.notification;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaskCompletionNotifierTest
{
	@Test
	public void sendsEnabledCompletionExactlyOnce()
	{
		AtomicBoolean enabled = new AtomicBoolean(true);
		RecordingNotificationSink sink = new RecordingNotificationSink();
		TaskCompletionNotifier notifier = new TaskCompletionNotifier(config(enabled), sink);
		ActiveTask task = completedTask("hard-clue", "Complete hard clue scrolls", "2026-08-18T10:00:00Z");

		assertTrue(notifier.notifyCompletion(task));
		assertFalse(notifier.notifyCompletion(task));

		assertEquals(1, sink.messages.size());
		assertEquals("Daily task completed: Complete hard clue scrolls", sink.messages.get(0));
	}

	@Test
	public void disabledCompletionStaysSuppressedAfterConfigurationChanges()
	{
		AtomicBoolean enabled = new AtomicBoolean(false);
		RecordingNotificationSink sink = new RecordingNotificationSink();
		TaskCompletionNotifier notifier = new TaskCompletionNotifier(config(enabled), sink);
		ActiveTask firstTask = completedTask("first", "First task", "2026-08-18T10:00:00Z");

		assertFalse(notifier.notifyCompletion(firstTask));
		enabled.set(true);
		assertFalse(notifier.notifyCompletion(firstTask));
		assertTrue(notifier.notifyCompletion(
			completedTask("second", "Second task", "2026-08-18T11:00:00Z")));

		assertEquals(1, sink.messages.size());
		assertEquals("Daily task completed: Second task", sink.messages.get(0));
	}

	@Test
	public void ignoresTasksThatHaveNotCompleted()
	{
		RecordingNotificationSink sink = new RecordingNotificationSink();
		TaskCompletionNotifier notifier = new TaskCompletionNotifier(
			config(new AtomicBoolean(true)),
			sink);
		ActiveTask activeTask = new ActiveTask(
			LocalDate.of(2026, 8, 18),
			"active",
			TaskType.ACTIVITY,
			"TEST",
			"Active task",
			2,
			1,
			null,
			null,
			0);

		assertFalse(notifier.notifyCompletion(activeTask));
		assertTrue(sink.messages.isEmpty());
	}

	private static OsrsDailyTasksConfig config(AtomicBoolean enabled)
	{
		return new OsrsDailyTasksConfig()
		{
			@Override
			public boolean notifyOnCompletion()
			{
				return enabled.get();
			}
		};
	}

	private static ActiveTask completedTask(String id, String title, String completedAt)
	{
		return new ActiveTask(
			LocalDate.of(2026, 8, 18),
			id,
			TaskType.ACTIVITY,
			id.toUpperCase(),
			title,
			1,
			1,
			null,
			Instant.parse(completedAt),
			0);
	}

	private static final class RecordingNotificationSink implements NotificationSink
	{
		private final List<String> messages = new ArrayList<>();

		@Override
		public void notify(String message)
		{
			messages.add(message);
		}
	}
}
