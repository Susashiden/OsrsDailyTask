package com.osrsdailytasks;

import com.google.inject.Provides;
import java.time.Clock;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import com.osrsdailytasks.model.TaskType;
import com.osrsdailytasks.notification.TaskCompletionNotifier;
import com.osrsdailytasks.service.DailyTaskService;
import com.osrsdailytasks.service.DailyTaskLoadCoordinator;
import com.osrsdailytasks.tracking.AgilityLapDetector;
import com.osrsdailytasks.tracking.BossCompletionDetector;
import com.osrsdailytasks.tracking.BrimhavenArenaTagDetector;
import com.osrsdailytasks.tracking.ClueScrollCompletionDetector;
import com.osrsdailytasks.tracking.ProgressUpdateResult;
import com.osrsdailytasks.tracking.TaskProgressService;
import com.osrsdailytasks.ui.DevelopmentTaskController;
import com.osrsdailytasks.ui.OsrsDailyTasksOverlayController;
import com.osrsdailytasks.ui.OsrsDailyTasksUiController;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "OSRS Daily Tasks",
	description = "Generates randomized daily goals and tracks their progress",
	tags = {"daily", "tasks", "goals", "progress"}
)
public class OsrsDailyTasksPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private DailyTaskService dailyTaskService;

	@Inject
	private DailyTaskLoadCoordinator dailyTaskLoadCoordinator;

	@Inject
	private TaskProgressService taskProgressService;

	@Inject
	private BossCompletionDetector bossCompletionDetector;

	@Inject
	private AgilityLapDetector agilityLapDetector;

	@Inject
	private BrimhavenArenaTagDetector brimhavenArenaTagDetector;

	@Inject
	private ClueScrollCompletionDetector clueScrollCompletionDetector;

	@Inject
	private OsrsDailyTasksUiController uiController;

	@Inject
	private OsrsDailyTasksOverlayController overlayController;

	@Inject
	private TaskCompletionNotifier completionNotifier;

	@Inject
	private DevelopmentTaskController developmentTaskController;

	@Override
	protected void startUp()
	{
		log.debug("OSRS Daily Tasks started");
		uiController.start();
		overlayController.start();
		developmentTaskController.start();
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			loadTask(true);
		}
	}

	@Override
	protected void shutDown()
	{
		developmentTaskController.stop();
		overlayController.stop();
		uiController.stop();
		brimhavenArenaTagDetector.reset();
		taskProgressService.resetTransientState();
		dailyTaskService.clearCachedTask();
		log.debug("OSRS Daily Tasks stopped");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			// Skill totals may not be synchronized yet. The first assigned-skill
			// StatChanged event establishes the persisted baseline without progress.
			loadTask(false);
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING)
		{
			brimhavenArenaTagDetector.reset();
			taskProgressService.resetTransientState();
			dailyTaskService.clearCachedTask();
			refreshUi();
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		brimhavenArenaTagDetector.reset();
		taskProgressService.resetTransientState();
		dailyTaskService.clearCachedTask();
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			loadTask(false);
		}
		else
		{
			refreshUi();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (OsrsDailyTasksConfig.GROUP.equals(event.getGroup()))
		{
			developmentTaskController.refreshVisibility();
			refreshUi();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		handleProgressResult(taskProgressService.recordXp(event.getSkill().name(), event.getXp()));

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			agilityLapDetector.findCompletion(event.getSkill(), localPlayer.getWorldLocation())
				.ifPresent(ignored -> recordAgilityLap());
		}
	}

	private void recordAgilityLap()
	{
		handleProgressResult(taskProgressService.recordDiscreteCompletion(
			TaskType.ACTIVITY,
			AgilityLapDetector.SUBJECT_ID,
			client.getTickCount()));
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Player localPlayer = client.getLocalPlayer();
		if (brimhavenArenaTagDetector.update(
			localPlayer == null ? null : localPlayer.getWorldLocation(),
			client.getHintArrowPoint()))
		{
			handleProgressResult(taskProgressService.recordDiscreteCompletion(
				TaskType.ACTIVITY,
				BrimhavenArenaTagDetector.SUBJECT_ID,
				client.getTickCount()));
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		bossCompletionDetector.findCompletion(event.getType(), event.getMessage())
			.ifPresent(completion ->
			handleProgressResult(taskProgressService.recordDiscreteCompletion(
				TaskType.BOSS,
				completion.getSubjectId(),
				completion.getKillCount())));

		clueScrollCompletionDetector.findCompletedTier(event.getType(), event.getMessage())
			.ifPresent(tier ->
			handleProgressResult(taskProgressService.recordDiscreteCompletion(
				TaskType.ACTIVITY,
				tier.getSubjectId(),
				client.getTickCount())));
	}

	private void loadTask(boolean initializeXpFromLoadedClient)
	{
		handleProgressResult(dailyTaskLoadCoordinator.load(initializeXpFromLoadedClient));
		refreshUi();
	}

	private void handleProgressResult(ProgressUpdateResult result)
	{
		if (result != ProgressUpdateResult.IGNORED)
		{
			refreshUi();
		}
		if (result == ProgressUpdateResult.COMPLETED)
		{
			dailyTaskService.getActiveTask().ifPresent(task ->
			{
				log.debug("Daily task completed: {}", task.getTaskId());
				completionNotifier.notifyCompletion(task);
			});
		}
	}

	private void refreshUi()
	{
		uiController.refresh();
		overlayController.refresh();
	}

	@Provides
	OsrsDailyTasksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(OsrsDailyTasksConfig.class);
	}

	@Provides
	@Singleton
	Clock provideClock()
	{
		return Clock.systemDefaultZone();
	}

	@Provides
	@Singleton
	Random provideRandom()
	{
		return new Random();
	}
}
