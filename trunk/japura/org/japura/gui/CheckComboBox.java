package org.japura.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;

import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.japura.gui.model.DefaultListCheckModel;
import org.japura.gui.model.ListCheckModel;
import org.japura.gui.renderer.CheckListRenderer;

/**
 * ComboBox with Check items.
 * <P>
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
public class CheckComboBox extends JComponent{

  private static final long serialVersionUID = 8189247456412690742L;
  public static final CheckState NONE = CheckState.NONE;
  public static final CheckState MULTIPLE = CheckState.MULTIPLE;
  public static final CheckState ALL = CheckState.ALL;
  private Popup itemsChooser;
  private CheckList checkList;
  private ListDataListener listDataListener;
  private ListCheckListener listCheckListener;

  private CompoundComponent compoundComponent;
  private JComboBox comboBox;

  private String prototypeDisplayValue;

  private int visibleRowCount = 8;

  private HashMap<CheckState, CharSequence> texts;

  public CheckComboBox() {
	texts = new HashMap<CheckState, CharSequence>();
	setFocusable(true);
	setRenderer(new CheckListRenderer());
	setModel(new DefaultListCheckModel());
	super.setLayout(new BorderLayout());
	add(getCompoundComponent(), BorderLayout.CENTER);

	addFocusListener(new FocusAdapter() {
	  @Override
	  public void focusGained(FocusEvent e) {
		getComboBox().requestFocusInWindow();
	  }
	});

	addAncestorListener(new AncestorListener() {
	  public void ancestorAdded(AncestorEvent event) {
		setItemsChooserVisible(false);
	  }

	  public void ancestorRemoved(AncestorEvent event) {
		setItemsChooserVisible(false);
	  }

	  public void ancestorMoved(AncestorEvent event) {
		if (event.getSource() != CheckComboBox.this)
		  setItemsChooserVisible(false);
	  }
	});
  }

  private JComboBox getComboBox() {
	if (comboBox == null) {
	  comboBox = new ComboBox();
	}
	return comboBox;
  }

  private CompoundComponent getCompoundComponent() {
	if (compoundComponent == null) {
	  MouseListener listener = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
		  long time =
			  Math.abs(getItemsChooser().getLastViewChanged() - e.getWhen());
		  if (time > 20) {
			changeItemsVisible();
		  }
		}
	  };
	  compoundComponent = new CompoundComponent(getComboBox(), listener);
	}
	return compoundComponent;
  }

  public void setRenderer(CheckListRenderer renderer) {
	getCheckList().setCellRenderer(renderer);
	updateCellPanelWidth();
  }

  private void changeItemsVisible() {
	setItemsChooserVisible(!isItemsChooserVisible());
  }

  public boolean isItemsChooserVisible() {
	return getItemsChooser().isVisible();
  }

  public int getVisibleRowCount() {
	return visibleRowCount;
  }

  public void setVisibleRowCount(int visibleRowCount) {
	this.visibleRowCount = Math.max(3, visibleRowCount);
	getCheckList().setVisibleRowCount(getVisibleRowCount());
  }

  public void setItemsChooserVisible(boolean visible) {
	if (visible) {
	  if (getModel().getSize() > 0) {
		int rows = Math.min(getVisibleRowCount(), getModel().getSize());
		getCheckList().setVisibleRowCount(rows);
		getCheckList().setPreferredSize(null);
		Dimension pSize = getCheckList().getPreferredSize();
		Dimension dim = getSize();
		if (pSize.width < dim.width) {
		  getCheckList().setPreferredSize(
			  new Dimension(dim.width, pSize.height));
		}
		getItemsChooser().show(this, 0, dim.height);
	  }
	} else {
	  getItemsChooser().setVisible(false);
	}
  }

  public void setModel(ListCheckModel model) {
	getCheckList().setModel(model);
	model.removeListDataListener(getListDataListener());
	model.removeListCheckListener(getListCheckListener());
	model.addListDataListener(getListDataListener());
	model.addListCheckListener(getListCheckListener());
	updateCellPanelWidth();
  }

  public ListCheckModel getModel() {
	return getCheckList().getModel();
  }

  public void setPrototypeDisplayValue(String prototypeDisplayValue) {
	this.prototypeDisplayValue = prototypeDisplayValue;
  }

  public String getPrototypeDisplayValue() {
	return prototypeDisplayValue;
  }

  private void updateCellPanelWidth() {
	if (getPrototypeDisplayValue() != null) {
	  getComboBox().setPrototypeDisplayValue(getPrototypeDisplayValue());
	  return;
	}

	CharSequence value = null;

	for (CheckState checkState : CheckState.values()) {
	  CharSequence text = getTextFor(checkState);
	  if (text != null) {
		if (value == null) {
		  value = text;
		} else if (text.length() > value.length()) {
		  value = text;
		}
	  }
	}

	ListCheckModel m = getModel();

	for (int i = 0; i < m.getSize(); i++) {
	  String str = m.getElementAt(i).toString();
	  if (value == null) {
		value = str;
	  } else if (str.length() > value.length()) {
		value = str;
	  }
	}

	getComboBox().setPrototypeDisplayValue(value);
  }

  @Override
  public final void setLayout(LayoutManager mgr) {}

  private CheckList getCheckList() {
	if (checkList == null) {
	  checkList = new CheckList();
	}
	return checkList;
  }

  private ListCheckListener getListCheckListener() {
	if (listCheckListener == null) {
	  listCheckListener = new ListCheckListener() {
		@Override
		public void addCheck(ListEvent event) {
		  checkUpdated();
		}

		@Override
		public void removeCheck(ListEvent event) {
		  checkUpdated();
		}
	  };
	}
	return listCheckListener;
  }

  private void checkUpdated() {
	getComboBox().removeAllItems();

	CheckListRenderer renderer = getCheckList().getCellRenderer();

	List<Object> checkeds = getModel().getCheckeds();
	int total = getModel().getSize();
	if (total > 0) {
	  if (checkeds.size() == 0 && texts.containsKey(CheckState.NONE)) {
		getComboBox().addItem(getTextFor(CheckState.NONE));
	  } else if (checkeds.size() == 1) {
		getComboBox().addItem(renderer.getText(checkeds.get(0)));
	  } else if (checkeds.size() == total && texts.containsKey(CheckState.ALL)) {
		getComboBox().addItem(getTextFor(CheckState.ALL));
	  } else if (checkeds.size() > 1 && texts.containsKey(CheckState.MULTIPLE)) {
		getComboBox().addItem(getTextFor(CheckState.MULTIPLE));
	  }
	}
  }

  private ListDataListener getListDataListener() {
	if (listDataListener == null) {
	  listDataListener = new ListDataListener() {
		@Override
		public void contentsChanged(ListDataEvent e) {}

		@Override
		public void intervalAdded(ListDataEvent e) {
		  checkUpdated();
		  updateCellPanelWidth();
		  repaint();
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
		  checkUpdated();
		  updateCellPanelWidth();
		  repaint();
		}
	  };
	}
	return listDataListener;
  }

  public CharSequence getTextFor(CheckState checkState) {
	if (checkState != null) {
	  return texts.get(checkState);
	}
	return null;
  }

  public void removeTextFor(CheckState checkState) {
	if (checkState != null) {
	  texts.remove(checkState);
	  if (checkState.equals(NONE)) {
		checkUpdated();
	  }
	}
  }

  public void setTextFor(CheckState checkState, CharSequence text) {
	if (checkState == null) {
	  return;
	}

	if (text == null) {
	  text = "";
	}

	texts.put(checkState, text);
	if (checkState.equals(NONE)) {
	  checkUpdated();
	}
	updateCellPanelWidth();
	repaint();
  }

  @Override
  public void setEnabled(boolean enabled) {
	super.setEnabled(enabled);
	getCompoundComponent().setEnabled(enabled);
  }

  private Popup getItemsChooser() {
	if (itemsChooser == null) {
	  itemsChooser = new Popup();
	  itemsChooser.setBorder(BorderFactory.createEmptyBorder());

	  JScrollPane sp = new JScrollPane();
	  sp.setBorder(BorderFactory.createLineBorder(Color.black));
	  sp.setViewportView(getCheckList());
	  itemsChooser.add(sp);

	  itemsChooser.addMenuKeyListener(new MenuKeyListener() {
		@Override
		public void menuKeyTyped(MenuKeyEvent e) {}

		@Override
		public void menuKeyReleased(MenuKeyEvent e) {}

		@Override
		public void menuKeyPressed(MenuKeyEvent e) {
		  if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setItemsChooserVisible(false);
		  } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
			setItemsChooserVisible(false);
			if (e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK) {
			  transferFocusBackward();
			} else {
			  transferFocus();
			}
		  }
		}
	  });
	}
	return itemsChooser;
  }

  private class ComboBox extends JComboBox{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void processKeyEvent(KeyEvent e) {
	  if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		CheckComboBox.this.setItemsChooserVisible(true);
	  }
	}
  }

  private class Popup extends JPopupMenu{

	private static final long serialVersionUID = -7940039384534412109L;

	private long lastViewChanged;

	public Popup() {
	  for (MenuKeyListener l : getMenuKeyListeners()) {
		removeMenuKeyListener(l);
	  }
	}

	public long getLastViewChanged() {
	  return lastViewChanged;
	}

	@Override
	public void setVisible(boolean b) {
	  lastViewChanged = System.currentTimeMillis();
	  super.setVisible(b);
	}
  }

  public static enum CheckState {
	NONE,
	ALL,
	MULTIPLE;
  }

}
