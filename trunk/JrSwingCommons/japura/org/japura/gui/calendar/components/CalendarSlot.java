package org.japura.gui.calendar.components;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.japura.gui.calendar.Calendar;
import org.japura.gui.calendar.CalendarComponent;
import org.japura.gui.calendar.CalendarComponentType;

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
public class CalendarSlot extends JLabel implements CalendarComponent{
  private static final long serialVersionUID = 5268878959819979307L;

  private Calendar calendar;
  private CalendarComponentType type;

  public CalendarSlot(Calendar calendar, CalendarComponentType type) {
	this.calendar = calendar;
	this.type = type;
	setHorizontalAlignment(SwingConstants.CENTER);
	setOpaque(true);
	setBackground(Color.WHITE);
  }

  @Override
  public Calendar getCalendar() {
	return calendar;
  }

  @Override
  public CalendarComponentType getType() {
	return type;
  }

}
