package org.rr.jeborker.app.preferences;

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
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.FileWatchService;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory.PREFERENCE_KEYS;
import org.rr.jeborker.gui.MainMenuBarController;

public abstract class APreferenceStore {

	private final ListenerList<JeboorkerPreferenceListener> preferenceChangeListener = new ListenerList<JeboorkerPreferenceListener>();
	
	private final String BASE_PATH = "basePath";

	/**
	 * Fetch a previously stored string value with it's key from the DB.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */	
	protected abstract String getEntryFromImpl(final String key);

	protected abstract void addEntryToImpl(final String key, final String value);

	protected abstract void deleteEntryFromImpl(final String key);

	public void addBasePath(final String path) {
		String basePath = getGenericEntryAsString(BASE_PATH);
		if (basePath.length() == 0) {
			basePath = path;
		} else {
			basePath += (File.pathSeparator + path);
		}
		
		addGenericEntryAsString(BASE_PATH, basePath);
		FileWatchService.addWatchPath(path);
	}
	
	/**
	 * Removes one entry from the base path list.
	 * @param path The path entry to be removed.
	 */
	public void removeBasePath(final String path) {
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
	public BasePathList getBasePath() {
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
	public boolean isBasePathVisible(String path) {
		boolean isShow = MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
		return isShow;
	}
	
	/**
	 * Get the base path for the given {@link IResourceHandler}.
	 * @return the desired {@link IResourceHandler} or <code>null</code> if no base path exists
	 * for the given {@link IResourceHandler}.
	 */
	public String getBasePathFor(IResourceHandler resource) {
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
	public void addGenericEntryAsString(final String key, final String value) {
		addEntryToImpl(key, value);
	}
	
	public void addEntryAsString(final PREFERENCE_KEYS key, final String value) {
		addGenericEntryAsString(key.getKey(), value);
	}		
	
	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public String getGenericEntryAsString(final String key, String defaultValue) {
		if(key != null) {
			String result;
			if((result = getEntryFromImpl(key)) != null) {
				return result;
			}
		} return defaultValue;
	}
	
	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public String getGenericEntryAsString(final String key) {
		return getGenericEntryAsString(key, "");
	}
	
	/**
	 * Just stores the given string value under the given key.
	 * @param key The key to access the given value.
	 * @param value The value which can be accessed with the given key.
	 */
	public void addGenericEntryAsNumber(final String key, final Number value) {
		addEntryToImpl(key, String.valueOf(value.doubleValue()));
	}
	
	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public Number getGenericEntryAsNumber(final String key) {
		String result = getEntryFromImpl(key);
		if(result != null && result.length() > 0) {
			return Double.valueOf(result);
		} else {
			return null;
		}
	}	
	
	public void addEntryAsBoolean(final PREFERENCE_KEYS key, final Boolean b) {
		addGenericEntryBoolean(key.getKey(), b);
	}
	
	private void addGenericEntryBoolean(final String key, final Boolean b) {
		addGenericEntryAsNumber(key, b.booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0));
	}
	
	/**
	 * Get the given {@link PREFERENCE_KEYS} as string value.  
	 * @return The desired value. Never returns <code>null</code>.
	 */
	public String getEntryAsString(final PREFERENCE_KEYS key) {
		final String preferenceEntry = getGenericEntryAsString(key.getKey(), null);
		if(preferenceEntry != null) {
			return preferenceEntry;
		} else {
			return key.getDefaultValue();
		}
	}	
	
	/**
	 * Get the given {@link PREFERENCE_KEYS} as boolean value.  
	 * @return The desired value. Never returns <code>null</code>.
	 */
	public Boolean getEntryAsBoolean(final PREFERENCE_KEYS key) {
		final String defaultValueString = key.getDefaultValue();
		final Boolean defaultValue = CommonUtils.toBoolean(defaultValueString);
		return getEntryAsBoolean(key.getKey(), defaultValue);
	}
	
	private Boolean getEntryAsBoolean(final String key, Boolean defaultValue) {
		Number entryAsNumber = getGenericEntryAsNumber(key);
		if(entryAsNumber != null && entryAsNumber.intValue() == 1) {
			return Boolean.TRUE;
		} else if(entryAsNumber != null && entryAsNumber.intValue() == 0) {
			return Boolean.FALSE;
		}
		return defaultValue;
	}
	
	/**
	 * Get the point of a window where a window should be shown. 
	 * @param xKey The key for the x value
	 * @param yKey The key for the y value
	 * @return The point where the window should be shown or <code>null</code> if 
	 * there is no previously stored location point or if the window is not located at the screen.
	 */
	public Point getGenericEntryAsScreenLocation(final String xKey, final String yKey) {
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
	
	private boolean intersectsScreen(Point location) {
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
	public String getConfigDirectory() {
		String result = System.getProperties().getProperty("user.home");
		String suffix = System.getProperties().getProperty("application.suffix");
		
		result += File.separator + ".jeboorker" + (StringUtils.isNotEmpty(suffix) ? "." + suffix : "") + File.separator;
		
		return result;
	}
	
	/**
	 * Adds a preference change listener that is always invoked if a preference is changed.
	 */
	public void addPreferenceChangeListener(final JeboorkerPreferenceListener listener) {
		preferenceChangeListener.addListener(listener);
	}
	
	/**
	 * Removes the given {@link JeboorkerPreferenceListener} from the listener list.
	 */
	public void removePreferenceChangeListener(final JeboorkerPreferenceListener listener) {
		preferenceChangeListener.removeListener(listener);
	}

	/**
	 * Tells if the auto scrolling the the trees is enabled.
	 */
	public boolean isTreeAutoScrollingEnabled() {
		return getEntryAsBoolean(PREFERENCE_KEYS.TREE_AUTO_SCROLLING_ENABLED);
	}	
	
	/**
	 * Sets the value for enable / disable the auto scrolling the the trees
	 */
	public void setTreeAutoScrollingEnabled(final boolean value) {
		addEntryAsBoolean(PREFERENCE_KEYS.TREE_AUTO_SCROLLING_ENABLED, Boolean.valueOf(value));
		
		//listener for enable the auto scroll function in the tree component.
		for(JeboorkerPreferenceListener listener : preferenceChangeListener) {
			listener.treeAutoScrollingChanged(value);
		}
	}	
	
}
