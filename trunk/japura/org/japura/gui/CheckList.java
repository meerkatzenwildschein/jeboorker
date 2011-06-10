package org.japura.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.japura.gui.event.ListCheckListener;
import org.japura.gui.event.ListEvent;
import org.japura.gui.event.ListLockListener;
import org.japura.gui.event.ListModelListener;
import org.japura.gui.model.DefaultListCheckModel;
import org.japura.gui.model.ListCheckModel;
import org.japura.gui.renderer.CheckListRenderer;

/**
 * List with CheckBoxes
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
public class CheckList extends JList{

  private static final long serialVersionUID = 7562297704191604289L;
  private ListCheckModel model;
  private PopupMenuBuilder<CheckList> popupMenuBuilder;

  public CheckList() {
	setCellRenderer(new CheckListRenderer());
	setModel(new DefaultListCheckModel());

	MouseMotionListener[] mmls = getMouseMotionListeners();
	for (MouseMotionListener mml : mmls) {
	  removeMouseMotionListener(mml);
	}

	MouseListener[] listeners = getMouseListeners();
	for (MouseListener ml : listeners) {
	  removeMouseListener(ml);
	}

	ToolTipManager.sharedInstance().registerComponent(this);

	addMouseListener(new MouseAdapter() {
	  @Override
	  public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
		  int index = locationToIndex(e.getPoint());
		  if (index > -1) {
			Object obj = getModel().getElementAt(index);
			if (getModel().isLocked(obj) == false) {
			  boolean checked = getModel().isChecked(obj);
			  if (checked) {
				getModel().removeCheck(obj);
			  } else {
				getModel().addCheck(obj);
			  }
			}
		  }
		} else if (SwingUtilities.isRightMouseButton(e)
			&& getPopupMenuBuilder() != null) {
		  JPopupMenu pm = getPopupMenuBuilder().buildPopupMenu(CheckList.this);
		  if (pm != null) {
			pm.show(CheckList.this, e.getX(), e.getY());
		  }
		}
	  }
	});
  }

  @Override
  public ListCheckModel getModel() {
	return model;
  }

  @Override
  public void setCellRenderer(ListCellRenderer cellRenderer) {
	if (cellRenderer instanceof CheckListRenderer) {
	  super.setCellRenderer(cellRenderer);
	}
  }

  public void setCellRenderer(CheckListRenderer cellRenderer) {
	super.setCellRenderer(cellRenderer);
  }

  @Override
  public CheckListRenderer getCellRenderer() {
	return (CheckListRenderer) super.getCellRenderer();
  }

  public void setPopupMenuBuilder(PopupMenuBuilder<CheckList> popupMenuBuilder) {
	this.popupMenuBuilder = popupMenuBuilder;
  }

  public PopupMenuBuilder<CheckList> getPopupMenuBuilder() {
	return popupMenuBuilder;
  }

  @Override
  public void setModel(ListModel model) {
	if (model instanceof ListCheckModel) {
	  setModel((ListCheckModel) model);
	} else {
	  throw new IllegalArgumentException("model must be ListCheckModel");
	}
  }

  public void setModel(ListCheckModel model) {
	if (model == null) {
	  throw new IllegalArgumentException("model must be non null");
	}
	ListCheckModel oldValue = getModel();
	this.model = model;
	if (oldValue != null) {
	  firePropertyChange("model", oldValue, model);
	}

	model.addListModelListener(new ListModelListener() {
	  @Override
	  public void valueAdded(ListEvent e) {
		repaint();
	  }

	  @Override
	  public void valueRemoved(ListEvent e) {
		repaint();
	  }
	});

	model.addListCheckListener(new ListCheckListener() {
	  @Override
	  public void addCheck(ListEvent event) {
		repaint();
	  }

	  @Override
	  public void removeCheck(ListEvent event) {
		repaint();
	  }
	});

	model.addListLockListener(new ListLockListener() {
	  @Override
	  public void addLock(ListEvent event) {
		repaint();
	  }

	  @Override
	  public void removeLock(ListEvent event) {
		repaint();
	  }
	});
	repaint();
  }
}
