package org.japura.gui.model;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Copyright (C) 2010 Carlos Eduardo Leite de Andrade
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
public abstract class NumberDocument<T extends Number> extends PlainDocument{

  private static final long serialVersionUID = 7570259805144825486L;
  private String separator;
  private String validateRegex;
  private String insertRegex;
  protected T maxValue;
  protected T minValue;

  /**
   * Constructor
   * 
   * @param locale
   *          {@link Locale} to gets the character used for decimal sign.
   * @param acceptFraction
   */
  public NumberDocument(Locale locale, boolean acceptFraction) {
	if (locale == null) {
	  locale = Locale.getDefault();
	}
	if (acceptFraction) {
	  DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
	  separator = Character.toString(dfs.getDecimalSeparator());
	  validateRegex = "([-]{1,1})?[0-9]{1,}([" + separator + "][0-9]{1,})?";
	  insertRegex = "([-]{1,1})?([0-9]{1,})?([" + separator + "])?([0-9]{1,})?";
	} else {
	  validateRegex = "([-]{1,1})?[0-9]{1,}";
	  insertRegex = "([-]{1,1})?([0-9]{1,})?";
	}
  }

  protected String getValidateRegex() {
	return validateRegex;
  }

  protected NumberFormat getNumberFormat() {
	NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
	nf.setGroupingUsed(false);
	return nf;
  }

  protected Number parse(String text) {
	NumberFormat n = getNumberFormat();
	try {
	  return n.parse(text);
	} catch (ParseException e) {
	  e.printStackTrace();
	}
	return null;
  }

  protected String format(Object value) {
	NumberFormat n = getNumberFormat();
	n.setMaximumFractionDigits(Integer.MAX_VALUE);
	return n.format(value);
  }

  protected String getText() throws BadLocationException {
	return getText(0, getLength());
  }

  protected abstract boolean isAcceptableValue(Number number);

  @Override
  public void insertString(int offs, String str, AttributeSet a)
	  throws BadLocationException {
	String text = getText();
	String newText =
		text.substring(0, offs) + str + text.substring(offs, text.length());
	if (newText.matches(insertRegex)) {
	  Number number = parse(newText);
	  if (number != null && isAcceptableValue(number)) {
		super.insertString(offs, str, a);
	  }
	}
  }

}
