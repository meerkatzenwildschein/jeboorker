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
public class DoubleDocument extends NumberDocument<Double>{

  private static final long serialVersionUID = -1334753847712151427L;

  public DoubleDocument() {
	this(null);
  }

  public DoubleDocument(Locale locale) {
	super(locale, true);
	maxValue = Double.MAX_VALUE;
	minValue = Double.MIN_VALUE;
  }

  public Double getValue() {
	try {
	  String text = getText();
	  if (text.matches(getValidateRegex())) {
		Number number = parse(text);
		if (number != null) {
		  Double value = number.doubleValue();
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

  public void setValue(double value) {
	String text = format(value);
	try {
	  remove(0, getLength());
	  insertString(0, text, null);
	} catch (BadLocationException e) {
	  e.printStackTrace();
	}
  }

  public double getMaxValue() {
	return maxValue;
  }

  public void setMaxValue(double maxValue) {
	this.maxValue = maxValue;
  }

  public double getMinValue() {
	return minValue;
  }

  public void setMinValue(double minValue) {
	this.minValue = minValue;
  }

  @Override
  protected boolean isAcceptableValue(Number number) {
	double value = number.doubleValue();
	if (value >= getMinValue() && value <= getMaxValue()) {
	  return true;
	}
	return false;
  }

}
