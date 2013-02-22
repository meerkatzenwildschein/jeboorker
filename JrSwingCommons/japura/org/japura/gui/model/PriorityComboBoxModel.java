package org.japura.gui.model;

import java.util.List;

import javax.swing.MutableComboBoxModel;

import org.japura.gui.event.PriorityComboBoxListener;

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
public interface PriorityComboBoxModel extends MutableComboBoxModel{

  public void addElement(Object item, boolean priorityItem);

  public void addToPriority(Object obj);

  public void removeFromPriority(Object obj);

  public void clearPriorities();

  public void setPriorityItemsCapacity(int capacity);

  public void setMinimalSizeForPriority(int size);

  public int getMinimalSizeForPriority();

  public boolean isPriorityAvailable();

  public boolean isPriorityItem(Object obj);

  public int getPriorityItemsCapacity();

  public List<Object> getPriorityItems();

  public int getPriorityItemsSize();

  public void addPriorityComboBoxListener(PriorityComboBoxListener listener);

  public void removePriorityComboBoxListener(PriorityComboBoxListener listener);

  public void setIncreasePriorityOnSelection(boolean enable);

  public boolean isIncreasePriorityOnSelection();

}
