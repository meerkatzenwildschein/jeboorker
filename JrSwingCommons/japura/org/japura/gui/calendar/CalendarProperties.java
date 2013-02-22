package org.japura.gui.calendar;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.HashMap;

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
public final class CalendarProperties{

  private static CalendarProperties dcp;

  public static CalendarProperties getDefaultCalendarProperties() {
	if (dcp == null) {
	  dcp = buildCalendarProperties();
	}
	return dcp;
  }

  private static CalendarProperties buildCalendarProperties() {
	CalendarProperties cp = new CalendarProperties();
	cp.getFromDefault = false;

	Color color = new Color(50, 122, 244);
	Color color2 = new Color(0, 80, 160);
	Color color3 = new Color(0, 95, 190);

	cp.setDayOfWeekForeground(DayOfWeek.MONDAY, Color.WHITE);
	cp.setDayOfWeekForeground(DayOfWeek.TUESDAY, Color.WHITE);
	cp.setDayOfWeekForeground(DayOfWeek.WEDNESDAY, Color.WHITE);
	cp.setDayOfWeekForeground(DayOfWeek.THURSDAY, Color.WHITE);
	cp.setDayOfWeekForeground(DayOfWeek.FRIDAY, Color.WHITE);
	cp.setDayOfWeekForeground(DayOfWeek.SATURDAY, Color.WHITE);
	cp.setDayOfWeekForeground(DayOfWeek.SUNDAY, Color.WHITE);

	cp.setDayOfWeekBackground(DayOfWeek.MONDAY, color3);
	cp.setDayOfWeekBackground(DayOfWeek.TUESDAY, color3);
	cp.setDayOfWeekBackground(DayOfWeek.WEDNESDAY, color3);
	cp.setDayOfWeekBackground(DayOfWeek.THURSDAY, color3);
	cp.setDayOfWeekBackground(DayOfWeek.FRIDAY, color3);
	cp.setDayOfWeekBackground(DayOfWeek.SATURDAY, color);
	cp.setDayOfWeekBackground(DayOfWeek.SUNDAY, color);

	cp.setDayOfWeekMargin(new Insets(2, 2, 2, 2));
	cp.setDayOfMonthMargin(new Insets(2, 2, 2, 2));

	cp.setDayOfMonthBackground(Color.WHITE);
	cp.setDayOfMonthForeground(Color.BLACK);

	cp.setDayOfNonCurrentMonthBackground(Color.WHITE);
	cp.setDayOfNonCurrentMonthForeground(Color.LIGHT_GRAY);

	cp.setSelectedDayOfMonthBackground(new Color(146, 190, 255));
	cp.setSelectedDayOfMonthForeground(Color.BLACK);

	cp.setDayOfMonthFont(new Font("Dialog", Font.PLAIN, 12));
	cp.setDayOfWeekFont(new Font("Dialog", Font.BOLD, 12));

	cp.setButtonColor(Color.WHITE);
	cp.setMouseOverButtonColor(new Color(220, 220, 255));
	cp.setDisabledButtonColor(Color.LIGHT_GRAY);
	cp.setMonthForeground(Color.WHITE);
	cp.setYearForeground(Color.WHITE);

	cp.setTopBarBackground(color2);

	cp.setTopDayOfWeekSeparatorColor(Color.WHITE);
	cp.setBottomDayOfWeekSeparatorColor(Color.BLACK);

	cp.setStartDayOfWeek(DayOfWeek.MONDAY);
	return cp;
  }

  public static void setDefaultCalendarProperties(CalendarProperties calendarProperties) {
	calendarProperties.getFromDefault = false;
	CalendarProperties.dcp = calendarProperties;
  }

  boolean getFromDefault = true;
  private HashMap<DayOfWeek, Color> dayOfWeekForegrounds;
  private HashMap<DayOfWeek, Color> dayOfWeekBackgrounds;
  private Insets dayOfWeekMargin;
  private Color dayOfMonthBackground;
  private Color dayOfMonthForeground;
  private Color dayOfNonCurrentMonthBackground;
  private Color dayOfNonCurrentMonthForeground;
  private Insets dayOfMonthMargin;
  private Font dayOfMonthFont;
  private Font dayOfWeekFont;
  private Color selectedDayOfMonthBackground;
  private Color selectedDayOfMonthForeground;
  private Color buttonColor;
  private Color disabledButtonColor;
  private Color mouseOverButtonColor;
  private Color monthForeground;
  private Color yearForeground;
  private Color topBarBackground;
  private Color topDayOfWeekSeparatorColor;
  private Color bottomDayOfWeekSeparatorColor;
  private DayOfWeek startDayOfWeek;

  public CalendarProperties() {
	dayOfWeekBackgrounds = new HashMap<DayOfWeek, Color>();
	dayOfWeekForegrounds = new HashMap<DayOfWeek, Color>();
  }

  public void setDayOfWeekForeground(DayOfWeek dayOfWeek, Color color) {
	if (color != null) {
	  dayOfWeekForegrounds.put(dayOfWeek, color);
	}
  }

  public void setDayOfWeekBackground(DayOfWeek dayOfWeek, Color color) {
	if (color != null) {
	  dayOfWeekBackgrounds.put(dayOfWeek, color);
	}
  }

  public void setDayOfWeekMargin(Insets margin) {
	if (margin != null) {
	  margin = validateMargin(margin);
	  this.dayOfWeekMargin = margin;
	}
  }

  private Insets validateMargin(Insets margin) {
	margin.left = Math.max(margin.left, 0);
	margin.right = Math.max(margin.right, 0);
	margin.top = Math.max(margin.top, 0);
	margin.bottom = Math.max(margin.bottom, 0);
	return margin;
  }

  public Color getDayOfWeekForeground(DayOfWeek dayOfWeek) {
	Color color = dayOfWeekForegrounds.get(dayOfWeek);
	if (getFromDefault && color == null) {
	  color = getDefaultCalendarProperties().getDayOfWeekForeground(dayOfWeek);
	  if (color == null) {
		throw new RuntimeException("Null foreground color for "
			+ dayOfWeek.name());
	  }
	}
	return color;
  }

  public Color getDayOfWeekBackground(DayOfWeek dayOfWeek) {
	Color color = dayOfWeekBackgrounds.get(dayOfWeek);
	if (getFromDefault && color == null) {
	  color = getDefaultCalendarProperties().getDayOfWeekBackground(dayOfWeek);
	  if (color == null) {
		throw new RuntimeException("Null background color for " + dayOfWeek);
	  }
	}
	return color;
  }

  public Insets getDayOfWeekMargin() {
	if (getFromDefault && dayOfWeekMargin == null) {
	  if (getDefaultCalendarProperties().getDayOfWeekMargin() != null) {
		return getDefaultCalendarProperties().getDayOfWeekMargin();
	  }
	  throw new RuntimeException("Null margin for day of week ");
	}
	return dayOfWeekMargin;
  }

  public Color getDayOfMonthBackground() {
	if (getFromDefault && dayOfMonthBackground == null) {
	  if (getDefaultCalendarProperties().getDayOfMonthBackground() == null) {
		throw new RuntimeException("Null background color for day of month");
	  }
	  return getDefaultCalendarProperties().getDayOfMonthBackground();
	}
	return dayOfMonthBackground;
  }

  public void setDayOfMonthBackground(Color dayOfMonthBackground) {
	this.dayOfMonthBackground = dayOfMonthBackground;
  }

  public Color getDayOfMonthForeground() {
	if (getFromDefault && dayOfMonthForeground == null) {
	  if (getDefaultCalendarProperties().getDayOfMonthForeground() == null) {
		throw new RuntimeException("Null foreground color for day of month");
	  }
	  return getDefaultCalendarProperties().getDayOfMonthForeground();
	}
	return dayOfMonthForeground;
  }

  public void setDayOfMonthForeground(Color dayOfMonthForeground) {
	this.dayOfMonthForeground = dayOfMonthForeground;
  }

  public Color getDayOfNonCurrentMonthBackground() {
	if (getFromDefault && dayOfNonCurrentMonthBackground == null) {
	  if (getDefaultCalendarProperties().getDayOfNonCurrentMonthBackground() == null) {
		throw new RuntimeException("Null background color for day of month");
	  }
	  return getDefaultCalendarProperties().getDayOfNonCurrentMonthBackground();
	}
	return dayOfNonCurrentMonthBackground;
  }

  public void setDayOfNonCurrentMonthBackground(Color color) {
	this.dayOfNonCurrentMonthBackground = color;
  }

  public Color getDayOfNonCurrentMonthForeground() {
	if (getFromDefault && dayOfNonCurrentMonthForeground == null) {
	  if (getDefaultCalendarProperties().getDayOfNonCurrentMonthForeground() == null) {
		throw new RuntimeException("Null foreground color for day of month");
	  }
	  return getDefaultCalendarProperties().getDayOfNonCurrentMonthForeground();
	}
	return dayOfNonCurrentMonthForeground;
  }

  public void setDayOfNonCurrentMonthForeground(Color color) {
	this.dayOfNonCurrentMonthForeground = color;
  }

  public Insets getDayOfMonthMargin() {
	if (getFromDefault && dayOfMonthMargin == null) {
	  if (getDefaultCalendarProperties().getDayOfMonthMargin() == null) {
		throw new RuntimeException("Null margin for day of month");
	  }
	  return getDefaultCalendarProperties().getDayOfMonthMargin();
	}
	return dayOfMonthMargin;
  }

  public void setDayOfMonthMargin(Insets margin) {
	margin = validateMargin(margin);
	this.dayOfMonthMargin = margin;
  }

  public Color getSelectedDayOfMonthBackground() {
	if (getFromDefault && selectedDayOfMonthBackground == null) {
	  if (getDefaultCalendarProperties().getSelectedDayOfMonthBackground() == null) {
		throw new RuntimeException(
			"Null background color for selected day of month");
	  }
	  return getDefaultCalendarProperties().getSelectedDayOfMonthBackground();
	}
	return selectedDayOfMonthBackground;
  }

  public void setSelectedDayOfMonthBackground(Color selectedDayOfMonthBackground) {
	this.selectedDayOfMonthBackground = selectedDayOfMonthBackground;
  }

  public Color getButtonColor() {
	if (getFromDefault && buttonColor == null) {
	  if (getDefaultCalendarProperties().getButtonColor() == null) {
		throw new RuntimeException("Null button color");
	  }
	  return getDefaultCalendarProperties().getButtonColor();
	}
	return buttonColor;
  }

  public void setButtonColor(Color buttonColor) {
	this.buttonColor = buttonColor;
  }

  public Color getDisabledButtonColor() {
	if (getFromDefault && disabledButtonColor == null) {
	  if (getDefaultCalendarProperties().getDisabledButtonColor() == null) {
		throw new RuntimeException("Null disabled button color");
	  }
	  return getDefaultCalendarProperties().getDisabledButtonColor();
	}
	return disabledButtonColor;
  }

  public void setDisabledButtonColor(Color color) {
	this.disabledButtonColor = color;
  }

  public Color getMouseOverButtonColor() {
	if (getFromDefault && mouseOverButtonColor == null) {
	  if (getDefaultCalendarProperties().getMouseOverButtonColor() == null) {
		throw new RuntimeException("Null mouse over button color");
	  }
	  return getDefaultCalendarProperties().getMouseOverButtonColor();
	}
	return mouseOverButtonColor;
  }

  public void setMouseOverButtonColor(Color color) {
	this.mouseOverButtonColor = color;
  }

  public Color getMonthForeground() {
	if (getFromDefault && monthForeground == null) {
	  if (getDefaultCalendarProperties().getMonthForeground() == null) {
		throw new RuntimeException("Null month foreground");
	  }
	  return getDefaultCalendarProperties().getMonthForeground();
	}
	return monthForeground;
  }

  public void setMonthForeground(Color monthForeground) {
	this.monthForeground = monthForeground;
  }

  public Color getYearForeground() {
	if (getFromDefault && yearForeground == null) {
	  if (getDefaultCalendarProperties().getYearForeground() == null) {
		throw new RuntimeException("Null year foreground");
	  }
	  return getDefaultCalendarProperties().getYearForeground();
	}
	return yearForeground;
  }

  public void setYearForeground(Color yearForeground) {
	this.yearForeground = yearForeground;
  }

  public Color getTopBarBackground() {
	if (getFromDefault && topBarBackground == null) {
	  if (getDefaultCalendarProperties().getTopBarBackground() == null) {
		throw new RuntimeException("Null top bar background");
	  }
	  return getDefaultCalendarProperties().getTopBarBackground();
	}
	return topBarBackground;
  }

  public void setTopBarBackground(Color topBarBackground) {
	this.topBarBackground = topBarBackground;
  }

  public Color getSelectedDayOfMonthForeground() {
	if (getFromDefault && selectedDayOfMonthForeground == null) {
	  if (getDefaultCalendarProperties().getSelectedDayOfMonthForeground() == null) {
		throw new RuntimeException(
			"Null foreground color for selected day of month");
	  }
	  return getDefaultCalendarProperties().getSelectedDayOfMonthForeground();
	}
	return selectedDayOfMonthForeground;
  }

  public void setSelectedDayOfMonthForeground(Color selectedDayOfMonthForeground) {
	this.selectedDayOfMonthForeground = selectedDayOfMonthForeground;
  }

  public Font getDayOfMonthFont() {
	if (getFromDefault && dayOfMonthFont == null) {
	  if (getDefaultCalendarProperties().getDayOfMonthFont() == null) {
		throw new RuntimeException("Null font for day of month");
	  }
	  return getDefaultCalendarProperties().getDayOfMonthFont();
	}
	return dayOfMonthFont;
  }

  public void setDayOfMonthFont(Font dayOfMonthFont) {
	this.dayOfMonthFont = dayOfMonthFont;
  }

  public Font getDayOfWeekFont() {
	if (getFromDefault && dayOfWeekFont == null) {
	  if (getDefaultCalendarProperties().getDayOfWeekFont() == null) {
		throw new RuntimeException("Null font for day of week");
	  }
	  return getDefaultCalendarProperties().getDayOfWeekFont();
	}
	return dayOfWeekFont;
  }

  public void setDayOfWeekFont(Font dayOfWeekFont) {
	this.dayOfWeekFont = dayOfWeekFont;
  }

  public Color getTopDayOfWeekSeparatorColor() {
	if (getFromDefault && topDayOfWeekSeparatorColor == null) {
	  if (getDefaultCalendarProperties().getTopDayOfWeekSeparatorColor() == null) {
		throw new RuntimeException("Null separator color for top day of week");
	  }
	  return getDefaultCalendarProperties().getTopDayOfWeekSeparatorColor();
	}
	return topDayOfWeekSeparatorColor;
  }

  public void setTopDayOfWeekSeparatorColor(Color topDayOfWeekSeparatorColor) {
	this.topDayOfWeekSeparatorColor = topDayOfWeekSeparatorColor;
  }

  public Color getBottomDayOfWeekSeparatorColor() {
	if (getFromDefault && bottomDayOfWeekSeparatorColor == null) {
	  if (getDefaultCalendarProperties().getBottomDayOfWeekSeparatorColor() == null) {
		throw new RuntimeException(
			"Null separator color for bottom day of week");
	  }
	  return getDefaultCalendarProperties().getBottomDayOfWeekSeparatorColor();
	}
	return bottomDayOfWeekSeparatorColor;
  }

  public void setBottomDayOfWeekSeparatorColor(Color bottomDayOfWeekSeparatorColor) {
	this.bottomDayOfWeekSeparatorColor = bottomDayOfWeekSeparatorColor;
  }

  public DayOfWeek getStartDayOfWeek() {
	if (getFromDefault && startDayOfWeek == null) {
	  if (getDefaultCalendarProperties().getStartDayOfWeek() == null) {
		throw new RuntimeException("Null start day of week");
	  }
	  return getDefaultCalendarProperties().getStartDayOfWeek();
	}
	return startDayOfWeek;
  }

  public void setStartDayOfWeek(DayOfWeek startDayOfWeek) {
	this.startDayOfWeek = startDayOfWeek;
  }

}
