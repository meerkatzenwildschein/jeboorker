package org.japura.gui.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import org.japura.gui.event.PriorityComboBoxEvent;
import org.japura.gui.event.PriorityComboBoxListener;

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
public class DefaultPriorityComboBoxModel extends AbstractListModel implements
	PriorityComboBoxModel{

  private static final long serialVersionUID = 3463014910762087126L;
  private List<Object> items;
  private List<Object> priorityItems;
  private Object selectedItem;
  private int priorityItemsCapacity;
  private int minimalSizeForPriority;
  private boolean increasePriorityOnSelection;

  public DefaultPriorityComboBoxModel(Object[] objects) {
	this();
	addAll(objects);
  }

  public DefaultPriorityComboBoxModel(List<?> objects) {
	this();
	addAll(objects);
  }

  public DefaultPriorityComboBoxModel() {
	items = new ArrayList<Object>();
	priorityItems = new ArrayList<Object>();
	priorityItemsCapacity = 3;
	setMinimalSizeForPriority(10);
	increasePriorityOnSelection = true;
  }

  private void addAll(Object... objects) {
	for (Object obj : objects) {
	  items.add(obj);
	}
  }

  @Override
  public void addElement(Object item, boolean priorityItem) {
	if (items.contains(item) == false) {
	  items.add(item);
	  int i = items.size() - 1;

	  if (priorityItem) {
		addToPriority(item);
	  }

	  fireContentsChanged(this, i, i);
	  if (selectedItem == null) {
		selectedItem = item;
	  }
	}
  }

  public PriorityComboBoxListener[] getPriorityComboBoxListeners() {
	return (PriorityComboBoxListener[]) listenerList
		.getListeners(PriorityComboBoxListener.class);
  }

  @Override
  public void addPriorityComboBoxListener(PriorityComboBoxListener listener) {
	listenerList.add(PriorityComboBoxListener.class, listener);
  }

  @Override
  public void removePriorityComboBoxListener(PriorityComboBoxListener listener) {
	listenerList.remove(PriorityComboBoxListener.class, listener);
  }

  protected void firePriorityComboBoxListeners() {
	PriorityComboBoxEvent event = new PriorityComboBoxEvent(this);
	PriorityComboBoxListener[] array = getPriorityComboBoxListeners();
	for (PriorityComboBoxListener listener : array) {
	  listener.priorityChanged(event);
	}
  }

  @Override
  public List<Object> getPriorityItems() {
	return new ArrayList<Object>(priorityItems);
  }

  @Override
  public int getPriorityItemsCapacity() {
	return priorityItemsCapacity;
  }

  @Override
  public void setPriorityItemsCapacity(int capacity) {
	priorityItemsCapacity = Math.max(1, capacity);
	while (priorityItems.size() > getPriorityItemsCapacity()) {
	  int i = priorityItems.size() - 1;
	  priorityItems.remove(i);
	  fireIntervalRemoved(this, i, i);
	}
	firePriorityComboBoxListeners();
  }

  @Override
  public Object getSelectedItem() {
	return selectedItem;
  }

  @Override
  public void setSelectedItem(Object anItem) {
	if (items.contains(anItem)) {
	  selectedItem = anItem;
	  if (isIncreasePriorityOnSelection() && anItem != null) {
		addToPriority(anItem);
	  }
	}
  }

  @Override
  public Object getElementAt(int index) {
	if (index < 0 || index >= getSize())
	  return null;

	if (isPriorityAvailable()) {
	  if (index < priorityItems.size()) {
		return priorityItems.get(index);
	  }

	  index -= priorityItems.size();
	}
	return items.get(index);
  }

  @Override
  public int getSize() {
	if (isPriorityAvailable()) {
	  return items.size() + priorityItems.size();
	}
	return items.size();
  }

  @Override
  public void addToPriority(Object obj) {
	if (priorityItems.contains(obj)) {
	  int index = priorityItems.indexOf(obj);
	  if (index > 0) {
		priorityItems.remove(index);
		priorityItems.add(index - 1, obj);
	  }
	} else {
	  if (priorityItems.size() == getPriorityItemsCapacity()) {
		priorityItems.remove(priorityItems.size() - 1);
	  }
	  priorityItems.add(obj);
	}
	if (isPriorityAvailable()) {
	  int index = priorityItems.indexOf(obj);
	  fireContentsChanged(this, index, index);
	}
	firePriorityComboBoxListeners();
  }

  @Override
  public void removeFromPriority(Object obj) {
	if (priorityItems.contains(obj)) {
	  int index = priorityItems.indexOf(obj);
	  boolean fire = isPriorityAvailable();
	  priorityItems.remove(index);
	  if (fire) {
		fireContentsChanged(this, index, index);
	  }
	}
  }

  @Override
  public void addElement(Object item) {
	addElement(item, false);
  }

  @Override
  public void insertElementAt(Object item, int index) {
	if (items.contains(item) == false) {
	  if (isPriorityAvailable()) {
		index -= priorityItems.size();
	  }
	  if (index >= 0 && index <= items.size()) {
		items.add(index, item);
	  }
	}
  }

  @Override
  public void removeElement(Object obj) {
	if (obj == null)
	  return;

	priorityItems.remove(obj);
	int index = items.indexOf(obj);
	if (items.remove(obj)) {
	  int i = index;
	  if (isPriorityAvailable()) {
		i += getPriorityItemsSize();
	  }
	  fireIntervalRemoved(this, i, i);

	  if (priorityItems.remove(obj)) {
		firePriorityComboBoxListeners();
	  }
	}
	if (selectedItem.equals(obj)) {
	  if (getSize() > 0) {
		selectedItem = getElementAt(0);
	  } else {
		selectedItem = null;
	  }
	}
  }

  @Override
  public void removeElementAt(int index) {
	Object obj = getElementAt(index);
	removeElement(obj);
  }

  @Override
  public boolean isIncreasePriorityOnSelection() {
	return increasePriorityOnSelection;
  }

  @Override
  public void setIncreasePriorityOnSelection(boolean enable) {
	this.increasePriorityOnSelection = enable;
  }

  @Override
  public int getPriorityItemsSize() {
	return priorityItems.size();
  }

  @Override
  public void clearPriorities() {
	priorityItems.clear();
	firePriorityComboBoxListeners();
  }

  @Override
  public int getMinimalSizeForPriority() {
	return minimalSizeForPriority;
  }

  @Override
  public boolean isPriorityAvailable() {
	if (items.size() >= getMinimalSizeForPriority()) {
	  return true;
	}
	return false;
  }

  @Override
  public void setMinimalSizeForPriority(int size) {
	minimalSizeForPriority = Math.max(1, size);
  }

  @Override
  public boolean isPriorityItem(Object obj) {
	return priorityItems.contains(obj);
  }

}
