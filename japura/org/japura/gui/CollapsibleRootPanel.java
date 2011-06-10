package org.japura.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.UIManager;

/**
 * Copyright (C) 2008, 2009 Carlos Eduardo Leite de Andrade
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
public class CollapsibleRootPanel extends JPanel implements Scrollable{

  public static final Mode FILL = Mode.FILL;
  public static final Mode SCROLL_BAR = Mode.SCROLL_BAR;
  private Mode mode;
  private List<CollapsiblePanel> panels;
  private int maxWidth;
  private int maxHeight;
  private Insets margin;
  private int panelGap = 10;

  public CollapsibleRootPanel() {
	this(Mode.SCROLL_BAR);
  }

  public CollapsibleRootPanel(Mode mode) {
	this.mode = mode;
	setMargin(new Insets(10, 10, 10, 10));
	setBackground(Color.WHITE);
	panels = new ArrayList<CollapsiblePanel>();
	super.setLayout(null);
	if (mode.equals(FILL)) {
	  addComponentListener(new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e) {
		  adjustEndHeights();
		}
	  });
	}
  }

  @Override
  public Dimension getMinimumSize() {
	return getPreferredSize();
  }

  @Override
  public Dimension getPreferredSize() {
	Dimension dim = null;
	if (isPreferredSizeSet()) {
	  dim = super.getPreferredSize();
	} else {
	  dim = new Dimension();
	  for (int i = 0; i < panels.size(); i++) {
		CollapsiblePanel cp = panels.get(i);
		if (cp.isVisible()) {
		  Dimension cpDim = cp.getPreferredSize();

		  dim.width = Math.max(dim.width, cpDim.width);
		  dim.height += cpDim.height;
		  if (i < panels.size() - 1) {
			dim.height += panelGap;
		  }
		}
	  }

	  Insets insets = getInsets();
	  dim.width += insets.left + insets.right;
	  dim.height += insets.bottom + insets.top;

	  dim.width += margin.left + margin.right;
	  dim.height += margin.top + margin.bottom;
	}

	if (!(getParent() instanceof JViewport)) {
	  dim.width += UIManager.getInt("ScrollBar.width");
	  applyMax(dim);
	}
	return dim;
  }

  /**
   * Applies the maximum height and width to the dimension.
   * 
   * @param dim
   *          the dimension to be limited
   */
  private void applyMax(Dimension dim) {
	if (getMaxHeight() > 0) {
	  dim.height = Math.min(dim.height, getMaxHeight());
	}
	if (getMaxWidth() > 0) {
	  dim.width = Math.min(dim.width, getMaxWidth());
	}
  }

  @Override
  public void doLayout() {
	if (mode.equals(Mode.FILL)) {
	  adjustCalculatedHeights();
	}
	int x = margin.left;
	int y = margin.top;
	for (int i = 0; i < panels.size(); i++) {
	  CollapsiblePanel cp = panels.get(i);
	  if (cp.isVisible()) {
		int height = cp.getCalculatedHeight();

		cp.setBounds(x, y, getWidth() - (margin.left + margin.right), height);

		y += height;
		if (i < panels.size() - 1) {
		  y += panelGap;
		}
	  }
	}
  }

  @Override
  public void setLayout(LayoutManager mgr) {}

  @Override
  public Component add(Component comp, int index) {
	return null;
  }

  @Override
  public void add(Component comp, Object constraints, int index) {}

  @Override
  public void add(Component comp, Object constraints) {}

  @Override
  public Component add(Component comp) {
	return null;
  }

  @Override
  public Component add(String name, Component comp) {
	return null;
  }

  /**
   * Adds a {@link CollapsiblePanel} to the container. The order of the
   * components inside the container is from top to bottom.
   * 
   * @param collapsiblePanel
   *          the {@link CollapsiblePanel} to be added
   * @param weight
   *          the weight for the {@link CollapsiblePanel} in a fill mode.
   *          <P>
   *          Value 0 disables the fill, the height's component is defined for
   *          his preferred size.
   */
  public void add(CollapsiblePanel collapsiblePanel, int weight) {
	if (panels.contains(collapsiblePanel) == false) {
	  collapsiblePanel.weightY = Math.max(0, weight);
	  panels.add(collapsiblePanel);
	  super.add(collapsiblePanel);
	}
  }

  public void remove(CollapsiblePanel collapsiblePanel) {
	panels.remove(collapsiblePanel);
	super.remove(collapsiblePanel);
  }

  @Override
  public void removeAll() {
	panels.clear();
	super.removeAll();
  }

  void adjustEndHeights() {
	double total = 0;
	int visiblePanels = 0;
	for (CollapsiblePanel cp : panels) {
	  if (cp.isVisible()) {
		visiblePanels++;
		if (cp.isExpanded() || cp.isExpanding()) {
		  total += cp.weightY;
		}
	  }
	}
	int height = getHeight();
	height -= (margin.top + margin.bottom);
	height -= (Math.max(0, visiblePanels - 1) * panelGap);
	for (CollapsiblePanel cp : panels) {
	  if (cp.isVisible()) {
		if (cp.isCollapsed()) {
		  if (cp.weightY > 0) {
			height -= cp.getCalculatedHeight();
		  } else {
			height -= cp.getPreferredSize().height;
		  }
		} else if (cp.isCollapsing()) {
		  height -= cp.getCalculatedHeight();
		} else if (cp.isCollapsed() || cp.weightY == 0) {
		  height -= cp.getPreferredSize().height;
		}
	  }
	}

	for (CollapsiblePanel cp : panels) {
	  if (cp.isVisible() && (cp.isExpanding())) {
		if (cp.weightY > 0) {
		  double per = (100D * cp.weightY) / total;
		  double endHeight = (height * per) / 100D;
		  cp.setEndHeight((int) endHeight);
		} else {
		  cp.setEndHeight(cp.getPreferredSize(false).height);
		}
	  }
	}
  }

  private void adjustCalculatedHeights() {
	double total = 0;
	int visiblePanels = 0;
	for (CollapsiblePanel cp : panels) {
	  if (cp.isVisible()) {
		visiblePanels++;
		if (cp.isExpanded()) {
		  total += cp.weightY;
		}
	  }
	}
	int height = getHeight();
	height -= (margin.top + margin.bottom);
	height -= (Math.max(0, visiblePanels - 1) * panelGap);
	for (CollapsiblePanel cp : panels) {
	  if (cp.isVisible()) {
		if (cp.weightY > 0) {
		  if (cp.isExpanded() == false)
			height -= cp.getCalculatedHeight();
		} else {
		  if (cp.isCollapsed() || cp.isExpanded()) {
			height -= cp.getPreferredSize().height;
		  } else {
			height -= cp.getCalculatedHeight();
		  }
		}
	  }
	}

	for (CollapsiblePanel cp : panels) {
	  if (cp.isVisible()) {
		if (cp.isExpanded()) {
		  if (cp.weightY > 0) {
			double per = (100D * cp.weightY) / total;
			double endHeight = (height * per) / 100D;
			cp.setCalculatedHeight((int) endHeight);
		  } else {
			cp.setCalculatedHeight(cp.getPreferredSize(false).height);
		  }
		}
	  }
	}
  }

  /**
   * Adds a {@link CollapsiblePanel} to the container. The order of the
   * components inside the container is from top to bottom.
   * <P>
   * The {@link CollapsiblePanel} is added with weight value 1.
   * 
   * @param collapsiblePanel
   *          the {@link CollapsiblePanel} to be added
   */
  public void add(CollapsiblePanel collapsiblePanel) {
	add(collapsiblePanel, 1);
  }

  /**
   * Expands all panels inside the container.
   * 
   * @see #collapseAll()
   */
  public void expandAll() {
	for (CollapsiblePanel cp : panels) {
	  cp.expand();
	}
  }

  /**
   * Collapses all panels inside the container.
   * 
   * @see #expandAll()
   */
  public void collapseAll() {
	for (CollapsiblePanel cp : panels) {
	  cp.collapse();
	}
  }

  public void setLockAllEnabled(boolean enabled) {
	for (CollapsiblePanel cp : panels) {
	  cp.setLockEnabled(enabled);
	}
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
	Dimension dim = getPreferredSize();
	dim.width += UIManager.getInt("ScrollBar.width");
	applyMax(dim);
	return dim;
  }

  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect,
										 int orientation, int direction) {
	return 15;
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
	if (getParent() instanceof JViewport) {
	  JViewport vp = (JViewport) getParent();
	  if (vp.getHeight() > getPreferredSize().height) {
		return true;
	  }
	}
	return false;
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
	return true;
  }

  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
										int direction) {
	return 15;
  }

  public int getMaxWidth() {
	return maxWidth;
  }

  public void setMaxWidth(int maxWidth) {
	this.maxWidth = Math.max(maxWidth, 0);
  }

  public int getMaxHeight() {
	return maxHeight;
  }

  public void setMaxHeight(int maxHeight) {
	this.maxHeight = Math.max(maxHeight, 0);
  }

  public Mode getMode() {
	return mode;
  }

  public Insets getMargin() {
	return margin;
  }

  /**
   * Defines the margin.
   * 
   * @param margin
   *          {@link Insets}
   */
  public final void setMargin(Insets margin) {
	if (margin == null) {
	  margin = new Insets(0, 0, 0, 0);
	} else {
	  margin.bottom = Math.max(margin.bottom, 0);
	  margin.left = Math.max(margin.left, 0);
	  margin.top = Math.max(margin.top, 0);
	  margin.right = Math.max(margin.right, 0);
	}
	this.margin = margin;
  }

  public int getPanelGap() {
	return panelGap;
  }

  /**
   * Defines the gap between the panels.
   * 
   * @param panelGap
   *          the value for the gap
   */
  public void setPanelGap(int panelGap) {
	this.panelGap = Math.max(0, panelGap);
  }

  public static enum Mode {
	SCROLL_BAR,
	FILL;
  }

}
