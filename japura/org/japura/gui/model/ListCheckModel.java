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
public interface ListCheckModel extends ListModel{

  @Override
  public Object getElementAt(int index);

  public boolean contains(Object value);

  public void addElement(Object... values);

  public void setElement(Object... values);

  public void addElement(int index, Object value);

  public void removeElement(Object... values);

  public void addListModelListener(ListModelListener listener);

  public void removeListModelListener(ListModelListener listener);

  public ListModelListener[] getListModelListeners();

  public void addListLockListener(ListLockListener listener);

  public void removeListLockListener(ListLockListener listener);

  public ListLockListener[] getListLockListeners();

  public List<Object> getLockeds();

  public void setLock(Object... values);

  public void removeLock(Object... values);

  public void addLock(Object... values);

  /**
   * Returns true if the specified value is locked.
   */
  public boolean isLocked(Object value);

  public void setCheck(Object... values);

  public void removeCheck(Object... values);

  public void addCheck(Object... values);

  /**
   * Returns true if the specified value is checked.
   */
  public boolean isChecked(Object value);

  public void clear();

  public void checkAll();

  public void lockAll();

  public void removeChecks();

  public void removeLocks();

  public void addListCheckListener(ListCheckListener listener);

  public void removeListCheckListener(ListCheckListener listener);

  public ListCheckListener[] getListCheckListeners();

  public List<Object> getCheckeds();

  public int getLocksCount();

  public int getChecksCount();

}
