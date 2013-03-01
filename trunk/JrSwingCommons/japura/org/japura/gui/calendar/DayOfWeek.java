package org.japura.gui.calendar;

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
public enum DayOfWeek {
  MONDAY(java.util.Calendar.MONDAY),
  TUESDAY(java.util.Calendar.TUESDAY),
  WEDNESDAY(java.util.Calendar.WEDNESDAY),
  THURSDAY(java.util.Calendar.THURSDAY),
  FRIDAY(java.util.Calendar.FRIDAY),
  SATURDAY(java.util.Calendar.SATURDAY),
  SUNDAY(java.util.Calendar.SUNDAY);

  private int juDayOfWeek;

  private DayOfWeek(int juDayOfWeek) {
	this.juDayOfWeek = juDayOfWeek;
  }

  int getJUDayOfWeek() {
	return juDayOfWeek;
  }

  protected DayOfWeek nextDayOfWeek() {
	if (this.equals(DayOfWeek.MONDAY)) {
	  return DayOfWeek.TUESDAY;
	}
	if (this.equals(DayOfWeek.TUESDAY)) {
	  return DayOfWeek.WEDNESDAY;
	}
	if (this.equals(DayOfWeek.WEDNESDAY)) {
	  return DayOfWeek.THURSDAY;
	}
	if (this.equals(DayOfWeek.THURSDAY)) {
	  return DayOfWeek.FRIDAY;
	}
	if (this.equals(DayOfWeek.FRIDAY)) {
	  return DayOfWeek.SATURDAY;
	}
	if (this.equals(DayOfWeek.SATURDAY)) {
	  return DayOfWeek.SUNDAY;
	}
	return DayOfWeek.MONDAY;
  }

  public static DayOfWeek getDayOfWeek(int dayOfWeek) {
	for (DayOfWeek dow : values()) {
	  if (dow.getJUDayOfWeek() == dayOfWeek) {
		return dow;
	  }
	}
	return null;
  }
}