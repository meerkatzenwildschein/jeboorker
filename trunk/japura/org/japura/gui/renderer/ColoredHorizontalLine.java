package org.japura.gui.renderer;

import java.awt.Color;
import java.awt.Graphics2D;

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
 * </P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 */
public class ColoredHorizontalLine implements HorizontalLineRenderer{

  private Color color;
  private int thickness;

  public ColoredHorizontalLine(Color color, int thickness) {
	if (color == null) {
	  color = Color.BLACK;
	}
	if (thickness <= 0) {
	  thickness = 1;
	}
	this.color = color;
	this.thickness = thickness;
  }

  @Override
  public void drawLine(Graphics2D g2d, int x1, int x2, int y) {
	g2d.setColor(color);
	int r = thickness / 2;
	for (int i = 0; i < thickness; i++) {
	  g2d.drawLine(x1, y - r + i, x2, y - r + i);
	}
  }

}