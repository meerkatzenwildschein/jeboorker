package org.japura.gui.calendar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

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
public class DefaultPropertiesProvider implements PropertiesProvider{

  private CalendarProperties properties;

  public DefaultPropertiesProvider() {
	properties = new CalendarProperties();
  }

  public CalendarProperties getProperties() {
	return properties;
  }

  @Override
  public Color getMouseOverForeground(CalendarComponent component) {
	CalendarComponentType type = component.getType();
	if (type.equals(CalendarComponentType.PREVIOUS_YEAR_BUTTON)) {
	  return getProperties().getMouseOverButtonColor();
	} else if (type.equals(CalendarComponentType.PREVIOUS_MONTH_BUTTON)) {
	  return getProperties().getMouseOverButtonColor();
	} else if (type.equals(CalendarComponentType.NEXT_YEAR_BUTTON)) {
	  return getProperties().getMouseOverButtonColor();
	} else if (type.equals(CalendarComponentType.NEXT_MONTH_BUTTON)) {
	  return getProperties().getMouseOverButtonColor();
	}
	return Color.LIGHT_GRAY;
  }

  @Override
  public Color getDisabledForeground(CalendarComponent component) {
	CalendarComponentType type = component.getType();
	if (type.equals(CalendarComponentType.PREVIOUS_YEAR_BUTTON)) {
	  return getProperties().getDisabledButtonColor();
	} else if (type.equals(CalendarComponentType.PREVIOUS_MONTH_BUTTON)) {
	  return getProperties().getDisabledButtonColor();
	} else if (type.equals(CalendarComponentType.NEXT_YEAR_BUTTON)) {
	  return getProperties().getDisabledButtonColor();
	} else if (type.equals(CalendarComponentType.NEXT_MONTH_BUTTON)) {
	  return getProperties().getDisabledButtonColor();
	}
	return Color.LIGHT_GRAY;
  }

  @Override
  public Color getForeground(CalendarComponent component) {
	CalendarComponentType type = component.getType();
	Calendar calendar = component.getCalendar();
	if (type.equals(CalendarComponentType.DAY_MONTH)) {
	  if (calendar.isSelected(component)) {
		return getProperties().getSelectedDayOfMonthForeground();
	  } else if (calendar.isCurrentMonth(component)) {
		return getProperties().getDayOfMonthForeground();
	  } else {
		return getProperties().getDayOfNonCurrentMonthForeground();
	  }
	} else if (type.equals(CalendarComponentType.DAY_WEEK_HEADER)) {
	  DayOfWeek dow = calendar.getDayOfWeek(component);
	  return getProperties().getDayOfWeekForeground(dow);
	} else if (type.equals(CalendarComponentType.PREVIOUS_YEAR_BUTTON)) {
	  return getProperties().getButtonColor();
	} else if (type.equals(CalendarComponentType.PREVIOUS_MONTH_BUTTON)) {
	  return getProperties().getButtonColor();
	} else if (type.equals(CalendarComponentType.NEXT_YEAR_BUTTON)) {
	  return getProperties().getButtonColor();
	} else if (type.equals(CalendarComponentType.NEXT_MONTH_BUTTON)) {
	  return getProperties().getButtonColor();
	} else if (type.equals(CalendarComponentType.MONTH_LABEL)) {
	  return getProperties().getMonthForeground();
	} else if (type.equals(CalendarComponentType.YEAR_LABEL)) {
	  return getProperties().getYearForeground();
	}
	return Color.BLACK;
  }

  @Override
  public Color getBackground(CalendarComponent component) {
	CalendarComponentType type = component.getType();
	Calendar calendar = component.getCalendar();
	if (type.equals(CalendarComponentType.DAY_MONTH)) {
	  if (calendar.isSelected(component)) {
		return getProperties().getSelectedDayOfMonthBackground();
	  } else if (calendar.isCurrentMonth(component)) {
		return getProperties().getDayOfMonthBackground();
	  } else {
		return getProperties().getDayOfNonCurrentMonthBackground();
	  }
	} else if (type.equals(CalendarComponentType.DAY_WEEK_HEADER)) {
	  DayOfWeek dow = calendar.getDayOfWeek(component);
	  return getProperties().getDayOfWeekBackground(dow);
	} else if (type.equals(CalendarComponentType.TOP_BAR)) {
	  return getProperties().getTopBarBackground();
	}
	return Color.WHITE;
  }

  @Override
  public DayOfWeek getStartDayOfWeek() {
	return getProperties().getStartDayOfWeek();
  }

  @Override
  public Insets getDayOfMonthMargin() {
	return getProperties().getDayOfMonthMargin();
  }

  @Override
  public Font getDayOfMonthFont() {
	return getProperties().getDayOfMonthFont();
  }

  @Override
  public Insets getDayOfWeekMargin() {
	return getProperties().getDayOfWeekMargin();
  }

  @Override
  public Font getDayOfWeekFont() {
	return getProperties().getDayOfWeekFont();
  }

  @Override
  public Color getTopDayOfWeekSeparatorColor() {
	return getProperties().getTopDayOfWeekSeparatorColor();
  }

  @Override
  public Color getBottomDayOfWeekSeparatorColor() {
	return getProperties().getBottomDayOfWeekSeparatorColor();
  }

}
