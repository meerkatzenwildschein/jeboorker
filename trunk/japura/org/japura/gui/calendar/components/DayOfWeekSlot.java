package org.japura.gui.calendar.components;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.japura.gui.PopupMenuBuilder;
import org.japura.gui.calendar.Calendar;
import org.japura.gui.calendar.CalendarComponent;
import org.japura.gui.calendar.CalendarComponentType;
import org.japura.gui.calendar.DayOfWeek;
import org.japura.gui.calendar.PropertiesProvider;

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
public class DayOfWeekSlot extends CalendarSlot implements MouseListener{
  private static final long serialVersionUID = 7413497097851129819L;
  private DayOfWeek dayOfWeek;

  public DayOfWeekSlot(Calendar calendar, String name, DayOfWeek dayOfWeek) {
	super(calendar, CalendarComponentType.DAY_WEEK_HEADER);
	this.dayOfWeek = dayOfWeek;
	setBorder(buildBorder());
	setText(name);
	setFont(getCalendar().getPropertiesProvider().getDayOfWeekFont());
	addMouseListener(this);
  }

  private Border buildBorder() {
	PropertiesProvider pp = getCalendar().getPropertiesProvider();

	Insets margin = pp.getDayOfWeekMargin();
	int t = margin.top;
	int l = margin.left;
	int b = margin.bottom;
	int r = margin.right;

	Border ini =
		BorderFactory.createMatteBorder(1, 0, 0, 0,
			pp.getTopDayOfWeekSeparatorColor());
	Border outi =
		BorderFactory.createMatteBorder(0, 0, 1, 0,
			pp.getBottomDayOfWeekSeparatorColor());

	Border in = BorderFactory.createEmptyBorder(t, l, b, r);
	Border out = BorderFactory.createCompoundBorder(outi, ini);
	return BorderFactory.createCompoundBorder(out, in);
  }

  public DayOfWeek getDayOfWeek() {
	return dayOfWeek;
  }

  @Override
  public Color getForeground() {
	if (dayOfWeek == null) {
	  return super.getForeground();
	}
	return getCalendar().getPropertiesProvider().getForeground(this);
  }

  @Override
  public Color getBackground() {
	if (dayOfWeek == null) {
	  return super.getBackground();
	}
	return getCalendar().getPropertiesProvider().getBackground(this);
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {
	if (SwingUtilities.isRightMouseButton(e)) {
	  Calendar calendar = getCalendar();
	  PopupMenuBuilder<CalendarComponent> pmb = calendar.getPopupMenuBuilder();
	  if (pmb != null) {
		JPopupMenu pm = pmb.buildPopupMenu(this);
		if (pm != null) {
		  pm.show(this, e.getX(), e.getY());
		}
	  }
	}
  }

  @Override
  public void mouseReleased(MouseEvent e) {}

}
