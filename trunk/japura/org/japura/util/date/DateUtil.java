package org.japura.util.date;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * 
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
public final class DateUtil{

  private static synchronized boolean validateDateWithRegex(String date,
															DateMask mask,
															DateSeparator separator) {
	String dayOrMonth = "[0-9]{1,2}";
	String year = "[0-9]{4}";
	String regex = "";
	String sep = "\\" + separator.getSeparatorString();
	if (mask.equals(DateMask.MMDDYYYY) || mask.equals(DateMask.DDMMYYYY)) {
	  regex = dayOrMonth + sep + dayOrMonth + sep + year;
	} else if (mask.equals(DateMask.YYYYMMDD) || mask.equals(DateMask.YYYYDDMM)) {
	  regex = year + sep + dayOrMonth + sep + dayOrMonth;
	}
	if (date != null && date.matches(regex)) {
	  return true;
	}
	return false;
  }

  public static synchronized boolean validate(String date, DateMask mask,
											  DateSeparator separator) {
	return validateDateWithRegex(date, mask, separator);
  }

  public static synchronized Date toDate(String date, Locale locale,
										 DateSeparator separator) {
	DateMask mask = DateMask.getMask(locale);
	return toDate(date, mask, separator);
  }

  public static synchronized Date toDate(String date, DateMask mask,
										 DateSeparator separator) {
	if (validateDateWithRegex(date, mask, separator)) {
	  int year = 0;
	  int month = 0;
	  int day = 0;
	  String[] split = date.split("\\" + separator.getSeparatorString());
	  if (mask == DateMask.DDMMYYYY) {
		day = Integer.parseInt(split[0]);
		month = Integer.parseInt(split[1]);
		year = Integer.parseInt(split[2]);
	  } else if (mask == DateMask.MMDDYYYY) {
		month = Integer.parseInt(split[0]);
		day = Integer.parseInt(split[1]);
		year = Integer.parseInt(split[2]);
	  } else if (mask == DateMask.YYYYMMDD) {
		year = Integer.parseInt(split[0]);
		month = Integer.parseInt(split[1]);
		day = Integer.parseInt(split[2]);
	  } else if (mask == DateMask.YYYYDDMM) {
		year = Integer.parseInt(split[0]);
		day = Integer.parseInt(split[1]);
		month = Integer.parseInt(split[2]);
	  }
	  try {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setLenient(false);
		gc.clear();
		gc.set(GregorianCalendar.DAY_OF_MONTH, day);
		gc.set(GregorianCalendar.MONTH, month - 1);
		gc.set(GregorianCalendar.YEAR, year);
		return gc.getTime();
	  } catch (Exception e) {
		return null;
	  }
	}

	return null;
  }

  public static synchronized String toString(Locale locale,
											 DateSeparator separator, long date) {
	DateMask mask = DateMask.getMask(locale);
	return toString(mask, separator, date);
  }

  public static synchronized String toString(DateMask mask,
											 DateSeparator separator, long date) {
	Calendar cal = new GregorianCalendar();
	cal.setTimeInMillis(date);
	int year = cal.get(Calendar.YEAR);
	int month = cal.get(Calendar.MONTH) + 1;
	int day = cal.get(Calendar.DAY_OF_MONTH);
	return toString(mask, separator, day, month, year);
  }

  public static synchronized String toString(DateMask mask,
											 DateSeparator separator, int day,
											 int month, int year) {
	String sep = separator.getSeparatorString();
	String strDate = "";
	if (day > 0 && month > 0 && year > 0) {
	  if (mask.equals(DateMask.DDMMYYYY)) {
		strDate = toString(day) + sep + toString(month) + sep + toString(year);
	  } else if (mask.equals(DateMask.MMDDYYYY)) {
		strDate = toString(month) + sep + toString(day) + sep + toString(year);
	  } else if (mask.equals(DateMask.YYYYMMDD)) {
		strDate = toString(year) + sep + toString(month) + sep + toString(day);
	  } else if (mask.equals(DateMask.YYYYDDMM)) {
		strDate = toString(year) + sep + toString(day) + sep + toString(month);
	  }
	}
	return strDate;
  }

  private static synchronized String toString(int value) {
	if (value >= 0 && value <= 9) {
	  return "0" + value;
	}
	return Integer.toString(value);
  }
}
