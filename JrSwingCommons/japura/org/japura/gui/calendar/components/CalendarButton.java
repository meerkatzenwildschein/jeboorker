package org.japura.gui.calendar.components;

import org.japura.gui.ArrowButton;
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
public class CalendarButton extends ArrowButton implements CalendarComponent{

  private static final long serialVersionUID = 2413025541462159947L;
  private Calendar calendar;
  private CalendarComponentType type;

  public CalendarButton(Calendar calendar, CalendarComponentType type) {
	this.calendar = calendar;
	this.type = type;
	if (type.equals(CalendarComponentType.PREVIOUS_YEAR_BUTTON)) {
	  setArrowType(ArrowButton.DOUBLE_LEFT);
	} else if (type.equals(CalendarComponentType.PREVIOUS_MONTH_BUTTON)) {
	  setArrowType(ArrowButton.LEFT);
	} else if (type.equals(CalendarComponentType.NEXT_YEAR_BUTTON)) {
	  setArrowType(ArrowButton.DOUBLE_RIGHT);
	} else if (type.equals(CalendarComponentType.NEXT_MONTH_BUTTON)) {
	  setArrowType(ArrowButton.RIGHT);
	} else {
	  throw new RuntimeException("Illegal CalendarComponentType: "
		  + type.name());
	}
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
