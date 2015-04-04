package org.rr.jeborker.app.preferences;

import java.util.Properties;


class PropertiesPreferenceStore extends APreferenceStore {
	
	private static final Properties PROPERTIES = System.getProperties();
	
	@Override
	protected String getEntryFromImpl(final String key) {
		return PROPERTIES.getProperty(key);
	}
	
	@Override
	protected void addEntryToImpl(final String key, final String value) {
		PROPERTIES.put(key, value);
	}
	
	@Override
	protected void deleteEntryFromImpl(final String key) {
		PROPERTIES.remove(key);
	}
}
