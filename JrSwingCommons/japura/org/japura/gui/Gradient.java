package org.japura.gui;

import java.awt.Color;
import java.io.Serializable;

/**
 * Copyright (C) 2008-2011 Carlos Eduardo Leite de Andrade
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
public class Gradient implements Serializable{

  public static final Direction LEFT_TO_RIGHT = Direction.LEFT_TO_RIGHT;
  public static final Direction RIGHT_TO_LEFT = Direction.RIGHT_TO_LEFT;
  public static final Direction TOP_TO_BOTTOM = Direction.TOP_TO_BOTTOM;
  public static final Direction BOTTOM_TO_TOP = Direction.BOTTOM_TO_TOP;

  private Direction direction;
  private Color firstColor;
  private Color secondColor;

  public Gradient(Color firstColor, Color secondColor) {
	this(Direction.TOP_TO_BOTTOM, firstColor, secondColor);
  }

  public Gradient(Direction direction, Color firstColor, Color secondColor) {
	this.direction = direction;
	this.firstColor = firstColor;
	this.secondColor = secondColor;
  }

  public Direction getDirection() {
	return direction;
  }

  public Color getFirstColor() {
	return firstColor;
  }

  public Color getSecondColor() {
	return secondColor;
  }

  public static enum Direction {
	LEFT_TO_RIGHT,
	RIGHT_TO_LEFT,
	TOP_TO_BOTTOM,
	BOTTOM_TO_TOP;
  }
}
