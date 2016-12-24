package org.rr.jeborker.app.preferences;

import static org.rr.commons.utils.BooleanUtils.not;
import static org.rr.commons.utils.StringUtil.EMPTY;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.rr.commons.collection.ListenerList;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.utils.BooleanUtils;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtil;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory.PREFERENCE_KEYS;
import org.rr.jeborker.gui.MainMenuBarController;

public abstract class APreferenceStore {

	private final ListenerList<JeboorkerPreferenceListener> preferenceChangeListener = new ListenerList<>();

	/**
	 * Fetch a previously stored string value with it's key from the DB.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	protected abstract String getEntryFromImpl(String key);

	protected abstract void addEntryToImpl(String key, String value);

	protected abstract void deleteEntryFromImpl(String key);

	public void addBasePath(String path) {
		throw new IllegalArgumentException("Base path action are not supported.");
	}
	
	/**
	 * Removes one entry from the base path list.
	 * @param path The path entry to be removed.
	 */
	public void removeBasePath(String path) {
		throw new IllegalArgumentException("Base path action are not supported.");
	}

	/**
	 * Get all base path entries from the preference store.
	 * @return The stored base path entries. never returns <code>null</code>.
	 */
	public BasePathList getBasePath() {
		throw new IllegalArgumentException("Base path action are not supported.");
	}

	/**
	 * Tests if the given path is visible.
	 */
	public boolean isBasePathVisible(String path) {
		return MainMenuBarController.getController().isShowHideBasePathStatusShow(path);
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
	public void addGenericEntryAsString(String key, String value) {
		addEntryToImpl(key, value);
	}

	public void addEntryAsString(PREFERENCE_KEYS key, String value) {
		addGenericEntryAsString(key.getKey(), value);
	}

	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public String getGenericEntryAsString(String key, String defaultValue) {
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
	public String getGenericEntryAsString(String key) {
		return getGenericEntryAsString(key, EMPTY);
	}

	/**
	 * Just stores the given string value under the given key.
	 * @param key The key to access the given value.
	 * @param value The value which can be accessed with the given key.
	 */
	public void addGenericEntryAsNumber(String key, Number value) {
		addEntryToImpl(key, String.valueOf(value.doubleValue()));
	}

	/**
	 * Fetch a previously stored string value with it's key.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */
	public Number getGenericEntryAsNumber(String key) {
		String result = getEntryFromImpl(key);
		if(result != null && result.length() > 0) {
			return Double.valueOf(result);
		} else {
			return null;
		}
	}
	
	public Number getGenericEntryAsNumber(String key, Number defaultValue) {
		Number result = getGenericEntryAsNumber(key);
		return result != null ? result : defaultValue;
	}

	public void addEntryAsBoolean(PREFERENCE_KEYS key, Boolean b) {
		addGenericEntryBoolean(key.getKey(), b);
	}

	private void addGenericEntryBoolean(String key, Boolean b) {
		addGenericEntryAsNumber(key, b.booleanValue() ? Integer.valueOf(1) : Integer.valueOf(0));
	}

	/**
	 * Get the given {@link PREFERENCE_KEYS} as string value.
	 * @return The desired value. Never returns <code>null</code>.
	 */
	public String getEntryAsString(PREFERENCE_KEYS key) {
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
	public Boolean getEntryAsBoolean(PREFERENCE_KEYS key) {
		String defaultValueString = key.getDefaultValue();
		Boolean defaultValue = BooleanUtils.toBoolean(defaultValueString);
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
	public Point getGenericEntryAsScreenLocation(String xKey, String yKey) {
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
	 * Gets the default configuration directory. The path have a trailing '/'. The config
	 * file directory is something like <code>user.home/.jeboorker/</code>.
	 *
	 * @return The config file directory.
	 */
	public static String getConfigDirectory() {
		String configDir = System.getProperties().getProperty("user.home");
		String suffix = System.getProperties().getProperty("application.suffix");
		configDir += File.separator + ".jeboorker" + (StringUtil.isNotEmpty(suffix) ? "." + suffix : EMPTY) + File.separator;

		makeConfigDirIfNotExists(configDir);

		return configDir;
	}

	private static void makeConfigDirIfNotExists(String configDir) {
		File configDirFile = new File(configDir);
		if(not(configDirFile.exists())) {
			configDirFile.mkdirs();
		}
	}

	/**
	 * Stores location and size of the given windows under the given key.
	 * @param key The unique key where the window location adn size are stored.
	 * @param window The window which location and size should be stored.
	 */
	public void storeWindowLocationAndSize(String key, Window window) {
		Point location = window.getLocation();
		Dimension size = window.getSize();
		List<Integer> values = Arrays.asList(new Integer[] { Integer.valueOf(location.x), Integer.valueOf(location.y), Integer.valueOf(size.width),
				Integer.valueOf(size.height) });
		addGenericEntryAsString(key, ListUtils.join(values, ";"));
	}

	/**
	 * Restores the size and location previously stored with the {@link #storeWindowLocationAndSize(String, Window)} method.
	 * @param key The key where the location and size was previously stored
	 * @param window The window where the location and size should be applied to.
	 */
	public void restoreWindowLocationAndSize(String key, Window window) {
		String sizeAndLocation = getGenericEntryAsString(key);
		if(StringUtil.isNotEmpty(sizeAndLocation)) {
			List<String> s = ListUtils.split(sizeAndLocation, ";");
			window.setLocation(new Point(NumberUtils.toInt(s.get(0)), NumberUtils.toInt(s.get(1))));
			window.setSize(NumberUtils.toInt(s.get(2)), NumberUtils.toInt(s.get(3)));
		}
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
