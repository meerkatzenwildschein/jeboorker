package org.rr.jeborker.app.preferences;

import java.util.List;
import java.util.Map;

import org.rr.commons.collection.LRUCacheMap;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.PreferenceItem;

class DBPreferenceStore extends APreferenceStore {

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
		
		deleteEntryFromImpl(key);
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
}
