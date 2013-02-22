package org.japura.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.japura.gui.renderer.HorizontalLineFactory;
import org.japura.gui.renderer.HorizontalLineRenderer;

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
public class LabelSeparator extends JComponent{

  public final static Align LEFT = Align.LEFT;
  public final static Align RIGHT = Align.RIGHT;

  private JLabel label;
  private int leftSeparatorWidth = 10;
  private int rightSeparatorWidth = 30;
  private int separatorGap = 5;
  private Align align = Align.LEFT;
  private HorizontalLineRenderer lineRenderer;

  public LabelSeparator(String text) {
	this();
	label.setText(text);
  }

  public void setText(String text) {
	label.setText(text);
	revalidate();
  }

  public LabelSeparator() {
	setLayout(null);
	setOpaque(false);
	label = new JLabel();
	add(label);
	lineRenderer = HorizontalLineFactory.createLoweredEtchedLine();
  }

  @Override
  public final void setForeground(Color fg) {
	label.setForeground(fg);
  }

  @Override
  public final void setFont(Font font) {
	label.setFont(font);
  }

  @Override
  public final Font getFont() {
	return label.getFont();
  }

  @Override
  public final FontMetrics getFontMetrics(Font font) {
	return label.getFontMetrics(font);
  }

  public HorizontalLineRenderer getLineRenderer() {
	return lineRenderer;
  }

  public void setLineRenderer(HorizontalLineRenderer lineRenderer) {
	this.lineRenderer = lineRenderer;
  }

  public int getLeftSeparatorWidth() {
	return leftSeparatorWidth;
  }

  public final void setLeftSeparatorWidth(int leftSeparatorWidth) {
	this.leftSeparatorWidth = Math.max(0, leftSeparatorWidth);
  }

  public int getRightSeparatorWidth() {
	return rightSeparatorWidth;
  }

  public final void setRightSeparatorWidth(int rightSeparatorWidth) {
	this.rightSeparatorWidth = Math.max(0, rightSeparatorWidth);
  }

  public int getSeparatorGap() {
	return separatorGap;
  }

  public final void setSeparatorGap(int separatorGap) {
	this.separatorGap = Math.max(0, separatorGap);
  }

  public Align getAlign() {
	return align;
  }

  public void setAlign(Align align) {
	this.align = align;
  }

  @Override
  public Dimension getMinimumSize() {
	if (isMinimumSizeSet())
	  return super.getMinimumSize();

	boolean hasText = true;
	if (label.getText() == null || label.getText().length() == 0) {
	  hasText = false;
	}
	Dimension dim = label.getMinimumSize();
	if (leftSeparatorWidth > 0) {
	  dim.width += leftSeparatorWidth;
	  if (hasText) {
		dim.width += separatorGap;
	  }
	}
	if (rightSeparatorWidth > 0) {
	  dim.width += rightSeparatorWidth;
	  if (hasText) {
		dim.width += separatorGap;
	  }
	}
	return dim;
  }

  @Override
  public final void doLayout() {
	int x = 0;
	if (align.equals(Align.LEFT)) {
	  if (leftSeparatorWidth > 0) {
		x = leftSeparatorWidth + separatorGap;
	  }
	} else if (align.equals(Align.RIGHT)) {
	  x = getWidth();
	  Font font = label.getFont();
	  FontMetrics fm = label.getFontMetrics(font);
	  x -= fm.stringWidth(label.getText());
	  if (rightSeparatorWidth > 0) {
		x -= (rightSeparatorWidth + separatorGap);
	  }
	}
	label.setBounds(x, 0, getWidth(), getHeight());
  }

  @Override
  protected final void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;

	if (isOpaque()) {
	  g2d.setColor(getBackground());
	  g2d.fillRect(0, 0, getWidth(), getHeight());
	}

	if (lineRenderer == null)
	  return;

	boolean hasText = true;
	if (label.getText() == null || label.getText().length() == 0) {
	  hasText = false;
	}

	Font font = label.getFont();
	FontMetrics fm = label.getFontMetrics(font);
	int y = getHeight() / 2;

	if (hasText) {
	  int x = 0;
	  if (align.equals(Align.LEFT)) {
		if (leftSeparatorWidth > 0) {
		  x = leftSeparatorWidth;
		}
	  } else if (align.equals(Align.RIGHT)) {
		x = getWidth();
		x -= fm.stringWidth(label.getText());
		if (rightSeparatorWidth > 0) {
		  x -= separatorGap;
		  x -= rightSeparatorWidth;
		}
		if (getWidth() - x > 0) {
		  x -= separatorGap;
		}
	  }

	  if (x > 0) {
		lineRenderer.drawLine(g2d, 0, x, y);
		x += separatorGap;
	  }

	  x += separatorGap + fm.stringWidth(label.getText());

	  lineRenderer.drawLine(g2d, x, getWidth(), y);
	} else {
	  lineRenderer.drawLine(g2d, 0, getWidth(), y);
	}
  }

  @Override
  public final Dimension getPreferredSize() {
	if (isPreferredSizeSet()) {
	  return super.getPreferredSize();
	}
	boolean hasText = true;
	if (label.getText() == null || label.getText().length() == 0) {
	  hasText = false;
	}

	Dimension dim = label.getPreferredSize();
	if (leftSeparatorWidth > 0) {
	  dim.width += leftSeparatorWidth;
	  if (hasText) {
		dim.width += separatorGap;
	  }
	}

	if (rightSeparatorWidth > 0) {
	  dim.width += separatorGap + rightSeparatorWidth;
	  if (hasText) {
		dim.width += separatorGap;
	  }
	}
	return dim;
  }

  public static enum Align {
	LEFT,
	RIGHT;
  }

}