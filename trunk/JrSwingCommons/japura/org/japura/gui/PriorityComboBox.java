package org.japura.gui;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.japura.gui.model.DefaultPriorityComboBoxModel;
import org.japura.gui.model.PriorityComboBoxModel;
import org.japura.gui.renderer.PriorityComboBoxRenderer;

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
public class PriorityComboBox extends JComboBox{

  public PriorityComboBox() {
	super(new DefaultPriorityComboBoxModel());
	init();
  }

  public PriorityComboBox(Object[] items) {
	super(new DefaultPriorityComboBoxModel(items));
	init();
  }

  public PriorityComboBox(Vector<?> items) {
	super(new DefaultPriorityComboBoxModel(items));
	init();
  }

  private void init() {
	setRenderer(new PriorityComboBoxRenderer());
	setKeySelectionManager(new PriorityKeySelectionManager());
  }

  @Override
  public PriorityComboBoxModel getModel() {
	return (PriorityComboBoxModel) super.getModel();
  }

  /**
   * Changing the default Java ComboBox KeySelectionManager
   * (JComboBox.DefaultKeySelectionManager).
   * 
   */
  private static class PriorityKeySelectionManager implements
	  KeySelectionManager, Serializable{

	@Override
	public int selectionForKey(char aKey, ComboBoxModel aModel) {
	  PriorityComboBoxModel model = (PriorityComboBoxModel) aModel;
	  int i, c;
	  int currentSelection = -1;
	  Object selectedItem = aModel.getSelectedItem();
	  String v;
	  String pattern;

	  if (selectedItem != null) {
		for (i = 0, c = aModel.getSize(); i < c; i++) {
		  if (selectedItem == aModel.getElementAt(i)
			  && i > model.getPriorityItemsSize()) {
			currentSelection = i;
			break;
		  }
		}
	  }

	  pattern = ("" + aKey).toLowerCase();
	  aKey = pattern.charAt(0);

	  for (i = ++currentSelection, c = aModel.getSize(); i < c; i++) {
		Object elem = aModel.getElementAt(i);
		if (elem != null && elem.toString() != null) {
		  v = elem.toString().toLowerCase();
		  if (v.length() > 0 && v.charAt(0) == aKey
			  && i > model.getPriorityItemsSize())
			return i;
		}
	  }

	  for (i = 0; i < currentSelection; i++) {
		Object elem = aModel.getElementAt(i);
		if (elem != null && elem.toString() != null) {
		  v = elem.toString().toLowerCase();
		  if (v.length() > 0 && v.charAt(0) == aKey
			  && i > model.getPriorityItemsSize())
			return i;
		}
	  }
	  return -1;
	}
  }

}
