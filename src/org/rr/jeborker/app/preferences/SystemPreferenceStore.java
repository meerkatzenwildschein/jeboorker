package org.rr.jeborker.app.preferences;

import java.util.prefs.Preferences;


class SystemPreferenceStore extends APreferenceStore {
	
	private static final Preferences APP_NODE;
	
	static {
		String suffix = System.getProperties().getProperty("application.suffix");
		APP_NODE = Preferences.userRoot().node("jeboorker" + (suffix != null ? "." + suffix : ""));
	}

	/**
	 * Fetch a previously stored string value with it's key from the DB.
	 * @param key The key to access the value.
	 * @return The desired value or <code>null</code> if the value wasn't stored.
	 */	
	@Override
	protected String getEntryFromImpl(final String key) {
		String result = APP_NODE.get(key, null);
		return result;
	}
	
	@Override
	protected void addEntryToImpl(final String key, final String value) {
		APP_NODE.put(key, value);
	}
	
	@Override
	protected void deleteEntryFromImpl(final String key) {
		APP_NODE.remove(key);
	}
}
