package org.japura.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.border.Border;

/**
 * Button with a tooltip function.
 * <P>
 * The tooltip text is wrapped with a defined width.
 * <P>
 * Copyright (C) 2009 Carlos Eduardo Leite de Andrade
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
public class ToolTipButton extends JPanel{

  private static Color DEFAULT_BACKGROUND = new Color(255, 255, 220);
  private Color borderColor = Color.BLACK;
  private Color toolTipBackground;
  private Icon image;
  private Icon imageMouseOver;
  private int toolTipWrapWidth = 300;
  private JLabel imageComponent;
  private Insets margin;
  private int borderThickness = 2;
  private Timer timer;
  private String text;

  /**
   * Constructor
   * 
   * @param image
   *          {@link Icon} - image for tooltip button
   */
  public ToolTipButton(Icon image) {
	this(image, null, null, DEFAULT_BACKGROUND);
  }

  /**
   * Constructor
   * 
   * @param image
   *          {@link Icon} - image for tooltip button
   * @param tooltip
   *          {@link String} - tooltip text
   */
  public ToolTipButton(Icon image, String tooltip) {
	this(image, null, tooltip, DEFAULT_BACKGROUND);
  }

  /**
   * Constructor
   * 
   * @param image
   *          {@link Icon} - image for tooltip button
   * @param imageMouseOver
   *          {@link Icon} - image for mouse over tooltip button
   * @param tooltip
   *          {@link String} - tooltip text
   */
  public ToolTipButton(Icon image, Icon imageMouseOver, String tooltip) {
	this(image, imageMouseOver, tooltip, DEFAULT_BACKGROUND);
  }

  /**
   * Constructor
   * 
   * @param image
   *          {@link Icon} - image for tooltip button
   * @param imageMouseOver
   *          {@link Icon} - image for mouse over tooltip button
   * @param tooltip
   *          {@link String} - tooltip text
   * @param toolTipBackground
   *          {@link Color} - tooltip background
   */
  public ToolTipButton(Icon image, Icon imageMouseOver, String tooltip,
	  Color toolTipBackground) {
	setTooltipMargin(null);
	setLayout(new GridBagLayout());

	imageComponent = new JLabel(image);
	add(imageComponent);

	this.image = image;
	this.imageMouseOver = imageMouseOver;
	this.toolTipBackground = toolTipBackground;
	setText(tooltip);

	ToolTipEvent tte = new ToolTipEvent();

	timer = new Timer(2000, tte);
	addMouseListener(tte);

	if (imageMouseOver != null) {
	  addMouseListener(new MouseAdapter() {
		@Override
		public void mouseEntered(MouseEvent e) {
		  imageComponent.setIcon(ToolTipButton.this.imageMouseOver);
		}

		@Override
		public void mouseExited(MouseEvent e) {
		  imageComponent.setIcon(ToolTipButton.this.image);
		}
	  });
	}
  }

  public String getText() {
	return text;
  }

  public void setText(String text) {
	if (text == null) {
	  text = "";
	}
	this.text = text;
  }

  public int getTooltipDelay() {
	return timer.getInitialDelay();
  }

  public void setTooltipDelay(int delay) {
	timer.setInitialDelay(delay);
  }

  public Insets getTooltipMargin() {
	return margin;
  }

  public void setTooltipMargin(Insets margin) {
	if (margin == null) {
	  margin = new Insets(10, 10, 10, 10);
	}
	margin.bottom = (Math.max(margin.bottom, 0));
	margin.top = (Math.max(margin.top, 0));
	margin.right = (Math.max(margin.right, 0));
	margin.left = (Math.max(margin.left, 0));
	this.margin = margin;
  }

  /**
   * Set the border width.
   * 
   * @param thickness
   *          an integer specifying the width in pixels
   */
  public void setTooltipBorderThickness(int thickness) {
	borderThickness = Math.max(0, thickness);
  }

  /**
   * Get the border width.
   * 
   * @return an integer specifying the width in pixels
   */
  public int getTooltipBorderThickness() {
	return borderThickness;
  }

  public Color getTooltipBorderColor() {
	return borderColor;
  }

  public void setTooltipBorderColor(Color borderColor) {
	this.borderColor = borderColor;
  }

  public Color getToolTipBackground() {
	return toolTipBackground;
  }

  public void setToolTipBackground(Color toolTipBackground) {
	this.toolTipBackground = toolTipBackground;
  }

  /**
   * Get the tooltip text wrap width.
   * 
   * @return int
   */
  public int getToolTipWrapWidth() {
	return toolTipWrapWidth;
  }

  /**
   * Set the tooltip text wrap width.
   * <P>
   * The minimal value is 200
   * 
   * @param toolTipWrapWidth
   *          int
   */
  public void setToolTipWrapWidth(int toolTipWrapWidth) {
	this.toolTipWrapWidth = Math.max(200, toolTipWrapWidth);
  }

  /**
   * ToolTip event.
   */
  private class ToolTipEvent extends MouseAdapter implements ActionListener{

	private JPopupMenu popup;

	/**
	 * Dispose the tooltip
	 */
	private void disposeWindow() {
	  if (popup != null) {
		popup.setVisible(false);
		popup = null;
	  }
	}

	/**
	 * Show the tooltip.
	 */
	private void showHint() {
	  disposeWindow();

	  WrapLabel tooltipLabel = new WrapLabel();
	  tooltipLabel.setFont(ToolTipButton.this.getFont());
	  tooltipLabel.setAlign(WrapLabel.Align.LEFT);
	  tooltipLabel.setWrapWidth(toolTipWrapWidth);
	  tooltipLabel.setText(text);

	  Border out = BorderFactory.createLineBorder(borderColor, borderThickness);
	  Border in =
		  BorderFactory.createEmptyBorder(margin.top, margin.left,
			  margin.bottom, margin.right);
	  Border border = BorderFactory.createCompoundBorder(out, in);
	  tooltipLabel.setBorder(border);
	  tooltipLabel.setBackground(toolTipBackground);
	  tooltipLabel.setOpaque(true);

	  popup = new JPopupMenu();
	  popup.setBorder(BorderFactory.createEmptyBorder());
	  popup.add(tooltipLabel);

	  popup.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent e) {
		  disposeWindow();
		}
	  });

	  popup.show(ToolTipButton.this, 0, getHeight());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	  timer.stop();
	  showHint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	  timer.start();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	  timer.stop();
	  showHint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
	  timer.stop();
	  if (popup != null) {
		if (popup.isShowing()) {
		  Point los = getLocationOnScreen();
		  Dimension dim = getSize();
		  Rectangle2D hbBounds =
			  new Rectangle2D.Double(los.getX(), los.getY(), dim.getWidth(),
				  dim.getHeight());

		  los = popup.getLocationOnScreen();
		  dim = popup.getSize();
		  Rectangle2D popupBounds =
			  new Rectangle2D.Double(los.getX(), los.getY(), dim.getWidth(),
				  dim.getHeight());

		  if (popupBounds.contains(hbBounds) == false) {
			disposeWindow();
		  }
		} else {
		  disposeWindow();
		}
	  }
	}
  }

}
