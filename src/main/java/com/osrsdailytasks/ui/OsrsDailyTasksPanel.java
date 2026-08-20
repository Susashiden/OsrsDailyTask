package com.osrsdailytasks.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Singleton
public class OsrsDailyTasksPanel extends PluginPanel
{
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu, HH:mm z", Locale.ENGLISH);

	private final JLabel titleLabel = new JLabel();
	private final JLabel categoryLabel = new JLabel();
	private final JLabel difficultyLabel = new JLabel();
	private final JLabel assignmentLabel = new JLabel();
	private final JLabel rolloverLabel = new JLabel();
	private final JLabel completionLabel = new JLabel();
	private final JProgressBar progressBar = new JProgressBar(0, 100);
	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	private final JPanel developmentPanel = new JPanel();
	private final JComboBox<DevelopmentTaskOption> developmentTaskSelector = new JComboBox<>();
	private final JButton assignDevelopmentTaskButton = new JButton("Assign selected task");
	private final JButton cancelDevelopmentTaskButton = new JButton("Cancel current task");
	private final JLabel developmentStatusLabel = new JLabel();

	public OsrsDailyTasksPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBackground(ColorScheme.DARK_GRAY_COLOR);
		content.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
		titleLabel.setForeground(Color.WHITE);
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		progressBar.setStringPainted(true);

		for (JLabel label : new JLabel[]{categoryLabel, difficultyLabel, assignmentLabel, rolloverLabel, completionLabel})
		{
			label.setForeground(Color.LIGHT_GRAY);
			label.setAlignmentX(Component.LEFT_ALIGNMENT);
		}
		completionLabel.setForeground(new Color(110, 220, 110));

		content.add(titleLabel);
		content.add(Box.createVerticalStrut(10));
		content.add(progressBar);
		content.add(Box.createVerticalStrut(10));
		content.add(categoryLabel);
		content.add(Box.createVerticalStrut(4));
		content.add(difficultyLabel);
		content.add(Box.createVerticalStrut(4));
		content.add(assignmentLabel);
		content.add(Box.createVerticalStrut(4));
		content.add(rolloverLabel);
		content.add(Box.createVerticalStrut(8));
		content.add(completionLabel);
		content.add(Box.createVerticalStrut(12));
		configureDevelopmentPanel();
		content.add(developmentPanel);

		add(content, BorderLayout.NORTH);
		showNoTask();
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	private void configureDevelopmentPanel()
	{
		developmentPanel.setLayout(new BoxLayout(developmentPanel, BoxLayout.Y_AXIS));
		developmentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		developmentPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(new Color(230, 150, 60)),
			"DEVELOPMENT — REMOVE BEFORE RELEASE"));

		for (Component component : new Component[]{
			developmentTaskSelector,
			assignDevelopmentTaskButton,
			cancelDevelopmentTaskButton,
			developmentStatusLabel})
		{
			if (component instanceof javax.swing.JComponent)
			{
				((javax.swing.JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
			}
		}
		developmentTaskSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
		assignDevelopmentTaskButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		cancelDevelopmentTaskButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		developmentStatusLabel.setForeground(new Color(230, 180, 90));

		developmentPanel.add(developmentTaskSelector);
		developmentPanel.add(Box.createVerticalStrut(6));
		developmentPanel.add(assignDevelopmentTaskButton);
		developmentPanel.add(Box.createVerticalStrut(4));
		developmentPanel.add(cancelDevelopmentTaskButton);
		developmentPanel.add(Box.createVerticalStrut(6));
		developmentPanel.add(developmentStatusLabel);
		developmentPanel.setVisible(false);
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	void configureDevelopmentControls(
		List<DevelopmentTaskOption> options,
		Consumer<String> assignmentAction,
		Runnable cancellationAction)
	{
		removeDevelopmentActionListeners();
		developmentTaskSelector.setModel(new DefaultComboBoxModel<>(
			options.toArray(new DevelopmentTaskOption[0])));
		assignDevelopmentTaskButton.addActionListener(event ->
		{
			DevelopmentTaskOption selected =
				(DevelopmentTaskOption) developmentTaskSelector.getSelectedItem();
			if (selected != null)
			{
				assignmentAction.accept(selected.getTaskId());
			}
		});
		cancelDevelopmentTaskButton.addActionListener(event -> cancellationAction.run());
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	void setDevelopmentControlsVisible(boolean visible)
	{
		developmentPanel.setVisible(visible);
		revalidate();
		repaint();
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	void showDevelopmentStatus(String message)
	{
		developmentStatusLabel.setText("<html>" + message + "</html>");
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE)
	void clearDevelopmentControls()
	{
		removeDevelopmentActionListeners();
		developmentTaskSelector.setModel(new DefaultComboBoxModel<>());
		developmentStatusLabel.setText("");
		setDevelopmentControlsVisible(false);
	}

	private void removeDevelopmentActionListeners()
	{
		for (ActionListener listener : assignDevelopmentTaskButton.getActionListeners())
		{
			assignDevelopmentTaskButton.removeActionListener(listener);
		}
		for (ActionListener listener : cancelDevelopmentTaskButton.getActionListeners())
		{
			cancelDevelopmentTaskButton.removeActionListener(listener);
		}
	}

	void showTask(TaskUiModel model, boolean showRolloverTime)
	{
		titleLabel.setText(model.getTitle());
		categoryLabel.setText("Category: " + model.getCategory());
		difficultyLabel.setText("Difficulty: " + displayName(model.getDifficulty().name()));
		assignmentLabel.setText("Assigned: " + DATE_FORMAT.format(model.getAssignmentDate()));
		rolloverLabel.setText("Next task: " + DATE_TIME_FORMAT.format(model.getNextRolloverAt()));
		rolloverLabel.setVisible(showRolloverTime);

		progressBar.setValue(model.getProgressPercentage());
		progressBar.setString(model.getProgressText() + " (" + model.getProgressPercentage() + "%)");

		completionLabel.setText(model.isComplete()
			? "Completed: " + DATE_TIME_FORMAT.format(model.getCompletedAt())
			: "In progress");
		completionLabel.setHorizontalAlignment(SwingConstants.LEFT);
	}

	void showNoTask()
	{
		titleLabel.setText("No active daily task");
		categoryLabel.setText("Log in to load today's task.");
		difficultyLabel.setText("");
		assignmentLabel.setText("");
		rolloverLabel.setText("");
		rolloverLabel.setVisible(false);
		completionLabel.setText("");
		progressBar.setValue(0);
		progressBar.setString("0 / 0 (0%)");
	}

	String getDisplayedTitle()
	{
		return titleLabel.getText();
	}

	String getDisplayedCategory()
	{
		return categoryLabel.getText();
	}

	String getDisplayedDifficulty()
	{
		return difficultyLabel.getText();
	}

	String getDisplayedAssignment()
	{
		return assignmentLabel.getText();
	}

	String getDisplayedRollover()
	{
		return rolloverLabel.getText();
	}

	boolean isRolloverVisible()
	{
		return rolloverLabel.isVisible();
	}

	String getDisplayedCompletion()
	{
		return completionLabel.getText();
	}

	String getDisplayedProgress()
	{
		return progressBar.getString();
	}

	int getDisplayedProgressPercentage()
	{
		return progressBar.getValue();
	}

	// DEVELOPMENT-ONLY (REMOVE BEFORE RELEASE): test accessors.
	boolean isDevelopmentControlsVisible()
	{
		return developmentPanel.isVisible();
	}

	int getDevelopmentTaskCount()
	{
		return developmentTaskSelector.getItemCount();
	}

	void selectDevelopmentTask(String taskId)
	{
		for (int index = 0; index < developmentTaskSelector.getItemCount(); index++)
		{
			DevelopmentTaskOption option = developmentTaskSelector.getItemAt(index);
			if (option.getTaskId().equals(taskId))
			{
				developmentTaskSelector.setSelectedIndex(index);
				return;
			}
		}
		throw new IllegalArgumentException("Unknown development task option: " + taskId);
	}

	void clickAssignDevelopmentTask()
	{
		assignDevelopmentTaskButton.doClick();
	}

	void clickCancelDevelopmentTask()
	{
		cancelDevelopmentTaskButton.doClick();
	}

	String getDevelopmentStatus()
	{
		return developmentStatusLabel.getText();
	}

	private static String displayName(String enumName)
	{
		return enumName.charAt(0) + enumName.substring(1).toLowerCase(Locale.ENGLISH);
	}
}
