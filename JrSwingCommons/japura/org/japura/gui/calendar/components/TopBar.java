package org.japura.gui.calendar.components;

import javax.swing.JPanel;

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
public class TopBar extends JPanel implements CalendarComponent{

  private static final long serialVersionUID = -766746307801523936L;

  private Calendar calendar;

  public TopBar(Calendar calendar) {
	this.calendar = calendar;
  }

  @Override
  public Calendar getCalendar() {
	return calendar;
  }

  @Override
  public CalendarComponentType getType() {
	return CalendarComponentType.TOP_BAR;
  }

}
