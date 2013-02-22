package org.japura.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;

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
public class Decorator extends JLayeredPane{

  private static final long serialVersionUID = -3654610014150907569L;
  public static final Direction VERTICAL = Direction.VERTICAL;
  public static final Direction HORIZONTAL = Direction.HORIZONTAL;

  private LinkedHashMap<String, Component> decorations;
  private Component component;
  private Anchor anchor;
  private Direction direction;
  private Insets margin;
  private int gap = 3;

  public Decorator(Component component) {
	this(component, Anchor.SOUTH_EAST, Direction.HORIZONTAL);
  }

  public Decorator(Component component, Anchor anchor) {
	this(component, anchor, Direction.HORIZONTAL);
  }

  public Decorator(Component component, Anchor anchor, Direction direction) {
	decorations = new LinkedHashMap<String, Component>();
	this.component = component;
	this.anchor = anchor;
	this.direction = direction;
	this.margin = new Insets(0, 0, 0, 0);
	super.add(component);
	if (component instanceof JScrollPane) {
	  JScrollPane sp = (JScrollPane) component;
	  sp.getViewport().addComponentListener(new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent arg0) {
		  revalidate();
		}
	  });
	}
  }

  public final Direction getDirection() {
	return direction;
  }

  public final void setDecorationVisible(String id, boolean visibled) {
	if (decorations.containsKey(id)) {
	  decorations.get(id).setVisible(visibled);
	}
  }

  public final void setDecorationsVisible(boolean visibled) {
	for (String id : decorations.keySet()) {
	  decorations.get(id).setVisible(visibled);
	}
  }

  public final void addDecoration(String id, ImageIcon image) {
	put(id, new JLabel(image));
  }

  public final String addDecoration(ImageIcon image) {
	return addWithRandomID(new JLabel(image));
  }

  public final void addDecoration(String id, Component component) {
	put(id, component);
  }

  public final String addDecoration(Component component) {
	return addWithRandomID(component);
  }

  public final void removeDecoration(String id) {
	if (decorations.containsKey(id)) {
	  Component comp = decorations.remove(id);
	  super.remove(comp);
	}
  }

  public final void removeDecorations() {
	for (Component c : decorations.values()) {
	  super.remove(c);
	}
	decorations.clear();
  }

  private void put(String key, Component component) {
	if (key != null && decorations.containsKey(key) == false) {
	  decorations.put(key, component);
	  super.add(component, Integer.valueOf(JLayeredPane.MODAL_LAYER - 1));
	}
  }

  public Component getComponent() {
	return component;
  }

  public Insets getMargin() {
	return margin;
  }

  public Anchor getAnchor() {
	return anchor;
  }

  public final void setMargin(Insets margin) {
	if (margin != null) {
	  margin.bottom = Math.max(0, margin.bottom);
	  margin.left = Math.max(0, margin.left);
	  margin.right = Math.max(0, margin.right);
	  margin.top = Math.max(0, margin.top);
	  this.margin = margin;
	} else {
	  this.margin = new Insets(0, 0, 0, 0);
	}
  }

  @Override
  public Dimension getMinimumSize() {
	return getPreferredSize();
  }

  @Override
  public final Dimension getPreferredSize() {
	Dimension dim = component.getPreferredSize();
	dim.height += margin.bottom + margin.top;
	dim.width += margin.left + margin.right;
	return dim;
  }

  @Override
  public final Component add(Component comp, int index) {
	addWithRandomID(comp);
	return null;
  }

  @Override
  public final void add(Component comp, Object constraints, int index) {
	addWithRandomID(comp);
  }

  @Override
  public final void add(Component comp, Object constraints) {
	addWithRandomID(comp);
  }

  @Override
  public final Component add(Component comp) {
	addWithRandomID(comp);
	return null;
  }

  @Override
  public final Component add(String name, Component comp) {
	addWithRandomID(comp);
	return null;
  }

  private String addWithRandomID(Component comp) {
	Random random = new Random();
	String id = Long.toString(random.nextLong());
	while (decorations.containsKey(id)) {
	  id = Long.toString(random.nextLong());
	}
	addDecoration(id, comp);
	return id;
  }

  @Override
  public final void remove(Component comp) {}

  public final void removeAll() {}

  @Override
  public final void doLayout() {
	Dimension dim = getSize();
	component.setBounds(margin.left, margin.top, dim.width
		- (margin.left + margin.right), dim.height
		- (margin.bottom + margin.top));
	if (component instanceof JScrollPane) {
	  JScrollPane sp = (JScrollPane) component;

	  dim = sp.getSize();
	  if (sp.getVerticalScrollBar().isShowing()) {
		dim.width -= sp.getVerticalScrollBar().getSize().width;
	  }
	  if (sp.getHorizontalScrollBar().isShowing()) {
		dim.height -= sp.getHorizontalScrollBar().getSize().height;
	  }
	}

	if (direction.equals(Direction.HORIZONTAL)) {
	  int count = 0;
	  int width = 0;
	  for (Component c : decorations.values()) {
		if (c.isVisible()) {
		  Dimension cdim = c.getPreferredSize();
		  width += cdim.width;
		  count++;
		}
	  }
	  width += gap * (count - 1);

	  if (anchor.equals(Anchor.CENTER)) {
		int x = (dim.width - width) / 2;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int y = (dim.height - cdim.height) / 2;
			c.setBounds(x, y, cdim.width, cdim.height);
			x += (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.NORTH)) {
		int x = (dim.width - width) / 2;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(x, 0, cdim.width, cdim.height);
			x += (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.SOUTH)) {
		int x = (dim.width - width) / 2;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(x, dim.height - cdim.height - 1, cdim.width,
				cdim.height);
			x += (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.EAST)) {
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int x = dim.width - width - 1;
			int y = (dim.height - cdim.height) / 2;
			c.setBounds(x, y, cdim.width, cdim.height);
			width -= (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.NORTH_EAST)) {
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int x = dim.width - width - 1;
			c.setBounds(x, 0, cdim.width, cdim.height);
			width -= (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.SOUTH_EAST)) {
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int x = dim.width - width - 1;
			int y = dim.height - cdim.height - 1;
			c.setBounds(x, y, cdim.width, cdim.height);
			width -= (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.WEST)) {
		int x = 0;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int y = (dim.height - cdim.height) / 2;
			c.setBounds(x, y, cdim.width, cdim.height);
			x += (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.NORTH_WEST)) {
		int x = 0;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(x, 0, cdim.width, cdim.height);
			x += (gap + cdim.width);
		  }
		}
	  } else if (anchor.equals(Anchor.SOUTH_WEST)) {
		int x = 0;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(x, dim.height - cdim.height - 1, cdim.width,
				cdim.height);
			x += (gap + cdim.width);
		  }
		}
	  }
	} else {
	  int count = 0;
	  int height = 0;
	  for (Component c : decorations.values()) {
		if (c.isVisible()) {
		  Dimension cdim = c.getPreferredSize();
		  height += cdim.height;
		  count++;
		}
	  }
	  height += gap * (count - 1);

	  if (anchor.equals(Anchor.SOUTH_EAST)) {
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(dim.width - cdim.width - 1, dim.height - height - 1,
				cdim.width, cdim.height);
			height -= (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.EAST)) {
		int y = (dim.height - height) / 2;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(dim.width - cdim.width - 1, y, cdim.width, cdim.height);
			y += (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.NORTH_EAST)) {
		int y = 0;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(dim.width - cdim.width - 1, y, cdim.width, cdim.height);
			y += (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.SOUTH)) {
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int x = (dim.width - cdim.width) / 2;
			c.setBounds(x, dim.height - height - 1, cdim.width, cdim.height);
			height -= (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.CENTER)) {
		int y = (dim.height - height) / 2;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int x = (dim.width - cdim.width) / 2;
			c.setBounds(x, y, cdim.width, cdim.height);
			y += (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.NORTH)) {
		int y = 0;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			int x = (dim.width - cdim.width) / 2;
			c.setBounds(x, y, cdim.width, cdim.height);
			y += (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.SOUTH_WEST)) {
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(0, dim.height - height - 1, cdim.width, cdim.height);
			height -= (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.WEST)) {
		int y = (dim.height - height) / 2;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(0, y, cdim.width, cdim.height);
			y += (gap + cdim.height);
		  }
		}
	  } else if (anchor.equals(Anchor.NORTH_WEST)) {
		int y = 0;
		for (Component c : decorations.values()) {
		  if (c.isVisible()) {
			Dimension cdim = c.getPreferredSize();
			c.setBounds(0, y, cdim.width, cdim.height);
			y += (gap + cdim.height);
		  }
		}
	  }
	}
  }

  public static enum Direction {
	VERTICAL,
	HORIZONTAL;
  }

}
