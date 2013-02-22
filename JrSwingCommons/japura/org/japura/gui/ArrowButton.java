package org.japura.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;

/**
 * <P>
 * Copyright (C) 2011 Carlos Eduardo Leite de Andrade
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
public class ArrowButton extends JComponent{
  public final static ArrowType UP = ArrowType.UP;
  public final static ArrowType DOWN = ArrowType.DOWN;
  public final static ArrowType LEFT = ArrowType.LEFT;
  public final static ArrowType RIGHT = ArrowType.RIGHT;
  public final static ArrowType DOUBLE_LEFT = ArrowType.DOUBLE_LEFT;
  public final static ArrowType DOUBLE_RIGHT = ArrowType.DOUBLE_RIGHT;
  public final static ArrowType DOUBLE_DOWN = ArrowType.DOUBLE_DOWN;
  public final static ArrowType DOUBLE_UP = ArrowType.DOUBLE_UP;
  private static final long serialVersionUID = -7746558416297550179L;
  private int arrowScale;
  private Dimension preferredSize;
  private GeneralPath gp;
  private Color disabledForeground;
  private Color mouseOverForeground;
  private ArrowType arrowType;
  private boolean mouseOver;

  public ArrowButton() {
	this(null, 12);
  }

  public ArrowButton(int arrowScale) {
	this(null, arrowScale);
  }

  public ArrowButton(ArrowType type) {
	this(type, 12);
  }

  public ArrowButton(ArrowType type, int arrowScale) {
	this.arrowScale = Math.max(5, arrowScale);
	setArrowType(type);
	disabledForeground = Color.LIGHT_GRAY;
	mouseOverForeground = new Color(90, 90, 90);
	addMouseListener(new MouseAdapter() {
	  @Override
	  public void mouseEntered(MouseEvent e) {
		mouseOver = true;
		repaint();
	  }

	  @Override
	  public void mouseExited(MouseEvent e) {
		mouseOver = false;
		repaint();
	  }

	  @Override
	  public void mousePressed(MouseEvent e) {
		doClick();
	  }
	});
  }

  public void doClick() {
	if (isEnabled()) {
	  fireActionPerformed();
	}
  }

  public int getArrowScale() {
	return arrowScale;
  }

  public ArrowType getArrowType() {
	return arrowType;
  }

  private int getShiftScale() {
	return (2 * getArrowScale()) / 3;
  }

  public void setArrowType(ArrowType arrowType) {
	if (arrowType == null) {
	  arrowType = ArrowType.DOUBLE_LEFT;
	}
	this.arrowType = arrowType;
	gp = new GeneralPath();
	if (arrowType.equals(ArrowType.DOUBLE_LEFT)) {
	  for (int x = 0; x < getArrowScale(); x += getShiftScale()) {
		gp.moveTo(x, getArrowScale() / 2);
		gp.lineTo(x + getArrowScale(), getArrowScale());
		gp.lineTo(x + getArrowScale(), 0);
		gp.lineTo(x, getArrowScale() / 2);
	  }
	} else if (arrowType.equals(ArrowType.LEFT)) {
	  gp.moveTo(0, getArrowScale() / 2);
	  gp.lineTo(getArrowScale(), getArrowScale());
	  gp.lineTo(getArrowScale(), 0);
	} else if (arrowType.equals(ArrowType.RIGHT)) {
	  gp.moveTo(0, 0);
	  gp.lineTo(0, getArrowScale());
	  gp.lineTo(getArrowScale(), getArrowScale() / 2);
	} else if (arrowType.equals(ArrowType.DOUBLE_RIGHT)) {
	  for (int x = 0; x < getArrowScale(); x += getShiftScale()) {
		gp.moveTo(x, 0);
		gp.lineTo(x, getArrowScale());
		gp.lineTo(x + getArrowScale(), getArrowScale() / 2);
		gp.lineTo(x, 0);
	  }
	} else if (arrowType.equals(ArrowType.UP)) {
	  gp.moveTo(getArrowScale() / 2, 0);
	  gp.lineTo(getArrowScale(), getArrowScale());
	  gp.lineTo(0, getArrowScale());
	} else if (arrowType.equals(ArrowType.DOUBLE_UP)) {
	  for (int y = 0; y < getArrowScale(); y += getShiftScale()) {
		gp.moveTo(getArrowScale() / 2, y);
		gp.lineTo(getArrowScale(), y + getArrowScale());
		gp.lineTo(0, y + getArrowScale());
	  }
	} else if (arrowType.equals(ArrowType.DOUBLE_DOWN)) {
	  for (int y = 0; y < getArrowScale(); y += getShiftScale()) {
		gp.moveTo(0, y);
		gp.lineTo(getArrowScale(), y);
		gp.lineTo(getArrowScale() / 2, y + getArrowScale());
	  }
	} else if (arrowType.equals(ArrowType.DOWN)) {
	  gp.moveTo(0, 0);
	  gp.lineTo(getArrowScale(), 0);
	  gp.lineTo(getArrowScale() / 2, getArrowScale());
	}
	gp.closePath();

	Rectangle bounds = gp.getBounds();
	preferredSize = new Dimension(bounds.width, bounds.height);
  }

  @Override
  public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g.create();
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

	if (isEnabled() == false) {
	  g2d.setColor(getDisabledForeground());
	} else if (mouseOver) {
	  g2d.setColor(getMouseOverForeground());
	} else {
	  g2d.setColor(getForeground());
	}

	g2d.fill(gp);
  }

  public void setDisabledForeground(Color color) {
	if (color != null) {
	  this.disabledForeground = color;
	}
  }

  public Color getMouseOverForeground() {
	return mouseOverForeground;
  }

  public void setMouseOverForeground(Color color) {
	if (color != null) {
	  this.mouseOverForeground = color;
	}
  }

  public Color getDisabledForeground() {
	return disabledForeground;
  }

  @Override
  public Dimension getPreferredSize() {
	return preferredSize;
  }

  public void addActionListener(ActionListener listener) {
	if (listener != null) {
	  listenerList.add(ActionListener.class, listener);
	}
  }

  public void removeActionListener(ActionListener listener) {
	if (listener != null) {
	  listenerList.remove(ActionListener.class, listener);
	}
  }

  /**
   * Returns an array of all the <code>ActionListener</code>s added to this
   * ArrowButton with addActionListener().
   * 
   * @return all of the <code>ActionListener</code>s added or an empty array if
   *         no listeners have been added
   */
  public ActionListener[] getActionListeners() {
	return (ActionListener[]) (listenerList.getListeners(ActionListener.class));
  }

  protected void fireActionPerformed() {
	ActionEvent event =
		new ActionEvent(ArrowButton.this, ActionEvent.ACTION_PERFORMED,
			"linkClicked", System.currentTimeMillis(), 0);

	ActionListener listeners[] =
		listenerList.getListeners(ActionListener.class);
	for (ActionListener listener : listeners) {
	  listener.actionPerformed(event);
	}
  }

  public static enum ArrowType {
	DOUBLE_LEFT,
	LEFT,
	DOUBLE_RIGHT,
	RIGHT,
	UP,
	DOWN,
	DOUBLE_UP,
	DOUBLE_DOWN;
  }

}
