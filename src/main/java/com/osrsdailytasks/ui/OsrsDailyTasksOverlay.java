package com.osrsdailytasks.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import com.osrsdailytasks.OsrsDailyTasksConfig;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
public class OsrsDailyTasksOverlay extends OverlayPanel
{
	private static final Color ACTIVE_COLOR = new Color(70, 170, 220);
	private static final Color COMPLETE_COLOR = new Color(70, 190, 100);

	private final OsrsDailyTasksConfig config;
	private volatile TaskUiModel model;

	@Inject
	public OsrsDailyTasksOverlay(OsrsDailyTasksConfig config)
	{
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setClearChildren(false);
		panelComponent.setPreferredSize(new Dimension(210, 0));
	}

	void setModel(Optional<TaskUiModel> model)
	{
		this.model = model.orElse(null);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		TaskUiModel currentModel = model;
		if (!config.showOverlay() || currentModel == null)
		{
			return null;
		}

		Color progressColor = currentModel.isComplete() ? COMPLETE_COLOR : ACTIVE_COLOR;
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(currentModel.getTitle())
			.color(Color.WHITE)
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left(currentModel.getCategory())
			.right(currentModel.isComplete() ? "Completed" : "In progress")
			.rightColor(progressColor)
			.build());

		ProgressBarComponent progressBar = new ProgressBarComponent();
		progressBar.setMinimum(0);
		progressBar.setMaximum(currentModel.getTargetAmount());
		progressBar.setValue(currentModel.getProgress());
		progressBar.setCenterLabel(currentModel.getProgressText());
		progressBar.setForegroundColor(progressColor);
		panelComponent.getChildren().add(progressBar);

		return super.render(graphics);
	}

	TaskUiModel getModel()
	{
		return model;
	}
}
