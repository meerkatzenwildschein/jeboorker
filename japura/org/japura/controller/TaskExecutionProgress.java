package org.japura.controller;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.japura.gui.I18nStringKeys;
import org.japura.gui.WrapLabel;
import org.japura.i18n.I18nManager;
import org.japura.task.Task;
import org.japura.task.TaskManagerListener;

/**
 * <P>
 * Copyright (C) 2009-2011 Carlos Eduardo Leite de Andrade
 * <P>
 * This library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <P>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <P>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <A
 * HREF="www.gnu.org/licenses/">www.gnu.org/licenses/</A>
 * <P>
 * For more information, contact: <A HREF="www.japura.org">www.japura.org</A>
 * <P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 */
public class TaskExecutionProgress implements TaskManagerListener{

  private HashMap<Integer, TaskExecutionModalPanel> modalPanels;
  private Icon progressBarIcon;

  public TaskExecutionProgress() {
	modalPanels = new HashMap<Integer, TaskExecutionModalPanel>();
	URL url = getClass().getResource("/resources/images/jpr_progressbar.gif");
	progressBarIcon = new ImageIcon(url);
  }

  public TaskExecutionProgress(Icon progressBarIcon) {
	this.progressBarIcon = progressBarIcon;
  }

  @Override
  public void taskWillExecute(Integer taskGroupId, Task<?> task,
							  String taskMessage) {
	Controller<?> rootController = Controller.getRoot(taskGroupId);
	if (rootController != null) {
	  TaskExecutionModalPanel modalPanel = modalPanels.get(taskGroupId);
	  if (modalPanel == null) {
		modalPanel = new TaskExecutionModalPanel(taskMessage, progressBarIcon);
		modalPanels.put(taskGroupId, modalPanel);
		modalPanel.start(taskGroupId);
	  } else {
		modalPanel.setText(taskMessage);
	  }
	}
  }

  @Override
  public void executionsFinished(Integer taskGroupId) {
	TaskExecutionModalPanel modalPanel = modalPanels.get(taskGroupId);
	if (modalPanel != null) {
	  modalPanel.stop();
	  modalPanels.remove(taskGroupId);

	  Controller<?> rootController = Controller.getRoot(taskGroupId);
	  if (rootController != null) {
		rootController.closeModal(modalPanel);
	  }
	}
  }

  @Override
  public void taskExecuted(Integer taskGroupId, Task<?> task) {}

  private static class TaskExecutionModalPanel extends JPanel{

	private WrapLabel label;
	private JLabel timerLabel;
	private long elapsedStartTime = 0;
	private DecimalFormat integerTimeFormatter;
	private int width = 300;
	private String text;
	private StringBuilder textTimer;
	private Timer timer;
	private Timer starter;
	private boolean stop;

	/**
	 * Construtor
	 * 
	 * @param text
	 *          mensagem do modal
	 */
	public TaskExecutionModalPanel(String text, Icon progressBarIcon) {
	  textTimer = new StringBuilder();
	  integerTimeFormatter = new DecimalFormat("00");
	  this.text = text;

	  Border out = BorderFactory.createLineBorder(Color.BLACK, 2);
	  Border in = BorderFactory.createEmptyBorder(8, 8, 8, 8);
	  setBorder(BorderFactory.createCompoundBorder(out, in));
	  setBackground(Color.WHITE);
	  setLayout(new GridBagLayout());

	  GridBagConstraints gbc = new GridBagConstraints();
	  gbc.gridx = 0;
	  gbc.gridy = 0;
	  gbc.anchor = GridBagConstraints.NORTHWEST;
	  gbc.weighty = 1;
	  gbc.weightx = 1;
	  add(getLabel(), gbc);

	  gbc.insets = new Insets(20, 0, 0, 0);
	  gbc.gridx = 0;
	  gbc.gridy = 1;
	  gbc.anchor = GridBagConstraints.SOUTH;
	  gbc.weighty = 1;
	  gbc.weightx = 1;

	  add(getTimeLabel(), gbc);

	  gbc.insets = new Insets(0, 0, 0, 0);
	  gbc.gridx = 0;
	  gbc.gridy = 2;
	  gbc.anchor = GridBagConstraints.CENTER;
	  gbc.weightx = 1;

	  JLabel progressBar = new JLabel(progressBarIcon);
	  progressBar.setName("progressComponent");
	  add(progressBar, gbc);
	}

	/**
	 * Seta o texto para a mensagem
	 * 
	 * @param text
	 *          {@link String}
	 */
	public void setText(String text) {
	  getLabel().setText(text);
	}

	/**
	 * Obtém o label para o tempo gasto
	 * 
	 * @return {@link JLabel}
	 */
	private JLabel getTimeLabel() {
	  if (timerLabel == null) {
		timerLabel = new JLabel();
		timerLabel.setName("timerLabel");
		timerLabel.setText(getTimerString(0));
	  }
	  return timerLabel;
	}

	/**
	 * Obtém o {@link Timer} para atualizar os segundos gastos
	 * 
	 * @return {@link Timer}
	 */
	protected Timer getTimer() {
	  if (timer == null) {
		timer = new Timer(1000, new ActionListener() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			long seconds = System.currentTimeMillis() - elapsedStartTime;
			getTimeLabel().setText(getTimerString(seconds));
		  }
		});
	  }
	  return timer;
	}

	/**
	 * Inicializa o contador do tempo gasto
	 * 
	 * @param taskGroupId
	 */
	public void start(final Integer taskGroupId) {
	  elapsedStartTime = System.currentTimeMillis();
	  getTimer().start();
	  if (starter == null) {
		starter = new Timer(600, new ActionListener() {
		  @Override
		  public void actionPerformed(ActionEvent e) {
			if (stop) {
			  return;
			}

			Controller<?> controller =
				Controller.getRoot(taskGroupId);
			if (controller != null) {
			  controller.addModal(TaskExecutionModalPanel.this, null,
				  JLayeredPane.POPUP_LAYER + 1);
			}
		  }
		});
		starter.setRepeats(false);
		starter.start();
	  }
	}

	public void stop() {
	  stop = true;
	  if (starter != null) {
		starter.stop();
	  }
	  getTimer().stop();
	}

	/**
	 * Obtém o label para a mensagem do modal
	 * 
	 * @return {@link WrapLabel}
	 */
	private WrapLabel getLabel() {
	  if (label == null) {
		label = new WrapLabel(text);
		label.setWrapWidth(width);
		label.setName("messageLabel");
	  }
	  return label;
	}

	/**
	 * Seta o texto do tempo gasto.
	 * 
	 * @param seconds
	 *          a quantidade de segundos.
	 * @return String
	 */
	private String getTimerString(long seconds) {
	  if (seconds < 0) {
		return "-";
	  }
	  seconds = seconds / 1000L;

	  textTimer.setLength(0);
	  textTimer.append(I18nManager.getString(I18nStringKeys.TIME_ELAPSED
		  .getKey()));
	  textTimer.append(" ");

	  long sec = seconds;
	  long min = 0;
	  long hou = 0;
	  if (sec >= 60) {
		min = (int) sec / 60;
		sec = sec - (min * 60);
	  }
	  if (min >= 60) {
		hou = (int) min / 60;
		min = min - (hou * 60);
	  }
	  textTimer.append(integerTimeFormatter.format(hou));
	  textTimer.append(I18nManager.getString(I18nStringKeys.HOUR_ACRONYM
		  .getKey()));
	  textTimer.append(" ");
	  textTimer.append(integerTimeFormatter.format(min));
	  textTimer.append(I18nManager.getString(I18nStringKeys.MINUTE_ACRONYM
		  .getKey()));
	  textTimer.append(" ");
	  textTimer.append(integerTimeFormatter.format(sec));
	  textTimer.append(I18nManager.getString(I18nStringKeys.SECOND_ACRONYM
		  .getKey()));

	  return textTimer.toString();
	}

  }

}
