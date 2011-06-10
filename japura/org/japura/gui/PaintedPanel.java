package org.japura.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.LinkedHashMap;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.japura.gui.Gradient.Direction;

/**
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
 * 
 */
public class PaintedPanel extends JPanel{

  private static final long serialVersionUID = -4090857463216178976L;
  private LinkedHashMap<Object, Paint> paints =
	  new LinkedHashMap<Object, Paint>();

  public PaintedPanel() {
	super();
  }

  public PaintedPanel(boolean isDoubleBuffered) {
	super(isDoubleBuffered);
  }

  public PaintedPanel(LayoutManager layout, boolean isDoubleBuffered) {
	super(layout, isDoubleBuffered);
  }

  public PaintedPanel(LayoutManager layout) {
	super(layout);
  }

  public void removeBackgrounds() {
	paints.clear();
  }

  public void addBackground(Color color) {
	add(color, null);
  }

  public void removeBackground(Color color) {
	remove(color);
  }

  public void addBackground(Icon icon) {
	add(icon, null);
  }

  public void addBackground(Icon icon, Anchor anchor) {
	add(icon, anchor);
  }

  public void removeBackground(Icon icon) {
	remove(icon);
  }

  public void addBackground(Gradient gradient) {
	add(gradient, null);
  }

  public void removeBackground(Gradient gradient) {
	remove(gradient);
  }

  private void remove(Object background) {
	if (background != null) {
	  paints.remove(background);
	}
  }

  private void add(Object background, Anchor anchor) {
	if (background != null && paints.containsKey(background) == false) {
	  if (anchor == null) {
		anchor = Anchor.CENTER;
	  }
	  Paint paint = new Paint();
	  paint.anchor = anchor;
	  paint.background = background;
	  paints.put(background, paint);
	}
  }

  @Override
  protected void paintComponent(Graphics g) {
	super.paintComponent(g);
	for (Object key : paints.keySet()) {
	  Paint paint = paints.get(key);
	  if (paint.background instanceof Icon) {
		paintIcon(paint, g);
	  } else if (paint.background instanceof Gradient) {
		paintGradient((Gradient) paint.background, g);
	  } else if (paint.background instanceof Color) {
		paintColor((Color) paint.background, g);
	  }
	}
  }

  private void paintColor(Color color, Graphics g) {
	Insets insets = getInsets();

	Graphics2D g2d = (Graphics2D) g;

	int y = insets.top;
	int x = insets.left;
	int w = getWidth() - (insets.left + insets.right);
	int h = getHeight() - (insets.bottom + insets.top);

	g2d.setColor(color);
	g2d.fillRect(x, y, w, h);
  }

  private void paintGradient(Gradient gradient, Graphics g) {
	Insets insets = getInsets();

	Graphics2D g2d = (Graphics2D) g;
	GradientPaint gp = null;
	Direction direction = gradient.getDirection();
	Color firstColor = gradient.getFirstColor();
	Color secondColor = gradient.getSecondColor();

	int y = insets.top;
	int x = insets.left;
	int w = getWidth() - (insets.left + insets.right);
	int h = getHeight() - (insets.bottom + insets.top);

	if (direction.equals(Direction.TOP_TO_BOTTOM)) {
	  gp = new GradientPaint(0, y, firstColor, 0, h, secondColor);
	} else if (direction.equals(Direction.BOTTOM_TO_TOP)) {
	  gp = new GradientPaint(0, y, secondColor, 0, h, firstColor);
	} else if (direction.equals(Direction.LEFT_TO_RIGHT)) {
	  gp = new GradientPaint(x, 0, firstColor, w, 0, secondColor);
	} else if (direction.equals(Direction.RIGHT_TO_LEFT)) {
	  gp = new GradientPaint(x, 0, secondColor, w, 0, firstColor);
	}
	g2d.setPaint(gp);
	g2d.fillRect(x, y, w, h);
  }

  private void paintIcon(Paint paint, Graphics g) {
	Insets insets = getInsets();
	Icon icon = (Icon) paint.background;
	int w = icon.getIconWidth();
	int h = icon.getIconHeight();
	int x = 0;
	int y = 0;

	if (paint.anchor.equals(Anchor.CENTER)) {
	  x = (getWidth() - w) / 2;
	  y = (getHeight() - h) / 2;
	} else if (paint.anchor.equals(Anchor.NORTH)) {
	  x = (getWidth() - w) / 2;
	  y = insets.top;
	} else if (paint.anchor.equals(Anchor.NORTH_WEST)) {
	  x = insets.left;
	  y = insets.top;
	} else if (paint.anchor.equals(Anchor.NORTH_EAST)) {
	  x = getWidth() - w - insets.right;
	  y = insets.top;
	} else if (paint.anchor.equals(Anchor.SOUTH)) {
	  x = (getWidth() - w) / 2;
	  y = getHeight() - h - insets.bottom;
	} else if (paint.anchor.equals(Anchor.SOUTH_WEST)) {
	  x = insets.left;
	  y = getHeight() - h - insets.bottom;
	} else if (paint.anchor.equals(Anchor.SOUTH_EAST)) {
	  x = getWidth() - w - insets.right;
	  y = getHeight() - h - insets.bottom;
	} else if (paint.anchor.equals(Anchor.EAST)) {
	  x = getWidth() - w - insets.right;
	  y = (getHeight() - h) / 2;
	} else if (paint.anchor.equals(Anchor.WEST)) {
	  x = insets.left;
	  y = (getHeight() - h) / 2;
	}

	icon.paintIcon(this, g, x, y);
  }

  private static class Paint{
	private Object background;
	private Anchor anchor;
  }
}
