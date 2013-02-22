package org.japura.gui.renderer;

import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.japura.gui.CheckList;
import org.japura.gui.model.ListCheckModel;

/**
 * Renderer for the CheckList
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
 */
public class CheckListRenderer extends JCheckBox implements ListCellRenderer{

  /**
   * Gets the text of the value
   * 
   * @param value
   *          Object
   * @return String
   */
  public String getText(Object value) {
	return value.toString();
  }

  public CheckListRenderer() {
	setBackground(UIManager.getColor("List.background"));
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value,
												int index, boolean isSelected,
												boolean cellHasFocus) {

	if (list instanceof CheckList) {
	  CheckList cbl = (CheckList) list;
	  ListCheckModel model = cbl.getModel();
	  boolean checked = model.isChecked(value);
	  boolean locked = model.isLocked(value);
	  setSelected(checked);

	  if (locked || cbl.isEnabled() == false) {
		setEnabled(false);
	  } else {
		setEnabled(true);
	  }
	}
	setText(getText(value));
	return this;

  }

  /**
   * Override method to improve performace
   */
  @Override
  public void validate() {}

  /**
   * Override method to improve performace
   */
  @Override
  public void invalidate() {}

  /**
   * Override method to improve performace
   */
  @Override
  public void repaint() {}

  /**
   * Override method to improve performace
   */
  @Override
  public void revalidate() {}

  /**
   * Override method to improve performace
   */
  @Override
  public void repaint(long tm, int x, int y, int width, int height) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void repaint(Rectangle r) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, byte oldValue,
								 byte newValue) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, char oldValue,
								 char newValue) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, short oldValue,
								 short newValue) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, long oldValue,
								 long newValue) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, float oldValue,
								 float newValue) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, double oldValue,
								 double newValue) {}

  /**
   * Override method to improve performace
   */
  @Override
  public void firePropertyChange(String propertyName, boolean oldValue,
								 boolean newValue) {}
}
