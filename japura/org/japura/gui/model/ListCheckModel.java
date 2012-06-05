package org.japura.gui.model;

import java.util.List;

import javax.swing.ListModel;

import org.japura.gui.event.ListCheckListener;
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
public interface ListCheckModel <T> extends ListModel {

  @Override
  public T getElementAt(int index);

  public boolean contains(T value);

  public void addElement(T... values);

  public void setElement(T... values);

  public void addElement(int index, T value);

  public void removeElement(T... values);

  public void addListModelListener(ListModelListener listener);

  public void removeListModelListener(ListModelListener listener);

  public ListModelListener[] getListModelListeners();

  public void addListLockListener(ListLockListener listener);

  public void removeListLockListener(ListLockListener listener);

  public ListLockListener[] getListLockListeners();

  public List<T> getLockeds();

  public void setLock(T... values);

  public void removeLock(T... values);

  public void addLock(T... values);

  /**
   * Returns true if the specified value is locked.
   */
  public boolean isLocked(T value);

  public void setCheck(T... values);

  public void removeCheck(T... values);

  public void addCheck(T... values);

  /**
   * Returns true if the specified value is checked.
   */
  public boolean isChecked(T value);

  public void clear();

  public void checkAll();

  public void lockAll();

  public void removeChecks();

  public void removeLocks();

  public void addListCheckListener(ListCheckListener listener);

  public void removeListCheckListener(ListCheckListener listener);

  public ListCheckListener[] getListCheckListeners();

  public List<T> getCheckeds();

  public int getLocksCount();

  public int getChecksCount();

}
