package org.japura.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Copyright (C) 2010-2011 Carlos Eduardo Leite de Andrade
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
public class TitlePanel extends JComponent{

  private static final long serialVersionUID = -2767246232683839619L;

  private static Object defaultTitleBackground = new Gradient(
	  Gradient.TOP_TO_BOTTOM, new Color(160, 190, 255),
	  new Color(240, 240, 255));

  private int borderY;
  private int titleSeparatorY;
  private PaintedPanel paintedPanel;
  private JLabel titleIcon;
  private JLabel titleLabel;
  private int titleSeparator = 1;
  private Insets titleMargin = new Insets(3, 3, 3, 3);
  private int iconAndTitleGap = 5;
  private int titleAndComponentsGap = 5;
  private int componentsGap = 5;
  private Component component;

  private Object titleBackground;
  private Color separatorColor = new Color(0, 0, 0, 80);

  private JComponent[] titleComponents;

  public TitlePanel(String title) {
	this(null, title, null);
  }

  public TitlePanel(Icon icon, String title) {
	this(icon, title, null);
  }

  public TitlePanel(String title, JComponent[] titleComponents) {
	this(null, title, titleComponents);
  }

  public TitlePanel(Icon icon, String title, JComponent[] titleComponents) {
	paintedPanel = new PaintedPanel();
	if (defaultTitleBackground instanceof Gradient) {
	  setTitleBackground((Gradient) defaultTitleBackground);
	} else if (defaultTitleBackground instanceof Color) {
	  setTitleBackground((Color) defaultTitleBackground);
	}

	titleBackground = defaultTitleBackground;

	setBorder(BorderFactory.createLineBorder(separatorColor));
	super.setLayout(new TitlePanelLayout());
	setOpaque(false);
	if (icon != null) {
	  this.titleIcon = new JLabel(icon);
	}
	if (title == null || title.length() == 0) {
	  title = " ";
	}
	this.titleLabel = new JLabel(title);

	this.titleComponents = titleComponents;

	build();
  }

  private void build() {
	super.add(paintedPanel);
	if (titleIcon != null) {
	  super.add(titleIcon);
	}
	super.add(titleLabel);
	if (titleComponents != null) {
	  for (JComponent titleComponent : titleComponents) {
		super.add(titleComponent);
	  }
	}
	updateZOrders();
  }

  private void updateZOrders() {
	int z = 0;
	if (titleIcon != null) {
	  setComponentZOrder(titleIcon, z++);
	}
	setComponentZOrder(titleLabel, z++);
	if (titleComponents != null) {
	  for (JComponent titleComponent : titleComponents) {
		setComponentZOrder(titleComponent, z++);
	  }
	}
	if (component != null) {
	  setComponentZOrder(component, z++);
	}
	setComponentZOrder(paintedPanel, z++);
  }

  @Override
  public final void setLayout(LayoutManager arg0) {}

  @Override
  protected void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	super.paintComponent(g2d);
	Insets insets = getInsets();
	if (titleSeparator > 0) {
	  g2d.setColor(getSeparatorColor());
	  g2d.fillRect(insets.left, titleSeparatorY, getWidth()
		  - (insets.left + insets.right), titleSeparator);
	}
  }

  @Override
  protected final void paintBorder(Graphics g) {
	Border border = getBorder();
	if (border != null) {
	  border
		  .paintBorder(this, g, 0, borderY, getWidth(), getHeight() - borderY);
	}
  }

  /**
   * Defines the title gaps
   * 
   * @param gap1
   *          gap between the icon and title
   * @param gap2
   *          gap between the title and title components
   * @param gap3
   *          gap between title components
   */
  public void setTitleGaps(int gap1, int gap2, int gap3) {
	iconAndTitleGap = Math.max(gap1, 0);
	titleAndComponentsGap = Math.max(gap2, 0);
	componentsGap = Math.max(gap3, 0);
  }

  public int getGapBetweenIconAndTitle() {
	return iconAndTitleGap;
  }

  public int getGapBetweenTitleAndTitleComponents() {
	return titleAndComponentsGap;
  }

  public int getGapBetweenTitleComponents() {
	return componentsGap;
  }

  /**
   * Creates a separator line border between title and view with the specified
   * width.
   * 
   * @param thickness
   *          an integer specifying the width in pixels
   */
  public void setSeparatorThickness(int thickness) {
	titleSeparator = Math.max(0, thickness);
  }

  @Override
  public void remove(Component arg0) {
	if (this.component.equals(arg0)) {
	  removeView();
	}
  }

  @Override
  public void remove(int arg0) {}

  @Override
  public void removeAll() {
	removeView();
  }

  private void removeView() {
	if (this.component != null) {
	  super.remove(this.component);
	  this.component = null;
	  updateZOrders();
	}
  }

  private void setView(Component component) {
	removeView();
	if (component != null) {
	  this.component = component;
	  super.add(component);
	  updateZOrders();
	}
  }

  @Override
  public Component add(Component comp, int index) {
	setView(comp);
	return comp;
  }

  @Override
  public void add(Component comp, Object constraints, int index) {
	setView(comp);
  }

  @Override
  public void add(Component comp, Object constraints) {
	setView(comp);
  }

  @Override
  public Component add(Component comp) {
	setView(comp);
	return comp;
  }

  @Override
  public Component add(String name, Component comp) {
	setView(comp);
	return comp;
  }

  public Color getSeparatorColor() {
	return separatorColor;
  }

  public void setSeparatorColor(Color separatorColor) {
	this.separatorColor = separatorColor;
  }

  @Override
  public Font getFont() {
	return titleLabel.getFont();
  }

  @Override
  public void setFont(Font font) {
	titleLabel.setFont(font);
  }

  public void setTitle(String title) {
	this.titleLabel.setText(title);
  }

  public void setTitleForeground(Color color) {
	titleLabel.setForeground(color);
  }

  public Color getTitleForeground() {
	return titleLabel.getForeground();
  }

  public void setTitleBackground(Color color) {
	if (color != null) {
	  this.titleBackground = color;
	  paintedPanel.removeBackgrounds();
	  paintedPanel.addBackground(color);
	}
  }

  public void setTitleBackground(Gradient gradient) {
	if (gradient != null && gradient.getDirection() != null
		&& gradient.getFirstColor() != null
		&& gradient.getSecondColor() != null) {
	  this.titleBackground = gradient;
	  paintedPanel.removeBackgrounds();
	  paintedPanel.addBackground(gradient);
	}
  }

  public Object getTitleBackground() {
	return titleBackground;
  }

  public boolean hasTitleBackgroundGradient() {
	if (titleBackground instanceof Gradient) {
	  return true;
	}
	return false;
  }

  public Insets getTitleMargin() {
	return (Insets) titleMargin.clone();
  }

  public void setTitleMargin(Insets titleMargin) {
	if (titleMargin == null) {
	  titleMargin = new Insets(0, 0, 0, 0);
	}
	int top = Math.max(titleMargin.top, 0);
	int left = Math.max(titleMargin.left, 0);
	int bottom = Math.max(titleMargin.bottom, 0);
	int right = Math.max(titleMargin.right, 0);
	this.titleMargin = new Insets(top, left, bottom, right);
  }

  public static Object getDefaultTitleBackground() {
	return defaultTitleBackground;
  }

  public static void setDefaultTitleBackground(Color color) {
	if (color != null) {
	  TitlePanel.defaultTitleBackground = color;
	}
  }

  public static void setDefaultTitleBackground(Gradient gradient) {
	if (gradient != null && gradient.getDirection() != null
		&& gradient.getFirstColor() != null
		&& gradient.getSecondColor() != null) {
	  TitlePanel.defaultTitleBackground = gradient;
	}
  }

  private class TitlePanelLayout implements LayoutManager{

	/**
	 * Preferred size of title label + extra components + title margin + title
	 * icon width
	 * 
	 * @return Dimension
	 */
	private Dimension getTitlePreferredSize() {
	  Dimension pdim = titleLabel.getPreferredSize();

	  if (titleIcon != null) {
		Dimension dim = titleIcon.getPreferredSize();
		pdim.width += dim.width;
		pdim.width += iconAndTitleGap;
	  }

	  if (titleComponents != null && titleComponents.length > 0) {
		pdim.width += titleAndComponentsGap;
		pdim.width += (componentsGap * (titleComponents.length - 1));
		for (JComponent titleComponent : titleComponents) {
		  Dimension dim = titleComponent.getPreferredSize();
		  pdim.width += dim.width;
		}
	  }

	  pdim.width += titleMargin.left + titleMargin.right;
	  pdim.height += titleMargin.top + titleMargin.bottom;
	  return pdim;
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
	  Dimension pdim = minimumLayoutSize(parent);

	  Insets insets = getInsets();
	  pdim.height += titleSeparator;

	  if (component != null) {
		Dimension dim = component.getPreferredSize();
		pdim.height += dim.height;

		pdim.width = Math.max(pdim.width, dim.width);
	  }

	  pdim.width += insets.left + insets.right;

	  return pdim;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
	  Dimension pdim = getTitlePreferredSize();

	  Insets insets = getInsets();
	  if (titleIcon != null) {
		Dimension dim = titleIcon.getPreferredSize();
		pdim.height =
			Math.max(pdim.height + insets.top, dim.height + titleMargin.bottom);
	  } else {
		pdim.height += insets.top;
	  }

	  pdim.height += insets.bottom;
	  return pdim;
	}

	@Override
	public void layoutContainer(Container parent) {
	  synchronized (getTreeLock()) {
		Dimension psize = parent.getSize();
		Dimension tdim = getTitlePreferredSize();
		Insets insets = getInsets();

		int h = tdim.height;
		int x = insets.left + titleMargin.left;

		if (titleIcon != null) {
		  Dimension dim = titleIcon.getPreferredSize();
		  h = Math.max(h + insets.top, dim.height + titleMargin.bottom);
		  titleIcon.setBounds(x, h - (dim.height + titleMargin.bottom),
			  dim.width, dim.height);
		  x += dim.width;
		  x += iconAndTitleGap;
		} else {
		  h += insets.top;
		}

		paintedPanel.setBounds(insets.left, h - tdim.height, psize.width
			- (insets.left + insets.right), tdim.height);
		borderY = h - (tdim.height + insets.top);

		int y = h - titleMargin.bottom;
		Dimension dim = titleLabel.getPreferredSize();
		titleLabel.setBounds(x, y - dim.height, dim.width, dim.height);

		if (titleComponents != null && titleComponents.length > 0) {
		  int tw = 0;
		  for (JComponent titleComponent : titleComponents) {
			Dimension cdim = titleComponent.getPreferredSize();
			tw += cdim.width;
		  }
		  tw += (componentsGap * (titleComponents.length - 1));

		  x =
			  Math.max(psize.width - (insets.right + tw + titleMargin.right), x
				  + dim.width);

		  for (JComponent titleComponent : titleComponents) {
			Dimension cdim = titleComponent.getPreferredSize();
			titleComponent
				.setBounds(x, y - dim.height, cdim.width, cdim.height);
			x += cdim.width + componentsGap;
		  }
		}

		if (component != null) {
		  y += titleMargin.bottom;
		  if (titleSeparator > 0) {
			titleSeparatorY = y;
			y += titleSeparator;
		  }

		  x = insets.left;
		  component.setBounds(x, y, psize.width - (insets.left + insets.right),
			  psize.height - (y + insets.bottom));
		}
	  }
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {}

	@Override
	public void removeLayoutComponent(Component comp) {}

  }

}
