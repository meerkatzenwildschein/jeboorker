package org.japura.util.date;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

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
final class MasksGenerator{

  public static class Item{
	public String nameInPortuguese;
	public String nameInEnglish;
	public String country;
	public String formatedDate;

	public String toString() {
	  return "Country[" + country + "] Data[" + formatedDate + "] ["
		  + nameInEnglish + "/" + nameInPortuguese + "]";
	}
  }

  public static void main(String dasda[]) {
	HashMap<String, List<Item>> map = new HashMap<String, List<Item>>();
	Locale[] ss = Locale.getAvailableLocales();
	for (Locale l : ss) {

	  java.text.DateFormat df =
		  java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, l);

	  GregorianCalendar gc = new GregorianCalendar();
	  gc.clear();
	  gc.set(2001, 1, 15);

	  Date date = gc.getTime();

	  Item item = new Item();
	  item.nameInPortuguese = l.getDisplayName(new Locale("pt", "BR"));
	  item.nameInEnglish = l.getDisplayName(Locale.ENGLISH);
	  item.country = l.getCountry();
	  item.formatedDate = df.format(date);

	  String lang = l.getLanguage();
	  List<Item> list = map.get(lang);
	  if (list == null) {
		list = new ArrayList<Item>();
		map.put(lang, list);
	  }
	  list.add(item);
	}

	if (false) {
	  for (Entry<String, List<Item>> entry : map.entrySet()) {
		System.out.println("------------------------------------");
		System.out.println("Language[" + entry.getKey() + "]");
		for (Item item : entry.getValue()) {
		  System.out.println("--> " + item);
		}
	  }
	}

	StringBuilder sb = new StringBuilder();

	sb.append("public static DateMask getMask(Locale locale) {");
	sb.append("\n");
	sb.append("if (locale == null) {");
	sb.append("\n");
	sb.append("locale = Locale.getDefault();");
	sb.append("\n");
	sb.append("}");
	sb.append("\n");
	sb.append("String lang = locale.getLanguage();");
	sb.append("\n");
	sb.append("String country = locale.getCountry();");
	sb.append("\n");

	boolean first = true;
	for (Entry<String, List<Item>> entry : map.entrySet()) {
	  if (first == false) {
		sb.append(" else \n");
	  }
	  first = false;

	  sb.append("if (lang.equals(\"" + entry.getKey() + "\")) {");
	  sb.append("\n");
	  for (Item item : entry.getValue()) {
		sb.append("if(country.equals(\"" + item.country + "\")) {");
		sb.append("\n");
		sb.append("//");
		sb.append(item.nameInEnglish + " - ");
		sb.append(item.nameInPortuguese + " - ");
		sb.append(item.formatedDate);
		sb.append("\n");
		sb.append("return DateMask.NULL;");
		sb.append("\n");
		sb.append("}");
		sb.append("\n");
	  }
	  sb.append("}");
	  sb.append("\n");
	}

	sb.append("return null;");
	sb.append("\n");
	sb.append("}");

	System.out.println(sb.toString());
  }

}
