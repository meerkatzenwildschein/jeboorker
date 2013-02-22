package org.japura.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;

import org.japura.gui.Gradient.Direction;
import org.japura.gui.event.CollapsiblePanelEvent;
import org.japura.gui.event.CollapsiblePanelListener;

/**
 * Copyright (C) 2008-2010 Carlos Eduardo Leite de Andrade
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
public class CollapsiblePanel extends JComponent{

  private static Icon defaultExpandIcon = new ImageIcon(Images.EXPAND_BLACK);
  private static Icon defaultCollapseIcon =
	  new ImageIcon(Images.COLLAPSE_BLACK);
  private static Object defaultTitleBackground = new Gradient(
	  Gradient.TOP_TO_BOTTOM, new Color(160, 190, 255),
	  new Color(240, 240, 255));

  private Icon iconDown;
  private Icon iconUp;
  private JLabel titleIcon;
  private JLabel titleLabel;
  private JLabel titleButton;
  private int titleSeparator = 1;
  private int titleMargin = 3;
  private int iconTitlePadding = 5;
  private int titleButtonsPadding = 5;
  private int buttonsPadding = 5;
  private JLayeredPane layeredPane;
  private JPanel modal;
  private Component component;
  private boolean collapsed;
  private boolean inAction;
  private boolean locked;
  private boolean animationEnabled = true;
  private transient ActionListener collapseListener;
  private transient ActionListener expandListener;
  private Timer collapseTimer;
  private Timer expandTimer;
  private int incremental = 3;
  private int pixels;
  private Object titleBackground;
  private Color separatorColor = new Color(0, 0, 0, 80);
  private JPanel titlePanel;
  private JPanel innerTitlePanel;
  private EventListenerList listeners;

  private JComponent[] extraButtons;
  private Icon icon;

  private int endHeight;
  private int calculatedHeight = -1;
  int weightY = 1;

  public CollapsiblePanel() {
	this(null, "", null);
  }

  public CollapsiblePanel(String title) {
	this(null, title, null);
  }

  public CollapsiblePanel(Icon icon, String title) {
	this(icon, title, null);
  }

  public CollapsiblePanel(String title, JComponent[] extraButtons) {
	this(null, title, extraButtons);
  }

  public CollapsiblePanel(Icon icon, String title, JComponent[] extraButtons) {
	listeners = new EventListenerList();
	modal = new JPanel();
	modal.setOpaque(false);
	modal.addMouseListener(new MouseAdapter() {});
	titleBackground = defaultTitleBackground;
	layeredPane = new JLayeredPane();
	layeredPane.add(modal, Integer.valueOf(JLayeredPane.MODAL_LAYER));
	super.add(layeredPane);
	setBorder(BorderFactory.createLineBorder(separatorColor));
	setLayout(null);
	setOpaque(false);
	this.icon = icon;
	setTitle(title);
	iconDown = CollapsiblePanel.defaultExpandIcon;
	iconUp = CollapsiblePanel.defaultCollapseIcon;

	titleButton = new JLabel();
	titleButton.setIcon(iconUp);

	super.add(getTitlePanel());
	this.extraButtons = extraButtons;
	buildTitlePanel();

	addMouseListener(new MouseAdapter() {
	  @Override
	  public void mouseClicked(MouseEvent e) {
		int y = getInnerTitlePanel().getY() - titleMargin;
		int max =
			getInnerTitlePanel().getY() + getInnerTitlePanel().getHeight()
				+ titleMargin;
		if (e.getPoint().y > y && e.getPoint().y < max) {
		  buttonAction();
		}
	  }
	});
  }

  public void removeExtraButtons() {
	this.extraButtons = null;
	buildTitlePanel();
  }

  public void setExtraButtons(JComponent[] extraButtons) {
	this.extraButtons = extraButtons;
	buildTitlePanel();
  }

  @Override
  public void doLayout() {
	Insets insets = getInsets();
	int x = insets.left;
	int y = 0;
	int width = getWidth() - (insets.left + insets.right);
	int height = getNorthPreferredHeight();
	getTitlePanel().setBounds(x, y, width, height);

	// component
	if (component != null) {
	  if (collapsed == false || inAction) {
		Dimension dim = component.getPreferredSize();
		x = insets.left;
		y += height + titleSeparator;
		height = getHeight() - y - insets.bottom;
		layeredPane.setBounds(x, y, width, height);

		y = Math.min(layeredPane.getHeight() - dim.height, 0);
		if (isFillMode()) {
		  component.setBounds(0, 0, width, height);
		} else {
		  component.setBounds(0, y, width, layeredPane.getHeight() - y);
		}

		if (inAction) {
		  modal.setBounds(0, y, width, layeredPane.getHeight() - y);
		} else {
		  modal.setBounds(0, 0, 0, 0);
		}
	  } else {
		layeredPane.setBounds(0, 0, 0, 0);
	  }
	}
  }

  @Override
  protected void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;

	Insets insets = getInsets();
	int y = getInnerTitlePanel().getY() - titleMargin;
	int height = getInnerTitlePanel().getHeight() + (2 * titleMargin);

	if (hasTitleBackgroundGradient()) {
	  GradientPaint gp = null;
	  Gradient gradient = (Gradient) getTitleBackground();
	  Direction direction = gradient.getDirection();
	  Color firstColor = gradient.getFirstColor();
	  Color secondColor = gradient.getSecondColor();

	  if (direction.equals(Direction.TOP_TO_BOTTOM)) {
		gp = new GradientPaint(0, y, firstColor, 0, y + height, secondColor);
	  } else if (direction.equals(Direction.BOTTOM_TO_TOP)) {
		gp = new GradientPaint(0, y, secondColor, 0, y + height, firstColor);
	  } else if (direction.equals(Direction.LEFT_TO_RIGHT)) {
		gp =
			new GradientPaint(insets.left, 0, firstColor, getWidth()
				- (insets.left + insets.right), 0, secondColor);
	  } else if (direction.equals(Direction.RIGHT_TO_LEFT)) {
		gp =
			new GradientPaint(insets.left, 0, secondColor, getWidth()
				- (insets.left + insets.right), 0, firstColor);
	  }
	  g2d.setPaint(gp);
	} else {
	  Color color = (Color) getTitleBackground();
	  g2d.setColor(color);
	}
	g2d.fillRect(insets.left, y, getWidth() - (insets.left + insets.right),
		height);

	y += height;
	g2d.setColor(getSeparatorColor());
	g2d.fillRect(insets.left, y, getWidth() - (insets.left + insets.right),
		titleSeparator);
  }

  @Override
  protected final void paintBorder(Graphics g) {
	Border border = getBorder();
	if (border != null) {
	  int y = getInnerTitlePanel().getY() - getInsets().top - titleMargin;
	  border.paintBorder(this, g, 0, y, getWidth(), getHeight() - y);
	}
  }

  @Override
  public final Dimension getPreferredSize() {
	if (isPreferredSizeSet()) {
	  return super.getPreferredSize();
	}
	return getPreferredSize(collapsed);
  }

  protected Dimension getPreferredSize(boolean collapsed) {
	Dimension dim = new Dimension(titlePanel.getPreferredSize());
	dim.height = getNorthPreferredHeight();
	Dimension cdim = null;
	if (component != null) {
	  cdim = component.getPreferredSize();
	} else {
	  cdim = new Dimension();
	}
	if (collapsed == false && component != null) {
	  dim.height += titleSeparator;
	  dim.height += cdim.height;
	}
	dim.width = Math.max(dim.width, cdim.width);
	Insets insets = getInsets();
	dim.width += insets.left + insets.right;
	dim.height += insets.bottom;

	return dim;
  }

  public void removeIcon() {
	this.icon = null;
	buildTitlePanel();
  }

  public void setIcon(Icon icon) {
	this.icon = icon;
	buildTitlePanel();
  }

  private JLabel getTitleLabel() {
	if (titleLabel == null) {
	  titleLabel = new JLabel();
	}
	return titleLabel;
  }

  private JPanel getTitlePanel() {
	if (titlePanel == null) {
	  titlePanel = new JPanel();
	  titlePanel.setOpaque(false);
	  titlePanel.setLayout(new GridBagLayout());
	}
	return titlePanel;
  }

  private void buildTitlePanel() {
	getTitlePanel().removeAll();
	titleIcon = null;

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.weighty = 1;
	gbc.anchor = GridBagConstraints.SOUTH;
	if (icon != null) {
	  titleIcon = new JLabel(icon);
	  gbc.insets =
		  new Insets(titleMargin, titleMargin, titleMargin, iconTitlePadding);
	  getTitlePanel().add(titleIcon, gbc);
	  gbc.insets = new Insets(titleMargin, 0, titleMargin, titleMargin);
	} else {
	  gbc.insets =
		  new Insets(titleMargin, titleMargin, titleMargin, titleMargin);
	}

	gbc.gridx = GridBagConstraints.RELATIVE;
	gbc.gridy = 0;
	gbc.weightx = 1;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	buildInnerTitlePanel();
	getTitlePanel().add(getInnerTitlePanel(), gbc);
	getTitlePanel().revalidate();
  }

  private void buildInnerTitlePanel() {
	getInnerTitlePanel().removeAll();
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = GridBagConstraints.RELATIVE;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.CENTER;
	gbc.weightx = 1;
	gbc.weighty = 1;
	gbc.insets = new Insets(0, 0, 0, titleButtonsPadding);
	getInnerTitlePanel().add(getTitleLabel(), gbc);
	gbc.weightx = 0;
	gbc.fill = GridBagConstraints.NONE;
	if (extraButtons != null) {
	  for (int i = 0; i < extraButtons.length; i++) {
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, buttonsPadding);
		getInnerTitlePanel().add(extraButtons[i], gbc);
	  }
	}
	gbc.gridx = GridBagConstraints.RELATIVE;
	gbc.gridy = 0;
	gbc.insets = new Insets(0, 0, 0, 0);
	getInnerTitlePanel().add(titleButton, gbc);
	getInnerTitlePanel().revalidate();
  }

  private JPanel getInnerTitlePanel() {
	if (innerTitlePanel == null) {
	  innerTitlePanel = new JPanel();
	  innerTitlePanel.setOpaque(false);
	  innerTitlePanel.setLayout(new GridBagLayout());
	}
	return innerTitlePanel;
  }

  private int getNorthPreferredHeight() {
	Insets insets = getInsets();

	int innerTitleHeight = getInnerTitlePanel().getPreferredSize().height;
	innerTitleHeight += (2 * titleMargin);

	int titleIconHeight = 0;
	if (titleIcon != null) {
	  titleIconHeight = titleIcon.getPreferredSize().height;
	  titleIconHeight += titleMargin + insets.top;
	} else {
	  innerTitleHeight += insets.top;
	}

	return Math.max(innerTitleHeight, titleIconHeight);
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

  /**
   * Determines whether the panel is collapsed.
   * 
   * @return true if the panel is collapsed, false otherwise
   * @see #isCollapsing()
   * @see #collapse()
   * @see #collapseImmediately()
   */
  public boolean isCollapsed() {
	if (collapsed && inAction == false) {
	  return true;
	}
	return false;
  }

  /**
   * Determines whether the panel is collapsing.
   * 
   * @return true if the panel is collapsing, false otherwise
   * @see #isCollapsed()
   * @see #collapse()
   * @see #collapseImmediately()
   */
  public boolean isCollapsing() {
	if (collapsed == false && inAction) {
	  return true;
	}
	return false;
  }

  /**
   * Determines whether the panel is expanded.
   * 
   * @return true if the panel is expanded, false otherwise
   * @see #isExpanding()
   * @see #expand()
   * @see #expandImmediately()
   */
  public boolean isExpanded() {
	if (collapsed == false && inAction == false) {
	  return true;
	}
	return false;
  }

  /**
   * Determines whether the panel is expanding.
   * 
   * @return true if the panel is expanding, false otherwise
   * @see #isExpanded()
   * @see #expand()
   * @see #expandImmediately()
   */
  public boolean isExpanding() {
	if (collapsed && inAction) {
	  return true;
	}
	return false;
  }

  /**
   * Determines whether the animation is enabled. Animation is enabled initially
   * by default. The animation may be enabled or disabled by calling its
   * setAnimationEnabled method.
   * 
   * @return true if the animation is enabled, false otherwise
   * @see #setAnimationEnabled(boolean)
   */
  public boolean isAnimationEnabled() {
	return animationEnabled;
  }

  public void setAnimationEnabled(boolean animationEnabled) {
	if (inAction == false) {
	  this.animationEnabled = animationEnabled;
	}
  }

  public void setSpeed(int speed) {
	incremental = Math.max(speed, 1);
  }

  public int getSpeed() {
	return incremental;
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
	}
  }

  private void setView(Component component) {
	removeView();
	if (component != null) {
	  this.component = component;
	  layeredPane.add(component, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
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

  private ActionListener getExpandListener() {
	if (expandListener == null) {
	  expandListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
		  calculatedHeight += pixels;
		  pixels += incremental;
		  revalidate();
		  if (calculatedHeight >= endHeight) {
			changeStatus();
		  }
		}
	  };
	}
	return expandListener;
  }

  private ActionListener getCollapseListener() {
	if (collapseListener == null) {
	  collapseListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
		  calculatedHeight -= pixels;
		  pixels += incremental;
		  revalidate();
		  if (calculatedHeight <= endHeight) {
			changeStatus();
		  }
		}
	  };
	}
	return collapseListener;
  }

  private void changeStatus() {
	if (collapsed) {
	  getExpandTimer().stop();
	  setIconUp();
	  calculatedHeight = -1;
	} else {
	  calculatedHeight = getCollapsedHeight();
	  getCollapseTimer().stop();
	  setIconDown();
	}
	revalidate();

	collapsed = !collapsed;
	inAction = false;

	CollapsiblePanelListener[] array =
		listeners.getListeners(CollapsiblePanelListener.class);
	for (CollapsiblePanelListener cpl : array) {
	  if (collapsed) {
		cpl.panelCollapsed(new CollapsiblePanelEvent(this));
	  } else {
		cpl.panelExpanded(new CollapsiblePanelEvent(this));
	  }
	}
  }

  private Timer getCollapseTimer() {
	if (collapseTimer == null) {
	  collapseTimer = new Timer(25, getCollapseListener());
	}
	return collapseTimer;
  }

  private Timer getExpandTimer() {
	if (expandTimer == null) {
	  expandTimer = new Timer(25, getExpandListener());
	}
	return expandTimer;
  }

  public boolean isLockEnabled() {
	return locked;
  }

  /**
   * Sets whether or not this collapse or expand option's of component is
   * enabled.
   * 
   * @param enabled
   *          true if these options should be enabled, false otherwise
   * @see #isLockEnabled()
   */
  public void setLockEnabled(boolean enabled) {
	this.locked = enabled;
	titleButton.setVisible(!enabled);
  }

  public void collapseImmediately() {
	collapse(true);
  }

  /**
   * Collapse this panel. This method will have no effect if the panel already
   * be expanding, locked or collapsed.
   * 
   * @see #collapseImmediately()
   * @see #setAnimationEnabled(boolean)
   * @see #isAnimationEnabled()
   */
  public void collapse() {
	collapse(false);
  }

  private int getCollapsedHeight() {
	Insets insets = getInsets();
	return getNorthPreferredHeight() + insets.bottom;
  }

  private void collapse(boolean immediately) {
	if (locked == false && inAction == false && collapsed == false) {
	  inAction = true;
	  CollapsiblePanelListener[] array =
		  listeners.getListeners(CollapsiblePanelListener.class);
	  for (CollapsiblePanelListener cpl : array) {
		cpl.panelWillCollapse(new CollapsiblePanelEvent(this));
	  }
	  pixels = incremental;
	  if (isAnimationEnabled() && immediately == false) {
		calculatedHeight = getSize().height;
		endHeight = getCollapsedHeight();
		getCollapseTimer().start();
	  } else {
		changeStatus();
	  }
	}
  }

  public void expandImmediately() {
	expand(true);
  }

  /**
   * Expand this panel. This method will have no effect if the panel already be
   * collapsing, locked or expanded.
   * 
   * @see #expandImmediately()
   * @see #setAnimationEnabled(boolean)
   * @see #isAnimationEnabled()
   */
  public void expand() {
	expand(false);
  }

  /**
   * Expand the panel.
   * 
   * @param immediately
   *          a boolean value, where true expand immediately the panel and false
   *          not.
   */
  private void expand(boolean immediately) {
	if (locked == false && inAction == false && collapsed) {
	  inAction = true;
	  CollapsiblePanelListener[] array =
		  listeners.getListeners(CollapsiblePanelListener.class);
	  for (CollapsiblePanelListener cpl : array) {
		cpl.panelWillExpand(new CollapsiblePanelEvent(this));
	  }
	  pixels = incremental;
	  if (isAnimationEnabled() && immediately == false) {
		calculatedHeight = getSize().height;
		if (isFillMode()) {
		  setEndHeights();
		} else {
		  endHeight = getPreferredSize(false).height;
		}
		getExpandTimer().start();
	  } else {
		changeStatus();
	  }
	}
  }

  /**
   * Check if parent component is a {@link CollapsibleRootPanel}
   * 
   * @return boolean
   */
  private boolean isCollapsiblePanelRootExists() {
	if (SwingUtilities.getAncestorOfClass(CollapsibleRootPanel.class, this) == null) {
	  return false;
	}
	return true;
  }

  /**
   * Action for collapse/expand button.
   */
  private void buttonAction() {
	if (isCollapsiblePanelRootExists() && locked == false && inAction == false) {
	  if (collapsed) {
		expand();
	  } else {
		collapse();
	  }
	}
  }

  public Color getSeparatorColor() {
	return separatorColor;
  }

  public void setSeparatorColor(Color separatorColor) {
	this.separatorColor = separatorColor;
  }

  @Override
  public Font getFont() {
	return getTitleLabel().getFont();
  }

  @Override
  public void setFont(Font font) {
	getTitleLabel().setFont(font);
  }

  public void setTitle(String title) {
	if (title == null) {
	  title = "";
	}
	getTitleLabel().setText(title);
  }

  public void setTitleForeground(Color color) {
	getTitleLabel().setForeground(color);
  }

  public Color getTitleForeground() {
	return getTitleLabel().getForeground();
  }

  public void setTitleBackground(Color color) {
	if (color != null) {
	  this.titleBackground = color;
	}
  }

  public void setTitleBackground(Gradient gradient) {
	if (gradient != null && gradient.getDirection() != null
		&& gradient.getFirstColor() != null
		&& gradient.getSecondColor() != null) {
	  this.titleBackground = gradient;
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

  public int getTitleMargin() {
	return titleMargin;
  }

  public void setTitleMargin(int titleMargin) {
	this.titleMargin = Math.max(0, titleMargin);
  }

  private void setIconDown() {
	titleButton.setIcon(iconDown);
  }

  private void setIconUp() {
	titleButton.setIcon(iconUp);
  }

  public void setIcons(Icon iconUp, Icon iconDown) {
	if (inAction == false) {
	  this.iconUp = iconUp;
	  this.iconDown = iconDown;
	  if (collapsed) {
		setIconDown();
	  } else {
		setIconUp();
	  }
	}
  }

  public void addCollapsiblePanelListener(CollapsiblePanelListener listener) {
	listeners.add(CollapsiblePanelListener.class, listener);
  }

  public void removeCollapsiblePanelListener(CollapsiblePanelListener listener) {
	listeners.remove(CollapsiblePanelListener.class, listener);
  }

  /**
   * Determines whether the panel is expanding or collapsing.
   * 
   * @return true if the panel is expanding or collapsing, false otherwise
   */
  public boolean isInAction() {
	return inAction;
  }

  private void setEndHeights() {
	Container ct = getParent();
	if (ct != null && ct instanceof CollapsibleRootPanel) {
	  CollapsibleRootPanel crp = (CollapsibleRootPanel) ct;
	  crp.adjustEndHeights();
	}
  }

  private boolean isFillMode() {
	Container ct = getParent();
	if (ct != null && ct instanceof CollapsibleRootPanel) {
	  CollapsibleRootPanel crp = (CollapsibleRootPanel) ct;
	  if (crp.getMode().equals(CollapsibleRootPanel.FILL)) {
		return true;
	  }
	}
	return false;
  }

  void setCalculatedHeight(int h) {
	this.calculatedHeight = h;
  }

  int getCalculatedHeight() {
	if (calculatedHeight == -1) {
	  calculatedHeight = getPreferredSize().height;
	}
	return calculatedHeight;
  }

  void setEndHeight(int endHeight) {
	this.endHeight = endHeight;
  }

  @Override
  public void invalidate() {
	if (inAction == false) {
	  calculatedHeight = -1;
	}
	super.invalidate();
  }

  public static void setDefaultExpandIcon(Icon icon) {
	if (icon == null) {
	  icon = new ImageIcon(Images.EXPAND_BLACK);
	}
	CollapsiblePanel.defaultExpandIcon = icon;
  }

  public static void setDefaultCollapseIcon(Icon icon) {
	if (icon == null) {
	  icon = new ImageIcon(Images.COLLAPSE_BLACK);
	}
	CollapsiblePanel.defaultCollapseIcon = icon;
  }

  public static void setDefaultBlackIcons() {
	defaultExpandIcon = new ImageIcon(Images.EXPAND_BLACK);
	defaultCollapseIcon = new ImageIcon(Images.COLLAPSE_BLACK);
  }

  public static void setDefaultWhiteIcons() {
	defaultExpandIcon = new ImageIcon(Images.EXPAND_WHITE);
	defaultCollapseIcon = new ImageIcon(Images.COLLAPSE_WHITE);
  }

  public static Object getDefaultTitleBackground() {
	return defaultTitleBackground;
  }

  public static void setDefaultTitleBackground(Color color) {
	if (color != null) {
	  CollapsiblePanel.defaultTitleBackground = color;
	}
  }

  public static void setDefaultTitleBackground(Gradient gradient) {
	if (gradient != null && gradient.getDirection() != null
		&& gradient.getFirstColor() != null
		&& gradient.getSecondColor() != null) {
	  CollapsiblePanel.defaultTitleBackground = gradient;
	}
  }

}
