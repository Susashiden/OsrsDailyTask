package com.osrsdailytasks.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.model.ActiveTask;
import com.osrsdailytasks.model.TaskDefinition;
import com.osrsdailytasks.service.DailyTaskService;
import com.osrsdailytasks.service.TaskCatalog;
import com.osrsdailytasks.tracking.TaskProgressService;
import com.osrsdailytasks.tracking.XpBaselineInitializer;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;

// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
@Singleton
public class DevelopmentTaskController
{
	private final OsrsDailyTasksPanel panel;
	private final UiExecutor uiExecutor;
	private final BooleanSupplier visibilitySupplier;
	private final List<DevelopmentTaskOption> taskOptions;
	private final Consumer<Runnable> clientActionExecutor;
	private final BooleanSupplier loggedInSupplier;
	private final Function<String, ActiveTask> assignmentAction;
	private final Runnable cancellationAction;
	private final Runnable transientStateReset;
	private final Runnable displayRefresh;
	private final Consumer<ActiveTask> xpBaselineInitializer;

	@Inject
	public DevelopmentTaskController(
		OsrsDailyTasksPanel panel,
		SwingUiExecutor uiExecutor,
		OsrsDailyTasksConfig config,
		DailyTaskService dailyTaskService,
		TaskProgressService taskProgressService,
		OsrsDailyTasksUiController uiController,
		OsrsDailyTasksOverlayController overlayController,
		TaskCatalog taskCatalog,
		Client client,
		ClientThread clientThread,
		XpBaselineInitializer xpBaselineInitializer)
	{
		this(
			panel,
			uiExecutor,
			config::developmentControls,
			createOptions(taskCatalog.getDefinitions()),
			action -> clientThread.invoke(action),
			() -> client.getGameState() == GameState.LOGGED_IN,
			dailyTaskService::assignForDevelopment,
			dailyTaskService::cancelForDevelopment,
			taskProgressService::resetTransientState,
			() ->
			{
				uiController.refresh();
				overlayController.refresh();
			},
			xpBaselineInitializer::initializeFromLoadedClient);
	}

	DevelopmentTaskController(
		OsrsDailyTasksPanel panel,
		UiExecutor uiExecutor,
		BooleanSupplier visibilitySupplier,
		List<DevelopmentTaskOption> taskOptions,
		Consumer<Runnable> clientActionExecutor,
		BooleanSupplier loggedInSupplier,
		Function<String, ActiveTask> assignmentAction,
		Runnable cancellationAction,
		Runnable transientStateReset,
		Runnable displayRefresh,
		Consumer<ActiveTask> xpBaselineInitializer)
	{
		this.panel = panel;
		this.uiExecutor = uiExecutor;
		this.visibilitySupplier = visibilitySupplier;
		this.taskOptions = taskOptions;
		this.clientActionExecutor = clientActionExecutor;
		this.loggedInSupplier = loggedInSupplier;
		this.assignmentAction = assignmentAction;
		this.cancellationAction = cancellationAction;
		this.transientStateReset = transientStateReset;
		this.displayRefresh = displayRefresh;
		this.xpBaselineInitializer = xpBaselineInitializer;
	}

	public void start()
	{
		uiExecutor.execute(() ->
		{
			panel.configureDevelopmentControls(taskOptions, this::requestAssignment, this::requestCancellation);
			panel.setDevelopmentControlsVisible(visibilitySupplier.getAsBoolean());
		});
	}

	public void stop()
	{
		uiExecutor.execute(panel::clearDevelopmentControls);
	}

	public void refreshVisibility()
	{
		boolean visible = visibilitySupplier.getAsBoolean();
		uiExecutor.execute(() -> panel.setDevelopmentControlsVisible(visible));
	}

	private void requestAssignment(String taskId)
	{
		clientActionExecutor.accept(() ->
		{
			if (!loggedInSupplier.getAsBoolean())
			{
				showStatus("Log in before assigning a development task.");
				return;
			}

			try
			{
				ActiveTask assignedTask = assignmentAction.apply(taskId);
				transientStateReset.run();
				xpBaselineInitializer.accept(assignedTask);
				displayRefresh.run();
				showStatus("Assigned: " + assignedTask.getTitle());
			}
			catch (IllegalArgumentException exception)
			{
				showStatus(exception.getMessage());
			}
		});
	}

	private void requestCancellation()
	{
		clientActionExecutor.accept(() ->
		{
			if (!loggedInSupplier.getAsBoolean())
			{
				showStatus("Log in before cancelling a development task.");
				return;
			}

			cancellationAction.run();
			transientStateReset.run();
			displayRefresh.run();
			showStatus("Current task cancelled. A later login may generate a new task.");
		});
	}

	private void showStatus(String message)
	{
		uiExecutor.execute(() -> panel.showDevelopmentStatus(message));
	}

	private static List<DevelopmentTaskOption> createOptions(List<TaskDefinition> definitions)
	{
		List<DevelopmentTaskOption> options = new ArrayList<>();
		for (TaskDefinition definition : definitions)
		{
			options.add(new DevelopmentTaskOption(definition));
		}
		return options;
	}
}
