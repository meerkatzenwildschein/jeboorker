package org.japura.gui.model;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.japura.gui.event.DateEvent;
import org.japura.gui.event.DateListener;
import org.japura.util.date.DateMask;
import org.japura.util.date.DateSeparator;
import org.japura.util.date.DateUtil;

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
public class DateDocument extends PlainDocument{

  private static final long serialVersionUID = 3692487946320420981L;

  private DateSeparator separator;

  private DateMask mask;
  private Locale locale;

  private String regex;

  private boolean autoCompleteEnabled = true;

  private Date oldDate;
  private boolean oldDateSetted;

  public DateDocument() {
	this(null, null);
  }

  public DateDocument(Locale locale, DateSeparator separator) {
	if (separator == null) {
	  this.separator = DateSeparator.SLASH;
	} else {
	  this.separator = separator;
	}
	this.mask = DateMask.getMask(locale);
	this.regex = buildRegex();
	setLocale(locale);
  }

  public Locale getLocale() {
	return locale;
  }

  private void setLocale(Locale locale) {
	if (locale == null) {
	  locale = Locale.getDefault();
	}
	this.locale = locale;
	mask = DateMask.getMask(locale);
  }

  public DateSeparator getSeparator() {
	return separator;
  }

  public boolean isAutoCompleteEnabled() {
	return autoCompleteEnabled;
  }

  public void setAutoCompleteEnabled(boolean enabled) {
	this.autoCompleteEnabled = enabled;
  }

  private String buildRegex() {
	String year = "([0-9]{1,4})?";
	String dayOrMonth = "([0-9]{1,2})?";
	String sep = "(\\" + getSeparator().getSeparatorString() + ")?";
	String regex = "";

	if (mask == DateMask.MMDDYYYY || mask == DateMask.DDMMYYYY) {
	  regex = dayOrMonth + sep + dayOrMonth + sep + year;
	} else if (mask == DateMask.YYYYMMDD || mask == DateMask.YYYYDDMM) {
	  regex = year + sep + dayOrMonth + sep + dayOrMonth;
	}
	return regex;
  }

  public boolean isValid() {
	if (getDate() != null) {
	  return true;
	}
	return false;
  }

  private GregorianCalendar getGregorianCalendar() {
	Date date = getDate();
	if (date != null) {
	  GregorianCalendar gc = new GregorianCalendar();
	  gc.setTimeInMillis(date.getTime());
	  return gc;
	}
	return null;
  }

  public Integer getDay() {
	GregorianCalendar gc = getGregorianCalendar();
	if (gc != null) {
	  return gc.get(GregorianCalendar.DAY_OF_MONTH);
	}
	return null;
  }

  public Integer getMonth() {
	GregorianCalendar gc = getGregorianCalendar();
	if (gc != null) {
	  return gc.get(GregorianCalendar.MONTH) + 1;
	}
	return null;
  }

  public Integer getYear() {
	GregorianCalendar gc = getGregorianCalendar();
	if (gc != null) {
	  return gc.get(GregorianCalendar.YEAR);
	}
	return null;
  }

  public Date getDate() {
	try {
	  String text = getText(0, getLength());
	  Date date = DateUtil.toDate(text, locale, getSeparator());
	  if (date != null) {
		return date;
	  }
	} catch (BadLocationException e) {
	  e.printStackTrace();
	}
	return null;
  }

  public void setDate(int day, int month, int year) {
	try {
	  replace(0, getLength(),
		  DateUtil.toString(mask, getSeparator(), day, month, year), null);
	} catch (BadLocationException e) {
	  e.printStackTrace();
	}
  }

  public void clear() {
	try {
	  remove(0, getLength());
	} catch (BadLocationException e) {
	  e.printStackTrace();
	}
  }

  public void setDate(long time) {
	try {
	  replace(0, getLength(), DateUtil.toString(locale, getSeparator(), time),
		  null);
	} catch (BadLocationException e) {
	  e.printStackTrace();
	}
  }

  public void setToCurrentDate() {
	setDate(System.currentTimeMillis());
  }

  private String buildCompletedText(String text, String newStr) {
	String regex1 = "";
	String regex2 = "";
	String complt1 = "";
	String complt2 = "";
	String sep = "\\" + separator.getSeparatorString();
	if (mask == DateMask.DDMMYYYY || mask == DateMask.MMDDYYYY) {
	  regex1 = "[0-9]{2}";
	  regex2 = "[0-9]{2}" + sep + "[0-9]{2}";
	  complt1 = "[0-9]{1,2}(" + sep + "([0-9]{1,4})?)?";
	  complt2 = "[0-9]{1,4}";
	} else if (mask == DateMask.YYYYMMDD || mask == DateMask.YYYYDDMM) {
	  regex1 = "[0-9]{4}";
	  complt1 = "[0-9]{1,2}(" + sep + "([0-9]{1,2})?)?";
	  regex2 = "[0-9]{4}" + sep + "[0-9]{2}";
	  complt2 = "[0-9]{1,2}";
	}
	if ((text.matches(regex1) && newStr.matches(complt1))
		|| text.matches(regex2) && newStr.matches(complt2)) {
	  return separator.getSeparatorString() + newStr;
	}

	return newStr;
  }

  public DateMask getMask() {
	return mask;
  }

  @Override
  public void remove(int offs, int len) throws BadLocationException {
	oldDate = getDate();
	super.remove(offs, len);
	if (getCurrentWriter() == null) {
	  fireListeners(oldDate, getDate());
	} else {
	  oldDateSetted = true;
	}

  }

  @Override
  public void insertString(int offs, String str, AttributeSet a)
	  throws BadLocationException {
	String newText = null;
	if (getLength() > 0) {
	  String text = getText(0, getLength());
	  if (isAutoCompleteEnabled() && offs == getLength()) {
		str = buildCompletedText(text, str);
	  }
	  newText =
		  text.substring(0, offs) + str + text.substring(offs, getLength());
	} else {
	  if (isAutoCompleteEnabled() && offs == getLength()) {
		str = buildCompletedText("", str);
	  }
	  newText = str;
	}
	if (newText.matches(regex)) {
	  if (oldDateSetted == false) {
		oldDate = getDate();
	  }
	  super.insertString(offs, str, a);
	  fireListeners(oldDate, getDate());
	}
  }

  protected void fireListeners(Date oldDate, Date newDate) {
	DateEvent event = new DateEvent(newDate, oldDate);

	DateListener[] listeners = listenerList.getListeners(DateListener.class);
	for (DateListener listener : listeners) {
	  listener.dateChanged(event);
	}
	oldDate = null;
	oldDateSetted = false;
  }

  public void addDateListener(DateListener listener) {
	listenerList.add(DateListener.class, listener);
  }

  public void removeDateListener(DateListener listener) {
	listenerList.remove(DateListener.class, listener);
  }
}
