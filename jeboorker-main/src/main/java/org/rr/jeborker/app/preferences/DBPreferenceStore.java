package org.rr.jeborker.app.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.rr.commons.collection.LRUCacheMap;
import org.rr.jeborker.app.BasePathList;
import org.rr.jeborker.app.FileWatchService;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.PreferenceItem;

class DBPreferenceStore extends APreferenceStore {
	
	private final String BASE_PATH = "basePath";

	private static final Map<String, String> CACHE = new LRUCacheMap<String, String>(100);
	
	/**
	 * Fetch a previously stored string value with it's key from the DB.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */	
	@Override
	protected String getEntryFromImpl(final String key) {
		String value = CACHE.get(key);
		if(value != null) {
			return value;
		}
		
		final DefaultDBManager db = DefaultDBManager.getInstance();
		final List<PreferenceItem> result = db.getObject(PreferenceItem.class, "name", key);
		
		if(result.isEmpty()) {
			value = null;
		} else {
			value =result.get(0).getValue();
		}
		CACHE.put(key, value);
		return value;
	}
	
	@Override
	protected void addEntryToImpl(final String key, final String value) {
		final DefaultDBManager db = DefaultDBManager.getInstance();
		final PreferenceItem newPreferenceItem = db.newInstance(PreferenceItem.class);
		
		newPreferenceItem.setName(key);
		newPreferenceItem.setValue(value);
		
		db.storeObject(newPreferenceItem);
		
		CACHE.put(key, value);
	}
	
	@Override
	protected void deleteEntryFromImpl(final String key) {
		final DefaultDBManager db = DefaultDBManager.getInstance();
		final List<PreferenceItem> result = db.getObject(PreferenceItem.class, "name", key);
		for(PreferenceItem item : result) {
			db.deleteObject(item);
		}
		CACHE.remove(key);
	}
	
	public void addBasePath(String path) {
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
	public void removeBasePath(String path) {
		List<String> oldBasePath = getBasePath();
		StringBuilder newBasePath = new StringBuilder();

		for (Iterator<String> iterator = oldBasePath.iterator(); iterator.hasNext();) {
			final String oldPath = iterator.next();
			if(!oldPath.replaceAll("\\/", "").equals(path.replaceAll("\\/", "")) &&
					!oldPath.equals(BasePathList.getImportBasePath())) {
				newBasePath.append(oldPath).append(File.pathSeparator);
			}
		}

		//remove the last separator char
		if(newBasePath.length() > 0) {
			newBasePath.deleteCharAt(newBasePath.length() - 1);
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
}
