package org.japura.util.date;

import java.util.HashMap;
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
public enum DateMask {
  DDMMYYYY(1),
  MMDDYYYY(2),
  YYYYMMDD(3),
  YYYYDDMM(4);

  private final int id;

  private DateMask(int id) {
	this.id = id;
  }

  public int getId() {
	return id;
  }

  private static HashMap<String, HashMap<String, DateMask>> registerMasks;

  public static DateMask getMask(int id) {
	for (DateMask dm : values()) {
	  if (dm.getId() == id) {
		return dm;
	  }
	}
	return null;
  }

  public static synchronized void register(Locale locale, DateMask mask) {
	if (locale == null || mask == null) {
	  return;
	}

	String language = locale.getLanguage();
	String country = locale.getCountry();

	if (registerMasks == null) {
	  registerMasks = new HashMap<String, HashMap<String, DateMask>>();
	}

	HashMap<String, DateMask> langMap = registerMasks.get(language);
	if (langMap == null) {
	  langMap = new HashMap<String, DateMask>();
	  registerMasks.put(language, langMap);
	}

	langMap.put(country, mask);
  }

  public static synchronized void unregister(Locale locale) {
	if (registerMasks != null) {
	  String language = locale.getLanguage();
	  HashMap<String, DateMask> langMap = registerMasks.get(language);
	  if (langMap != null) {
		String country = locale.getCountry();
		langMap.remove(country);
		if (langMap.size() == 0) {
		  registerMasks.remove(language);
		  if (registerMasks.size() == 0) {
			registerMasks = null;
		  }
		}
	  }
	}
  }

  public static synchronized DateMask getMask(Locale locale) {
	if (registerMasks != null) {
	  String language = locale.getLanguage();
	  HashMap<String, DateMask> langMap = registerMasks.get(language);
	  if (langMap != null) {
		String country = locale.getCountry();
		DateMask mask = langMap.get(country);
		if (mask != null) {
		  return mask;
		}
	  }
	}
	DateMask mask = LocaleToMask.getMask(locale);
	if (mask == null) {
	  throw new RuntimeException("There is no DateMask for locale: " + locale);
	}
	return mask;
  }

}
