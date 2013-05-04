package org.rr.jeborker;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.collection.ListenerList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.CommonUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.PreferenceItem;
import org.rr.jeborker.gui.MainMenuBarController;

public class JeboorkerPreferences {
	
	private static final ListenerList<JeboorkerPreferenceListener> preferenceChangeListener = new ListenerList<JeboorkerPreferenceListener>();
	
	private static final String BASE_PATH = "basePath";
	
	public static interface PreferenceKey {
		
		public String getKey();

		public String getDefaultValue();
		
	}	
	
	public static enum PREFERENCE_KEYS implements PreferenceKey {
		DELETE_EBOOK_AFTER_IMPORT {
			@Override
			public String getKey() {
				return "deleteEbookAfterImport";
			}
			
			public String getDefaultValue() {
				return "false";
			}
		}
	}

	public static void addBasePath(final String path) {
		String basePath = getGenericEntryAsString(BASE_PATH);
		if (basePath.length() == 0) {
			basePath = path;
		} else {
			basePath += (File.pathSeparator + path);
		}
		
		addGenericEntryAsString(BASE_PATH, basePath);
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
		
		addGenericEntryAsString(BASE_PATH, newBasePath.toString());
	}	
	
	/**
	 * Get all base path entries from the preference store.
	 * @return The stored base path entries. never returns <code>null</code>.
	 */
	public static BasePathList getBasePath() {
		String basePath = getGenericEntryAsString(BASE_PATH);
		if(basePath.length() == 0) {
			return new BasePathList();
		} else if (basePath.indexOf(File.pathSeparator) == -1) {
			return new BasePathList(basePath);
		} else {
			String[] split = basePath.split(File.pathSeparator);
			return new BasePathList(Arrays.asList(split));
		}
	}

	/**
	 * Tests if the given path is visible. 
	 */
	public static boolean isBasePathVisible(String path) {
		boolean isShow = MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
		return isShow;
	}
	
	/**
	 * Get the base path for the given {@link IResourceHandler}.
	 * @return the desired {@link IResourceHandler} or <code>null</code> if no base path exists
	 * for the given {@link IResourceHandler}.
	 */
	public static String getBasePathFor(IResourceHandler resource) {
		if(resource != null) {
			String resourceString = resource.toString();
			List<String> basePaths = getBasePath();
			for(String basePath : basePaths) {
				if(resourceString.startsWith(basePath)) {
					return basePath;
				}
			}
		}
		return null;
	}
	
	/**
	 * Just stores the given string value under the given key.
	 * @param key The key to access the given value.
	 * @param value The value which can be accessed with the given key.
	 */
	public static void addGenericEntryAsString(final String key, final String value) {
		addEntryToDB(key, value);
	}
	
	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public static String getGenericEntryAsString(final String key) {
		if(key != null) {
			String result;
			if((result = getEntryFromDB(key)) != null) {
				return result;
			}
		} return "";
	}
	
	/**
	 * Just stores the given string value under the given key.
	 * @param key The key to access the given value.
	 * @param value The value which can be accessed with the given key.
	 */
	public static void addGenericEntryAsNumber(final String key, final Number value) {
		addEntryToDB(key, String.valueOf(value.doubleValue()));
	}
	
	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public static Number getGenericEntryAsNumber(final String key) {
		String result = getEntryFromDB(key);
		if(result != null && result.length() > 0) {
			return Double.valueOf(result);
		} else {
			return null;
		}
	}	
	
	public static void addGenericEntryBoolean(final PREFERENCE_KEYS key, final Boolean b) {
		addGenericEntryBoolean(key.getKey(), b);
	}
	
	public static void addGenericEntryBoolean(final String key, final Boolean b) {
		addGenericEntryAsNumber(key, b.booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0));
	}
	
	public static Boolean getGenericEntryAsBoolean(final String key) {
		return getEntryAsBoolean(key, Boolean.FALSE);
	}
	
	/**
	 * Get the given {@link PREFERENCE_KEYS} as boolean value.  
	 * @return The desired value. Never returns <code>null</code>.
	 */
	public static Boolean getEntryAsBoolean(final PREFERENCE_KEYS key) {
		final String defaultValueString = key.getDefaultValue();
		final Boolean defaultValue = CommonUtils.toBoolean(defaultValueString);
		return getEntryAsBoolean(key.getKey(), defaultValue);
	}
	
	public static Boolean getEntryAsBoolean(final String key, Boolean defaultValue) {
		Number entryAsNumber = getGenericEntryAsNumber(key);
		if(entryAsNumber != null && entryAsNumber.intValue() == 1) {
			return Boolean.TRUE;
		} else if(entryAsNumber != null && entryAsNumber.intValue() == 0) {
			return Boolean.FALSE;
		}
		return defaultValue;
	}
	
	/**
	 * Fetch a previously stored string value with it's key from the DB.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */	
	private static String getEntryFromDB(final String key) {
		final DefaultDBManager db = DefaultDBManager.getInstance();
		final List<PreferenceItem> result = db.getObject(PreferenceItem.class, "name", key);
		
		if(result.isEmpty()) {
			return null;
		} else {
			return result.get(0).getValue();
		}
	}
	
	private static void addEntryToDB(final String key, final String value) {
		final DefaultDBManager db = DefaultDBManager.getInstance();
		final PreferenceItem newPreferenceItem = db.newInstance(PreferenceItem.class);
		
		newPreferenceItem.setName(key);
		newPreferenceItem.setValue(value);
		
		deleteEntryFromDB(key);
		db.storeObject(newPreferenceItem);
	}
	
	private static void deleteEntryFromDB(final String key) {
		final DefaultDBManager db = DefaultDBManager.getInstance();
		final List<PreferenceItem> result = db.getObject(PreferenceItem.class, "name", key);
		for(PreferenceItem item : result) {
			db.deleteObject(item);
		}
	}
	
	/**
	 * Get the point of a window where a window should be shown. 
	 * @param xKey The key for the x value
	 * @param yKey The key for the y value
	 * @return The point where the window should be shown or <code>null</code> if 
	 * there is no previously stored location point or if the window is not located at the screen.
	 */
	public static Point getGenericEntryAsScreenLocation(final String xKey, final String yKey) {
		String widthValue = getGenericEntryAsString(xKey);
		String heightValue = getGenericEntryAsString(yKey);
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
	
	/**
	 * Gets the default configuration directory. The path have a trailing / 
	 * 
	 * @return The config file directory.
	 */
	public static String getConfigDirectory() {
		String result = System.getProperties().getProperty("user.home");
		String suffix = System.getProperties().getProperty("application.suffix");
		
		result += File.separator + ".jeboorker" + (StringUtils.isNotEmpty(suffix) ? "." + suffix : "") + File.separator;
		
		return result;
	}
	
	/**
	 * Adds a preference change listener that is always invoked if a preference is changed.
	 */
	public static void addPreferenceChangeListener(final JeboorkerPreferenceListener listener) {
		preferenceChangeListener.addListener(listener);
	}
	
	/**
	 * Removes the given {@link JeboorkerPreferenceListener} from the listener list.
	 */
	public static void removePreferenceChangeListener(final JeboorkerPreferenceListener listener) {
		preferenceChangeListener.removeListener(listener);
	}

	/**
	 * Tells if the auto scrolling the the trees is enabled.
	 */
	public static boolean isTreeAutoScrollingEnabled() {
		Boolean result = getEntryAsBoolean("TreeAutoScrollingEnabled", Boolean.TRUE);
		return result;
	}	
	
	/**
	 * Sets the value for enable / disable the auto scrolling the the trees
	 */
	public static void setTreeAutoScrollingEnabled(final boolean value) {
		addGenericEntryBoolean("TreeAutoScrollingEnabled", Boolean.valueOf(value));
		for(JeboorkerPreferenceListener listener : preferenceChangeListener) {
			listener.treeAutoScrollingChanged(value);
		}
	}	
	
}
