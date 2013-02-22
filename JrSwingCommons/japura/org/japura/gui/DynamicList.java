package org.japura.gui;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.japura.gui.renderer.HighlightCellRenderer;

/**
 * 
 * <P>
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
 * 
 */
public class DynamicList<T> {

  private JPopupMenu optionsPopup;
  private JComponent invoker;
  private JTextField field;
  private JPanel root;
  private JList list;
  private DefaultListModel model;
  private List<T> options;
  private boolean caseSensitive;

  public DynamicList(ButtonTextField buttonField, List<T> options) {
	this(buttonField, options, false);
  }

  public DynamicList(ButtonTextField buttonField, List<T> options,
	  boolean caseSensitive) {
	invoker = buttonField;
	init(buttonField.getField(), options, caseSensitive);
  }

  public DynamicList(JTextField field, List<T> options) {
	this(field, options, false);
  }

  public DynamicList(JTextField field, List<T> options, boolean caseSensitive) {
	invoker = field;
	init(field, options, caseSensitive);
  }

  private void init(JTextField field, List<T> options, boolean caseSensitive) {
	this.field = field;
	this.caseSensitive = caseSensitive;
	this.options = options;
	setCellRenderer(new HighlightCellRenderer(caseSensitive));

	model = new DefaultListModel();
	for (T item : options) {
	  String text = item.toString();
	  model.addElement(text);
	}
	getList().setModel(model);
	getOptionsPopup().setPreferredSize(getOptionsPopup().getPreferredSize());

	field.addKeyListener(new KeyAdapter() {
	  @Override
	  public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() != KeyEvent.VK_ESCAPE) {
		  rebuildModel(true);
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		  getList().requestFocus();
		  if (getList().getSelectedIndex() == -1 && model.size() > 0) {
			getList().setSelectedIndex(0);
		  }
		} else {
		  getField().requestFocus();
		}
	  }
	});
  }

  public ListCellRenderer getCellRenderer() {
	return getList().getCellRenderer();
  }

  public void setCellRenderer(ListCellRenderer cellRenderer) {
	getList().setCellRenderer(cellRenderer);
  }

  @SuppressWarnings("unchecked")
  private void chooseSelectedListItem() {
	if (getList().getSelectedIndex() > -1) {
	  T item = (T) getList().getSelectedValue();
	  getField().setText(item.toString());
	  rebuildModel(false);
	}
  }

  private JTextField getField() {
	return field;
  }

  private JPopupMenu getOptionsPopup() {
	if (optionsPopup == null) {
	  optionsPopup = new JPopupMenu();
	  optionsPopup.add(getRoot());
	}
	return optionsPopup;
  }

  private JPanel getRoot() {
	if (root == null) {
	  root = new JPanel();
	  root.setLayout(new GridLayout(1, 1));
	  JScrollPane sp = new JScrollPane(getList());
	  sp.setBorder(null);
	  root.add(sp);
	}
	return root;
  }

  private void rebuildModel(boolean show) {
	String typedText = getField().getText();
	if (caseSensitive == false) {
	  typedText = typedText.toLowerCase();
	}
	if (getCellRenderer() instanceof HighlightCellRenderer) {
	  HighlightCellRenderer renderer =
		  (HighlightCellRenderer) getCellRenderer();
	  renderer.setHighlightText(typedText);
	}
	model = new DefaultListModel();
	for (T item : options) {
	  String text = item.toString();
	  if (caseSensitive == false)
		text = text.toLowerCase();
	  if (text.length() == 0 || text.indexOf(typedText) > -1) {
		model.addElement(item);
	  }
	}
	getList().setModel(model);

	if (show && model.getSize() > 0) {
	  getOptionsPopup().show(invoker, 0, invoker.getHeight());
	} else {
	  getOptionsPopup().setVisible(false);
	}
  }

  public T getItem() {
	String typedText = getField().getText();
	if (caseSensitive == false) {
	  typedText = typedText.toLowerCase();
	}
	for (T item : options) {
	  String text = item.toString();
	  if (caseSensitive == false)
		text = text.toLowerCase();
	  if (text.equals(typedText)) {
		return item;
	  }
	}
	return null;
  }

  public void setItem(T item) {
	if (item == null) {
	  getField().setText("");
	} else if (options.contains(item)) {
	  getField().setText(item.toString());
	}
  }

  public int getAvailableItemsCount() {
	return model.getSize();
  }

  public void showDynamicList() {
	rebuildModel(true);
  }

  private JList getList() {
	if (list == null) {
	  list = new JList();
	  list.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
	  list.addKeyListener(new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
		  if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			chooseSelectedListItem();
		  }
		}

	  });
	  list.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		  if (e.getClickCount() == 2) {
			chooseSelectedListItem();
		  }
		}
	  });
	}
	return list;
  }

}
