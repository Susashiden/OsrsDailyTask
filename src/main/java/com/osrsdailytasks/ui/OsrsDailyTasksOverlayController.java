package com.osrsdailytasks.ui;

import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.service.DailyTaskService;

@Singleton
public class OsrsDailyTasksOverlayController
{
	private final OsrsDailyTasksOverlay overlay;
	private final OverlayHost overlayHost;
	private final Supplier<Optional<TaskUiModel>> modelSupplier;

	@Inject
	public OsrsDailyTasksOverlayController(
		OsrsDailyTasksOverlay overlay,
		RuneLiteOverlayHost overlayHost,
		DailyTaskService dailyTaskService,
		TaskUiModelFactory modelFactory)
	{
		this(
			overlay,
			overlayHost,
			() -> dailyTaskService.getActiveTask().map(modelFactory::create));
	}

	OsrsDailyTasksOverlayController(
		OsrsDailyTasksOverlay overlay,
		OverlayHost overlayHost,
		Supplier<Optional<TaskUiModel>> modelSupplier)
	{
		this.overlay = overlay;
		this.overlayHost = overlayHost;
		this.modelSupplier = modelSupplier;
	}

	public void start()
	{
		overlayHost.add(overlay);
		refresh();
	}

	public void stop()
	{
		overlayHost.remove(overlay);
		overlay.setModel(Optional.empty());
	}

	public void refresh()
	{
		overlay.setModel(modelSupplier.get());
	}
}
