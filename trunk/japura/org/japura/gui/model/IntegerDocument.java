package org.japura.gui.model;

import java.util.Locale;

import javax.swing.text.BadLocationException;

/**
 * Copyright (C) 2010-2011 Carlos Eduardo Leite de Andrade
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
public class IntegerDocument extends NumberDocument<Integer>{

  private static final long serialVersionUID = -6161711707235392895L;

  public IntegerDocument() {
	this(null);
  }

  public IntegerDocument(Locale locale) {
	super(locale, false);
	maxValue = Integer.MAX_VALUE;
	minValue = Integer.MIN_VALUE;
  }

  public Integer getValue() {
	try {
	  String text = getText();
	  if (text.matches(getValidateRegex())) {
		Number number = parse(text);
		if (number != null) {
		  Integer value = number.intValue();
		  if (value >= minValue && value <= maxValue) {
			return value;
		  }
		}
	  }
	} catch (BadLocationException e) {
	  e.printStackTrace();
	}
	return null;
  }

  public void setValue(int value) {
	String text = format(value);
	try {
	  remove(0, getLength());
	  insertString(0, text, null);
	} catch (BadLocationException e) {
	  e.printStackTrace();
	}
  }

  public int getMaxValue() {
	return maxValue;
  }

  public void setMaxValue(int maxValue) {
	this.maxValue = maxValue;
  }

  public int getMinValue() {
	return minValue;
  }

  public void setMinValue(int minValue) {
	this.minValue = minValue;
  }

  @Override
  protected boolean isAcceptableValue(Number number) {
	int value = number.intValue();
	if (value >= getMinValue() && value <= getMaxValue()) {
	  return true;
	}
	return false;
  }

}
