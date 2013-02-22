package org.japura.gui.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

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
public class DefaultSplitButtonRenderer extends JLabel implements
	SplitButtonRenderer{

  private Border mouseOverBorder;
  private Border mouseOutBorder;

  @Override
  public Component getCellRendererComponent(String button,
											boolean cellHasFocus,
											boolean buttonEnabled) {
	setText(button);
	setOpaque(true);
	if (buttonEnabled == false) {
	  setBorder(getMouseOutBorder());
	  setBackground(Color.WHITE);
	  setForeground(Color.LIGHT_GRAY);
	} else if (cellHasFocus) {
	  setBorder(getMouseOverBorder());
	  setBackground(Color.BLACK);
	  setForeground(Color.WHITE);
	} else {
	  setBorder(getMouseOutBorder());
	  setBackground(Color.WHITE);
	  setForeground(Color.BLACK);
	}
	return this;
  }

  public Border getMouseOverBorder() {
	if (mouseOverBorder == null) {
	  mouseOverBorder = BorderFactory.createLineBorder(Color.BLACK, 3);
	}
	return mouseOverBorder;
  }

  public Border getMouseOutBorder() {
	if (mouseOutBorder == null) {
	  mouseOutBorder = BorderFactory.createLineBorder(Color.WHITE, 3);
	}
	return mouseOutBorder;
  }

}
