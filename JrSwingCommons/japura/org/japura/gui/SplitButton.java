package org.japura.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.japura.gui.renderer.DefaultSplitButtonRenderer;
import org.japura.gui.renderer.SplitButtonRenderer;

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
public class SplitButton extends JButton{

  private static final long serialVersionUID = -8584516730020272064L;
  public final static Mode BUTTON = Mode.BUTTON;
  public final static Mode MENU = Mode.MENU;
  private Mode mode;
  private int maxLabelWidth;
  private int alignment = SwingConstants.CENTER;
  private int imageWidth = 13;
  private int gap = 5;
  private int separatorGap = 7;
  private String actualButton;
  private boolean fireBlocked;
  private boolean buttonChooserVisible;
  private boolean mouseIn;
  private HashMap<String, ArrayList<ActionListener>> listeners;
  private LinkedHashMap<String, Boolean> buttons;
  private List<String> disabledButtons;
  private DefaultListModel listModel;
  private JPopupMenu buttonsChooser;
  private JPanel buttonsRoot;
  private JList buttonsList;
  private ListRenderer listRenderer;
  private SplitButtonRenderer splitButtonRenderer;
  private transient MouseListener originalMouseListener;
  private transient MouseListener handlerMouseListener;

  public SplitButton() {
	this(Mode.BUTTON);
  }

  public SplitButton(Mode mode) {
	if (mode != null) {
	  this.mode = mode;
	} else {
	  this.mode = BUTTON;
	}
	buttons = new LinkedHashMap<String, Boolean>();
	splitButtonRenderer = new DefaultSplitButtonRenderer();
	disabledButtons = new ArrayList<String>();
	listeners = new HashMap<String, ArrayList<ActionListener>>();
	URL url =
		getClass().getResource("/resources/images/jpr_splitbuttondown.png");

	Insets margin = getMargin();
	margin.right = gap;
	margin.left = gap;
	setMargin(margin);

	super.setHorizontalTextPosition(SwingConstants.LEFT);
	super.setHorizontalAlignment(SwingConstants.RIGHT);
	super.setIcon(new ImageIcon(url));
	setFocusPainted(false);
	originalMouseListener = getMouseListeners()[0];
	removeMouseListener(originalMouseListener);
	addMouseListener(getHandlerMouseListener());
	addComponentListener(new ComponentAdapter() {
	  @Override
	  public void componentResized(ComponentEvent e) {
		reajustTextGap();
	  }
	});
  }

  private DefaultListModel getListModel() {
	if (listModel == null) {
	  listModel = new DefaultListModel();
	}
	return listModel;
  }

  private void showButtonsChooser() {
	if (isEnabled() == false) {
	  return;
	}

	buttonChooserVisible = true;
	fireBlocked = true;

	getListModel().removeAllElements();
	for (Entry<String, Boolean> entry : buttons.entrySet()) {
	  if (entry.getValue()) {
		getListModel().addElement(entry.getKey());
	  }
	}

	if (getListModel().size() == 0) {
	  buttonChooserVisible = false;
	  fireBlocked = false;
	  return;
	}

	Dimension dim = getSize();
	Dimension bcDim = getButtonsRoot().getPreferredSize();
	Insets insets = getButtonsChooser().getInsets();
	int width = dim.width;
	int height = bcDim.height + insets.bottom + insets.top;
	Dimension newDim = new Dimension(width, height);
	getButtonsChooser().setPreferredSize(newDim);
	getButtonsChooser().show(this, 0, dim.height);
  }

  private JPopupMenu getButtonsChooser() {
	if (buttonsChooser == null) {
	  buttonsChooser = new JPopupMenu();
	  buttonsChooser.add(getButtonsRoot());

	  buttonsChooser.addPopupMenuListener(new PopupMenuListener() {
		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		  if (mouseIn) {
			buttonChooserVisible = true;
		  } else {
			buttonChooserVisible = false;
		  }
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
	  });
	}
	return buttonsChooser;
  }

  private JPanel getButtonsRoot() {
	if (buttonsRoot == null) {
	  buttonsRoot = new JPanel();
	  buttonsRoot.setLayout(new BorderLayout());
	  buttonsRoot.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
	  buttonsRoot.add(getButtonsList(), BorderLayout.CENTER);
	}
	return buttonsRoot;
  }

  private JList getButtonsList() {
	if (buttonsList == null) {
	  buttonsList = new JList();
	  listRenderer = new ListRenderer();
	  buttonsList.setCellRenderer(listRenderer);
	  buttonsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  buttonsList.setModel(getListModel());
	  buttonsList.addListSelectionListener(new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
		  if (e.getValueIsAdjusting() == false) {
			if (buttonsList.getSelectedIndex() > -1) {
			  String name = (String) buttonsList.getSelectedValue();
			  if (isButtonEnabled(name)) {
				setButton(name);
				doClick();
				buttonsList.clearSelection();
			  }
			}
		  }
		}
	  });
	  buttonsList.addMouseMotionListener(new MouseMotionListener() {
		@Override
		public void mouseDragged(MouseEvent e) {}

		@Override
		public void mouseMoved(MouseEvent e) {
		  listRenderer.mouseOverIndex =
			  buttonsList.locationToIndex(e.getPoint());
		  buttonsList.repaint();
		}
	  });
	  buttonsList.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent e) {
		  listRenderer.mouseOverIndex = -1;
		  buttonsList.repaint();
		}
	  });
	}
	return buttonsList;
  }

  private void removeActualListeners() {
	if (actualButton != null) {
	  ArrayList<ActionListener> array = listeners.get(actualButton);
	  for (ActionListener listener : array) {
		super.removeActionListener(listener);
	  }
	}
  }

  public void setButton(String name) {
	name = ajustName(name);
	fireBlocked = false;
	getButtonsChooser().setVisible(false);
	listRenderer.mouseOverIndex = -1;

	if (name == null) {
	  actualButton = null;
	  if (mode.equals(Mode.BUTTON)) {
		super.setText("");
	  }
	} else if (listeners.containsKey(name) && isButtonEnabled(name)) {
	  removeActualListeners();
	  actualButton = name;
	  ArrayList<ActionListener> array = listeners.get(actualButton);
	  for (ActionListener listener : array) {
		super.addActionListener(listener);
	  }
	  if (mode.equals(Mode.BUTTON)) {
		super.setText(name);
	  }
	}
	reajustTextGap();
  }

  private void reajustTextGap() {
	FontMetrics fm = getFontMetrics(getFont());
	Dimension dim = null;
	if (isShowing()) {
	  dim = getSize();
	} else {
	  dim = getPreferredSize();
	}
	Insets insets = getInsets();
	int avaiableWidth =
		dim.width - insets.left - insets.right - imageWidth - separatorGap - 3
			- gap;
	int d = gap + 3 + separatorGap;
	int width = fm.stringWidth(getText());
	if (alignment == SwingConstants.LEFT) {
	  super.setIconTextGap(avaiableWidth - width + d);
	} else if (alignment == SwingConstants.CENTER) {
	  super.setIconTextGap((avaiableWidth / 2) - ((width) / 2) + d);
	} else if (alignment == SwingConstants.RIGHT) {
	  super.setIconTextGap(gap + 3 + separatorGap);
	}
  }

  @Override
  public final void setHorizontalTextPosition(int textPosition) {}

  /**
   * Sets the horizontal alignment of the text. {@code SplitButton}'s default is
   * {@code SwingConstants.CENTER}.
   * 
   * @param alignment
   *          the alignment value, one of the following values:
   *          <ul>
   *          <li>{@code SwingConstants.RIGHT}
   *          <li>{@code SwingConstants.LEFT}
   *          <li>{@code SwingConstants.CENTER} (default)
   *          </ul>
   * @throws IllegalArgumentException
   *           if the alignment is not one of the valid values
   * 
   */
  @Override
  public final void setHorizontalAlignment(int alignment) {
	if ((alignment == LEFT) || (alignment == CENTER) || (alignment == RIGHT)) {
	  this.alignment = alignment;
	  reajustTextGap();
	} else {
	  throw new IllegalArgumentException();
	}
  }

  @Override
  public final void setIconTextGap(int iconTextGap) {}

  private void calculateMaxLabelWidth() {
	maxLabelWidth = 0;
	FontMetrics fm = getFontMetrics(getFont());
	for (String name : listeners.keySet()) {
	  maxLabelWidth = Math.max(maxLabelWidth, fm.stringWidth(name));
	}
	if (mode.equals(Mode.MENU)) {
	  maxLabelWidth = Math.max(maxLabelWidth, fm.stringWidth(getText()));
	}
  }

  @Override
  public Dimension getMinimumSize() {
	if (isMinimumSizeSet()) {
	  return super.getMinimumSize();
	}
	return calculatePreferredSize();
  }

  @Override
  public Dimension getPreferredSize() {
	if (isPreferredSizeSet()) {
	  return super.getPreferredSize();
	}
	return calculatePreferredSize();
  }

  private Dimension calculatePreferredSize() {
	Dimension dim = super.getPreferredSize();
	int width = maxLabelWidth;
	Insets insets = getInsets();
	width += insets.left + insets.right;
	width += gap + imageWidth + separatorGap + 3;
	dim.width = width;
	return dim;
  }

  @Override
  public final void setIcon(Icon defaultIcon) {}

  private String ajustName(String name) {
	if (name == null) {
	  return null;
	}
	return name.trim();
  }

  public void addButton(String name) {
	name = ajustName(name);
	if (name != null && name.length() > 0
		&& listeners.containsKey(name) == false) {
	  listeners.put(name, new ArrayList<ActionListener>());
	  buttons.put(name, true);
	  calculateMaxLabelWidth();
	  if (mode.equals(Mode.BUTTON) && actualButton == null) {
		setButton(name);
	  }
	  reajustTextGap();
	}
  }

  public void removeButton(String name) {
	name = ajustName(name);
	if (name != null && listeners.containsKey(name)) {
	  disabledButtons.remove(name);
	  buttons.remove(name);

	  if (actualButton.equals(name)) {
		removeActualListeners();
	  }
	  listeners.remove(name);
	  calculateMaxLabelWidth();
	  if (actualButton.equals(name)) {
		setButton(null);
	  }
	  reajustTextGap();
	}
  }

  @Override
  public final void addActionListener(ActionListener l) {
	if (mode.equals(Mode.BUTTON) && actualButton != null) {
	  addActionListener(actualButton, l);
	}
  }

  public final void addActionListener(String name, ActionListener l) {
	if (listeners.containsKey(name)) {
	  ArrayList<ActionListener> array = listeners.get(name);
	  if (array.contains(l) == false) {
		array.add(l);
		if (mode.equals(Mode.BUTTON) && actualButton != null
			&& actualButton.equals(name)) {
		  super.addActionListener(l);
		}
	  }
	}
  }

  @Override
  public final void removeActionListener(ActionListener l) {
	if (mode.equals(Mode.BUTTON) && actualButton != null) {
	  removeActionListener(actualButton, l);
	}
  }

  public final void removeActionListener(String name, ActionListener l) {
	if (name != null && listeners.containsKey(name)) {
	  ArrayList<ActionListener> array = listeners.get(name);
	  array.remove(l);
	  if (actualButton.equals(name)) {
		super.removeActionListener(l);
	  }
	}
  }

  @Override
  public final ActionListener[] getActionListeners() {
	return getActionListeners(actualButton);
  }

  public final ActionListener[] getActionListeners(String name) {
	if (name != null && listeners.containsKey(name)) {
	  ArrayList<ActionListener> array = listeners.get(name);
	  return array.toArray(new ActionListener[0]);
	}
	return new ActionListener[] {};
  }

  @Override
  protected final void fireActionPerformed(ActionEvent event) {
	if (fireBlocked == false) {
	  super.fireActionPerformed(event);
	}
  }

  public Mode getMode() {
	return mode;
  }

  @Override
  public final void setText(String text) {
	if (mode.equals(Mode.MENU)) {
	  super.setText(text);
	  calculateMaxLabelWidth();
	}
  }

  @Override
  protected final void paintComponent(Graphics g) {
	super.paintComponent(g);
	if (mode.equals(Mode.BUTTON)) {
	  Dimension dim = getSize();
	  Insets insets = getInsets();
	  int x = dim.width - insets.right - imageWidth - separatorGap;
	  int y = 6;
	  g.setColor(Color.white);
	  g.fillRect(x, y, 3, dim.height - (2 * y));
	  g.setColor(Color.gray);
	  g.drawLine(x + 1, y, x + 1, dim.height - y);
	}
  }

  public void setButtonEnabled(String name, boolean enabled) {
	name = ajustName(name);
	if (name != null && listeners.containsKey(name)) {
	  if (enabled) {
		disabledButtons.remove(name);
		if (mode.equals(Mode.BUTTON) && actualButton == null) {
		  setButton(name);
		}
	  } else {
		disabledButtons.add(name);

		if (mode.equals(Mode.BUTTON) && actualButton != null
			&& actualButton.equals(name)) {
		  boolean founded = false;
		  for (String otherButton : listeners.keySet()) {
			if (isButtonEnabled(otherButton)) {
			  founded = true;
			  setButton(otherButton);
			  break;
			}
		  }
		  if (founded == false) {
			setButton(null);
		  }
		}
	  }
	}
  }

  public void setButtonVisible(String name, boolean visible) {
	name = ajustName(name);
	buttons.put(name, visible);
  }

  public boolean isButtonVisible(String name) {
	Boolean visible = buttons.get(name);
	if (visible != null) {
	  return visible.booleanValue();
	}
	return false;
  }

  public boolean isButtonEnabled(String name) {
	if (name != null && listeners.containsKey(name)) {
	  return !disabledButtons.contains(name);
	}
	return false;
  }

  public String getSelectedButton() {
	return actualButton;
  }

  private MouseListener getHandlerMouseListener() {
	if (handlerMouseListener == null) {
	  handlerMouseListener = new MouseListener() {
		@Override
		public void mouseClicked(MouseEvent e) {
		  originalMouseListener.mouseClicked(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		  mouseIn = true;
		  originalMouseListener.mouseEntered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
		  mouseIn = false;
		  originalMouseListener.mouseExited(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
		  originalMouseListener.mousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		  if (mode.equals(Mode.MENU)) {
			removeActualListeners();
		  }

		  Dimension dim = getSize();
		  int x = dim.width - getInsets().right - imageWidth - separatorGap;

		  if (buttonChooserVisible == false && listeners.keySet().size() > 0
			  && (mode.equals(Mode.MENU) || e.getPoint().x > x)) {
			if (isEnabled())
			  showButtonsChooser();
		  } else {
			getButtonsChooser().setVisible(false);
			listRenderer.mouseOverIndex = -1;
			buttonChooserVisible = false;
		  }
		  if (e.getPoint().x < x) {
			fireBlocked = false;
		  }
		  originalMouseListener.mouseReleased(e);
		}
	  };
	}
	return handlerMouseListener;
  }

  public SplitButtonRenderer getRenderer() {
	return splitButtonRenderer;
  }

  public void setRenderer(SplitButtonRenderer renderer) {
	if (renderer == null) {
	  renderer = new DefaultSplitButtonRenderer();
	}
	this.splitButtonRenderer = renderer;
  }

  private class ListRenderer implements ListCellRenderer{

	public int mouseOverIndex;

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus) {
	  String button = (String) value;
	  if (mouseOverIndex == index) {
		cellHasFocus = true;
	  } else {
		cellHasFocus = false;
	  }

	  return splitButtonRenderer.getCellRendererComponent(button, cellHasFocus,
		  isButtonEnabled(button));
	}
  }

  public static enum Mode {
	BUTTON,
	MENU
  }

}
