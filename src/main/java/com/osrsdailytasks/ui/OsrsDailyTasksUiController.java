package com.osrsdailytasks.ui;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import com.osrsdailytasks.OsrsDailyTasksPlugin;
import com.osrsdailytasks.service.DailyTaskService;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Singleton
public class OsrsDailyTasksUiController
{
	private final OsrsDailyTasksPanel panel;
	private final NavigationHost navigationHost;
	private final UiExecutor uiExecutor;
	private final Supplier<Optional<TaskUiModel>> modelSupplier;
	private final BooleanSupplier showRolloverTimeSupplier;
	private final NavigationButton navigationButton;

	@Inject
	public OsrsDailyTasksUiController(
		OsrsDailyTasksPanel panel,
		RuneLiteNavigationHost navigationHost,
		SwingUiExecutor uiExecutor,
		DailyTaskService dailyTaskService,
		TaskUiModelFactory modelFactory,
		OsrsDailyTasksConfig config)
	{
		this(
			panel,
			navigationHost,
			uiExecutor,
			() -> dailyTaskService.getActiveTask().map(modelFactory::create),
			config::showRolloverTime);
	}

	OsrsDailyTasksUiController(
		OsrsDailyTasksPanel panel,
		NavigationHost navigationHost,
		UiExecutor uiExecutor,
		Supplier<Optional<TaskUiModel>> modelSupplier,
		BooleanSupplier showRolloverTimeSupplier)
	{
		this.panel = panel;
		this.navigationHost = navigationHost;
		this.uiExecutor = uiExecutor;
		this.modelSupplier = modelSupplier;
		this.showRolloverTimeSupplier = showRolloverTimeSupplier;

		BufferedImage icon = ImageUtil.loadImageResource(OsrsDailyTasksPlugin.class, "task_icon.png");
		this.navigationButton = NavigationButton.builder()
			.tooltip("OSRS Daily Tasks")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();
	}

	public void start()
	{
		uiExecutor.execute(() -> navigationHost.add(navigationButton));
		refresh();
	}

	public void stop()
	{
		uiExecutor.execute(() ->
		{
			navigationHost.remove(navigationButton);
			panel.showNoTask();
		});
	}

	public void refresh()
	{
		Optional<TaskUiModel> model = modelSupplier.get();
		boolean showRolloverTime = showRolloverTimeSupplier.getAsBoolean();
		uiExecutor.execute(() ->
		{
			if (model.isPresent())
			{
				panel.showTask(model.get(), showRolloverTime);
			}
			else
			{
				panel.showNoTask();
			}
		});
	}

	NavigationButton getNavigationButton()
	{
		return navigationButton;
	}
}
