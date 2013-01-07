package org.rr.jeborker;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.utils.CommonUtils;

public class JeboorkerPreferences {
	
	private static final Preferences APP_NODE;
	static {
		String suffix = System.getProperties().getProperty("application.suffix");
		APP_NODE = Preferences.userRoot().node("jeboorker" + (suffix != null ? "." + suffix : ""));
	}

	public static void addBasePath(final String path) {
		String basePath = APP_NODE.get("basePath", "");
		if(basePath.length()==0) {
			basePath = path;
		} else {
			basePath += (File.pathSeparator + path);
		}
		APP_NODE.put("basePath", basePath);
	}
	
	/**
	 * Removes one entry from the base path list.
	 * @param path The path entry to be removed.
	 */
	public static void removeBasePath(final String path) {
		final List<String> oldBasePath = getBasePath();
		final StringBuilder newBasePath = new StringBuilder();
		for (Iterator<String> iterator = oldBasePath.iterator(); iterator.hasNext();) {
			final String oldPath = iterator.next();
			if(!StringUtils.replace(oldPath, File.separator, "").equals(StringUtils.replace(path, File.separator, ""))) {
				newBasePath.append(oldPath).append(File.pathSeparator);
			}
		}
		
		//remove the last separator char
		if(newBasePath.length()>0) {
			newBasePath.deleteCharAt(newBasePath.length()-1);
		}
		
		APP_NODE.put("basePath", newBasePath.toString());
	}	
	
	/**
	 * Get all base path entries from the preference store.
	 * @return The stored base path entries. never returns <code>null</code>.
	 */
	public static List<String> getBasePath() {
		String basePath = APP_NODE.get("basePath", "");
		if(basePath.length() == 0) {
			return Collections.emptyList();
		} else if(basePath.indexOf(File.pathSeparator)==-1) {
			return Collections.singletonList(basePath);
		} else {
			String[] split = basePath.split(File.pathSeparator);
			return Arrays.asList(split);
		}
	}
	
	/**
	 * Just stores the given string value under the given key.
	 * @param key The key to access the given value.
	 * @param value The value which can be accessed with the given key.
	 */
	public static void addEntryString(final String key, final String value) {
		APP_NODE.put(key, value);
	}
	
	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public static String getEntryString(final String key) {
		return APP_NODE.get(key, "");
	}
	
	/**
	 * Just stores the given string value under the given key.
	 * @param key The key to access the given value.
	 * @param value The value which can be accessed with the given key.
	 */
	public static void addEntryNumber(final String key, final Number value) {
		APP_NODE.put(key, String.valueOf(value.doubleValue()));
	}
	
	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public static Number getEntryAsNumber(final String key) {
		String value = APP_NODE.get(key, "");
		if(value!=null && value.length()>0) {
			return Double.valueOf(APP_NODE.get(key, ""));
		} else {
			return null;
		}
	}	
	
	public static void addEntryBoolean(final String key, final Boolean b) {
		addEntryNumber(key, b.booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0));
	}
	
	public static Boolean getEntryAsBoolean(final String key) {
		Number entryAsNumber = getEntryAsNumber(key);
		if(entryAsNumber != null && entryAsNumber.intValue() == 1) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Get the point of a window where a window should be shown. 
	 * @param xKey The key for the x value
	 * @param yKey The key for the y value
	 * @return The point where the window should be shown or <code>null</code> if 
	 * there is no previously stored location point or if the window is not located at the screen.
	 */
	public static Point getEntryAsScreenLocation(final String xKey, final String yKey) {
		String widthValue = APP_NODE.get(xKey, "");
		String heightValue = APP_NODE.get(yKey, "");
		Point location = new Point();
		
		if(widthValue!=null && widthValue.length()>0) {
			location.x = CommonUtils.toNumber(widthValue).intValue();
		} else {
			return null;
		}
		
		if(heightValue!=null && heightValue.length()>0) {
			location.y = CommonUtils.toNumber(heightValue).intValue();
		} else {
			return null;
		}
		
		if(!intersectsScreen(location)) {
			return null;
		}
		
		return location;
	}	
	
	private static boolean intersectsScreen(Point location) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if(location.x +150 > screen.width || location.y +150 > screen.height) {
			//don't restore and use the default values.
			return false;
		} 	
		return true;
	}
	
}