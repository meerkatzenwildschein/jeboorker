package org.japura.gui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * <P>
 * Copyright (C) 2011 Carlos Eduardo Leite de Andrade
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
class CompoundComponent extends JLayeredPane implements MouseListener{

  private JComponent component;
  private JPanel glassPanel;
  private MouseListener mouseHandler;

  public CompoundComponent(JComboBox component) {
	this(component, null);
  }

  public CompoundComponent(JComboBox component, MouseListener mouseListener) {
	glassPanel = new JPanel();
	glassPanel.setOpaque(false);
	this.mouseHandler = mouseListener;
	this.component = component;
	add(component, Integer.valueOf(JLayeredPane.DEFAULT_LAYER));
	add(glassPanel, Integer.valueOf(JLayeredPane.DEFAULT_LAYER + 1));
	glassPanel.addMouseListener(this);
  }

  @Override
  public Dimension getPreferredSize() {
	return component.getPreferredSize();
  }

  @Override
  public void setEnabled(boolean enabled) {
	super.setEnabled(enabled);
	component.setEnabled(enabled);
	glassPanel.setEnabled(enabled);
  }

  @Override
  public void doLayout() {
	component.setBounds(0, 0, getWidth(), getHeight());
	glassPanel.setBounds(0, 0, getWidth(), getHeight());
  }

  @Override
  public void mouseReleased(MouseEvent e) {
	if (isEnabled()) {
	  if (mouseHandler != null) {
		e.setSource(component);
		mouseHandler.mouseReleased(e);
	  }
	}
  }

  @Override
  public void mousePressed(MouseEvent e) {
	if (isEnabled()) {
	  component.requestFocusInWindow();
	  if (mouseHandler != null) {
		e.setSource(component);
		mouseHandler.mousePressed(e);
	  }
	}
  }

  @Override
  public void mouseExited(MouseEvent e) {
	if (isEnabled()) {
	  e.setSource(component);
	  component.dispatchEvent(e);
	  if (mouseHandler != null) {
		mouseHandler.mouseExited(e);
	  }
	}
  }

  @Override
  public void mouseEntered(MouseEvent e) {
	if (isEnabled()) {
	  e.setSource(component);
	  component.dispatchEvent(e);
	  if (mouseHandler != null) {
		mouseHandler.mouseEntered(e);
	  }
	}
  }

  @Override
  public void mouseClicked(MouseEvent e) {
	if (isEnabled()) {
	  if (mouseHandler != null) {
		e.setSource(component);
		mouseHandler.mouseClicked(e);
	  }
	}
  }

}
