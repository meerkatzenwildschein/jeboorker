package org.japura.gui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.japura.gui.event.ListLockListener;
import org.japura.gui.event.ListModelListener;

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
public class DefaultListCheckModel<T> implements ListCheckModel<T>, Serializable{

  protected EventListenerList listenerList = new EventListenerList();
  private List<T> items;
  private List<T> checkeds;
  private List<T> lockeds;

  public DefaultListCheckModel() {
	checkeds = new ArrayList<T>();
	lockeds = new ArrayList<T>();
	items = new ArrayList<T>();
  }

  protected void fireAddedListModelListeners(List<T> values, int index1, int index2, boolean valueIsAdjusting) {
	ListModelListener listeners[] = listenerList.getListeners(ListModelListener.class);

	ListEvent<T> e = new ListEvent<T>(this, values, valueIsAdjusting);
	for (ListModelListener l : listeners) {
	  l.valueAdded(e);
	}

	ListDataEvent e2 =
		new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index1, index2);
	ListDataListener listeners2[] =
		listenerList.getListeners(ListDataListener.class);
	for (ListDataListener l : listeners2) {
	  l.intervalAdded(e2);
	}
  }

  protected void fireRemovedListModelListeners(List<T> values,
											   boolean valueIsAdjusting) {
	ListModelListener listeners[] =
		listenerList.getListeners(ListModelListener.class);

	ListEvent<T> e = new ListEvent<T>(this, values, valueIsAdjusting);
	for (ListModelListener l : listeners) {
	  l.valueRemoved(e);
	}
  }

  protected void fireRemovedListModelListeners(int index1, int index2,
											   boolean valueIsAdjusting) {
	ListDataEvent e2 =
		new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index1, index2);
	ListDataListener listeners2[] =
		listenerList.getListeners(ListDataListener.class);
	for (ListDataListener l : listeners2) {
	  l.intervalRemoved(e2);
	}
  }

  protected void fireAddLockListModelListeners(List<T> values, boolean valueIsAdjusting) {
	fireLockListModelListeners(values, valueIsAdjusting, true);
  }

  protected void fireRemoveLockListModelListeners(List<T> values, boolean valueIsAdjusting) {
	fireLockListModelListeners(values, valueIsAdjusting, false);
  }

  private void fireLockListModelListeners(List<T> values, boolean valueIsAdjusting, boolean add) {
	ListLockListener listeners[] =
		listenerList.getListeners(ListLockListener.class);

	ListEvent e = new ListEvent(this, values, valueIsAdjusting);
	for (ListLockListener l : listeners) {
	  if (add)
		l.addLock(e);
	  else
		l.removeLock(e);
	}
  }

  protected void fireAddCheckListModelListeners(List<T> values, boolean valueIsAdjusting) {
	fireCheckListModelListeners(values, valueIsAdjusting, true);
  }

  protected void fireRemoveCheckListModelListeners(List<T> values, boolean valueIsAdjusting) {
	fireCheckListModelListeners(values, valueIsAdjusting, false);
  }

  private void fireCheckListModelListeners(List<T> values, boolean valueIsAdjusting, boolean add) {
	ListCheckListener listeners[] =
		listenerList.getListeners(ListCheckListener.class);

	ListEvent e = new ListEvent(this, values, valueIsAdjusting);
	for (ListCheckListener l : listeners) {
	  if (add)
		l.addCheck(e);
	  else
		l.removeCheck(e);
	}
  }

  @Override
  public void clear() {
	clear(false);
  }

  private void clear(boolean valueIsAdjusting) {
	if (items.size() == 0)
	  return;

	checkeds.clear();
	lockeds.clear();
	if (items.size() > 0) {
	  int index2 = items.size() - 1;
	  items.clear();
	  fireRemovedListModelListeners(0, index2, valueIsAdjusting);
	  fireRemovedListModelListeners(new ArrayList<T>(), valueIsAdjusting);
	}
  }

  public void clearLockeds() {
	clearLockeds(false);
  }

  private void clearLockeds(boolean valueIsAdjusting) {
	if (lockeds.size() == 0)
	  return;

	lockeds.clear();
	fireRemoveLockListModelListeners(new ArrayList<T>(), valueIsAdjusting);
  }

  @Override
  public void addListLockListener(ListLockListener listener) {
	listenerList.add(ListLockListener.class, listener);
  }

  @Override
  public ListLockListener[] getListLockListeners() {
	return listenerList.getListeners(ListLockListener.class);
  }

  @Override
  public void removeListLockListener(ListLockListener listener) {
	listenerList.remove(ListLockListener.class, listener);
  }

  @Override
  public void addLock(T... values) {
	if (values.length == 0)
	  return;

	List<T> list = new ArrayList<T>();
	for (T value : values) {
	  if (lockeds.contains(value) == false) {
		lockeds.add(value);
		list.add(value);
	  }
	}

	if (list.size() > 0) {
	  fireAddLockListModelListeners(list, false);
	}
  }

  @Override
  public boolean isLocked(Object obj) {
	return lockeds.contains(obj);
  }

  @Override
  public void removeLock(T... values) {
	if (values.length == 0)
	  return;

	List<T> list = new ArrayList<T>();
	for (T value : values) {
	  if (lockeds.remove(value)) {
		list.add(value);
	  }
	}

	if (list.size() > 0) {
	  fireRemoveLockListModelListeners(list, false);
	}
  }

  @Override
  public void setLock(T... objs) {
	clearLockeds(true);
	addLock(objs);
  }

  @Override
  public List<T> getLockeds() {
	return new ArrayList<T>(lockeds);
  }

  private void clearCheckeds(boolean valueIsAdjusting) {
	if (checkeds.size() == 0)
	  return;

	checkeds.clear();
	fireRemoveCheckListModelListeners(new ArrayList<T>(), false);
  }

  @Override
  public void addListCheckListener(ListCheckListener listener) {
	listenerList.add(ListCheckListener.class, listener);
  }

  @Override
  public ListCheckListener[] getListCheckListeners() {
	return listenerList.getListeners(ListCheckListener.class);
  }

  @Override
  public void removeListCheckListener(ListCheckListener listener) {
	listenerList.remove(ListCheckListener.class, listener);
  }

  @Override
  public void addCheck(T... values) {
	if (values.length == 0)
	  return;

	List<T> list = new ArrayList<T>();
	for (T value : values) {
	  if (checkeds.contains(value) == false) {
		checkeds.add(value);
		list.add(value);
	  }
	}

	if (list.size() > 0) {
	  fireAddCheckListModelListeners(list, false);
	}
  }

  @Override
  public boolean isChecked(Object value) {
	return checkeds.contains(value);
  }

  @Override
  public void removeCheck(T... values) {
	if (values.length == 0)
	  return;

	List<T> list = new ArrayList<T>();
	for (T value : values) {
	  if (checkeds.remove(value)) {
		list.add(value);
	  }
	}

	if (list.size() > 0) {
	  fireRemoveCheckListModelListeners(list, false);
	}
  }

  @Override
  public void setCheck(T... values) {
	clearCheckeds(true);
	addCheck(values);
  }

  @Override
  public List<T> getCheckeds() {
	return new ArrayList<T>(checkeds);
  }

  @Override
  public void addElement(int index, T value) {
	if (index >= 0 && index <= items.size() && items.contains(value) == false) {
	  List<T> list = new ArrayList<T>();
	  items.add(index, value);
	  list.add(value);
	  fireAddedListModelListeners(list, index, index, false);
	}
  }

  @Override
  public void setElement(T... values) {
	clear(true);
	addElement(values);
  }

  @Override
  public void addElement(T... values) {
	if (values.length == 0)
	  return;

	List<T> list = new ArrayList<T>();
	for (T value : values) {
		items.add(value);
		list.add(value);
	}
	if (list.size() > 0) {
	  int index1 = items.size();
	  int index2 = index1 - 1 + list.size();
	  fireAddedListModelListeners(list, index1, index2, false);
	}
  }

  @Override
  public boolean contains(Object obj) {
	return items.contains(obj);
  }

  @Override
  public void removeElement(T... values) {
	if (values.length == 0)
	  return;

	HashMap<Object, Integer> indexMap = new HashMap<Object, Integer>();
	for (Object value : values) {
	  indexMap.put(value, items.indexOf(value));
	}
	List<Integer> indexList = new ArrayList<Integer>();

	List<T> list = new ArrayList<T>();
	for (T value : values) {
	  if (items.remove(value)) {
		indexList.add(indexMap.get(value));
		list.add(value);
		lockeds.remove(value);
		checkeds.remove(value);
	  }
	}

	if (list.size() > 0) {
	  Collections.sort(indexList);

	  int index1 = -1;
	  int index2 = -1;
	  for (int i = 0; i < indexList.size(); i++) {
		if (index1 == -1) {
		  index1 = indexList.get(i);
		  index2 = index1;
		} else {
		  if (indexList.get(i) > index2 + 1) {
			fireRemovedListModelListeners(index1, index2, true);
			index1 = indexList.get(i);
			index2 = index1;
		  } else {
			index2 = indexList.get(i);
		  }
		}

		if (i == indexList.size() - 1) {
		  fireRemovedListModelListeners(index1, index2, false);
		}
	  }

	  fireRemovedListModelListeners(list, false);
	}
  }

  @Override
  public T getElementAt(int index) {
	return items.get(index);
  }

  @Override
  public int getSize() {
	return items.size();
  }

  @Override
  public void addListModelListener(ListModelListener listener) {
	listenerList.add(ListModelListener.class, listener);
  }

  @Override
  public ListModelListener[] getListModelListeners() {
	return listenerList.getListeners(ListModelListener.class);
  }

  @Override
  public void removeListModelListener(ListModelListener listener) {
	listenerList.remove(ListModelListener.class, listener);
  }

  @Override
  public void addListDataListener(ListDataListener l) {
	listenerList.add(ListDataListener.class, l);
  }

  @Override
  public void removeListDataListener(ListDataListener l) {
	listenerList.remove(ListDataListener.class, l);
  }

  @Override
  public void removeChecks() {
	List<T> list = null;
	if (listenerList.getListenerCount(ListCheckListener.class) > 0) {
	  list = new ArrayList<T>(checkeds);
	}

	checkeds.clear();
	if (list != null) {
	  fireRemoveCheckListModelListeners(list, false);
	}
  }

  @Override
  public void removeLocks() {
	List<T> list = null;
	if (listenerList.getListenerCount(ListLockListener.class) > 0) {
	  list = new ArrayList<T>(checkeds);
	}

	lockeds.clear();
	if (list != null) {
	  fireRemoveLockListModelListeners(list, false);
	}
  }

  @Override
  public int getLocksCount() {
	return lockeds.size();
  }

  @Override
  public int getChecksCount() {
	return checkeds.size();
  }

  @Override
  public void checkAll() {
	List<T> list = null;
	if (listenerList.getListenerCount(ListCheckListener.class) > 0) {
	  list = new ArrayList<T>(items);
	}

	checkeds.clear();
	checkeds.addAll(items);

	if (list != null) {
	  fireAddCheckListModelListeners(list, false);
	}
  }

  @Override
  public void lockAll() {
	List<T> list = null;
	if (listenerList.getListenerCount(ListLockListener.class) > 0) {
	  list = new ArrayList<T>(items);
	}

	lockeds.clear();
	lockeds.addAll(items);

	if (list != null) {
	  fireAddLockListModelListeners(list, false);
	}
  }

}
