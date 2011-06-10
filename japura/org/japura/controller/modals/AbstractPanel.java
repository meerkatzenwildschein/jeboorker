package org.japura.controller.modals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.japura.gui.WrapLabel;
import org.japura.modal.Modal;

/**
 * Abstract standard panel for modal.
 * <P>
 * Copyright (C) 2009, 2010 Carlos Eduardo Leite de Andrade
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
public abstract class AbstractPanel extends JPanel{

  private WrapLabel titleLabel;
  private WrapLabel label;
  private int width = 300;
  private String text;
  private String title;

  public AbstractPanel(String title, String text) {
	this.title = title;
	if (text != null)
	  this.text = text;
	else
	  this.text = "";
	setName("modalPanel");
	Border out = BorderFactory.createLineBorder(Color.BLACK, 2);
	Border in = BorderFactory.createEmptyBorder(8, 8, 8, 8);
	setBorder(BorderFactory.createCompoundBorder(out, in));
	setBackground(Color.WHITE);
	setLayout(new BorderLayout(0, 5));
	if (title != null && title.length() > 0) {
	  add(getTitleLabel(), BorderLayout.NORTH);
	}
	add(getButtonsPanel(), BorderLayout.SOUTH);

	JPanel panel = new JPanel(new GridBagLayout());
	panel.setOpaque(false);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.anchor = GridBagConstraints.NORTH;
	gbc.weighty = 1;
	if (getIcon() != null) {
	  panel.add(new JLabel(getIcon()), gbc);
	}
	gbc.weightx = 1;
	gbc.insets = new Insets(0, 5, 0, 0);
	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	panel.add(getLabel(), gbc);

	JComponent content = getContent();
	if (content != null) {
	  gbc.gridx = 0;
	  gbc.gridy = 1;
	  if (getIcon() != null)
		gbc.gridwidth = 2;
	  panel.add(content, gbc);
	}
	add(panel, BorderLayout.CENTER);
  }

  protected abstract JPanel getButtonsPanel();

  protected abstract Icon getIcon();

  protected abstract JComponent getContent();

  /**
   * Obtém o label para o título do modal
   * 
   * @return {@link WrapLabel}
   */
  private WrapLabel getTitleLabel() {
	if (titleLabel == null) {
	  titleLabel = new WrapLabel();
	  titleLabel.setName("titleLabel");
	  if (title != null)
		titleLabel.setText(title);
	  titleLabel.setWrapWidth(width);
	  titleLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
		  Color.BLACK));
	}
	return titleLabel;
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

  protected final void setTextWrapWidth(int value) {
	width = value;
  }

  protected final int getTextWrapWidth() {
	return width;
  }

  protected final void closeModal() {
	Modal.closeModal(this);
  }

}
