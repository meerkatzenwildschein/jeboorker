package org.japura.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.japura.gui.model.PriorityComboBoxModel;

/**
 * Copyright (C) 2010 Carlos Eduardo Leite de Andrade
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
public class PriorityComboBoxRenderer extends JLabel implements
	ListCellRenderer, Serializable{

  protected static Border emptyBorder = new EmptyBorder(1, 1, 1, 1);
  protected static Border bottomLineBorder = BorderFactory.createMatteBorder(0,
	  0, 1, 0, Color.BLACK);
  protected static Border separatorBorder = BorderFactory.createCompoundBorder(
	  bottomLineBorder, emptyBorder);

  public PriorityComboBoxRenderer() {
	setOpaque(true);
	setBorder(emptyBorder);
  }

  @Override
  public Dimension getPreferredSize() {
	Dimension size = null;
	if ((getText() == null) || (getText().equals(""))) {
	  setText(" ");
	  size = super.getPreferredSize();
	  setText("");
	} else {
	  size = super.getPreferredSize();
	}
	return size;
  }

  protected void applyChangesForPriorityComboBox(JList list, Object value,
												 int index, boolean isSelected,
												 boolean cellHasFocus) {
	if (list.getModel() instanceof PriorityComboBoxModel) {
	  PriorityComboBoxModel model = (PriorityComboBoxModel) list.getModel();
	  if (model.isPriorityAvailable() == false) {
		return;
	  }

	  if (model.getPriorityItemsSize() > 0
		  && index == model.getPriorityItemsSize() - 1) {
		setBorder(separatorBorder);
	  } else {
		setBorder(emptyBorder);
	  }

	}
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value,
												int index, boolean isSelected,
												boolean cellHasFocus) {

	applyChangesForPriorityComboBox(list, value, index, isSelected,
		cellHasFocus);

	if (isSelected) {
	  setBackground(list.getSelectionBackground());
	  setForeground(list.getSelectionForeground());
	} else {
	  setBackground(list.getBackground());
	  setForeground(list.getForeground());
	}

	setFont(list.getFont());

	if (value instanceof Icon) {
	  setIcon((Icon) value);
	} else {
	  setText((value == null) ? "" : value.toString());
	}
	return this;
  }

}
