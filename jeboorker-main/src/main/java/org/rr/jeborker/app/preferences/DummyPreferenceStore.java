package org.rr.jeborker.app.preferences;

public class DummyPreferenceStore extends APreferenceStore {

	@Override
	protected String getEntryFromImpl(String key) {
		return null;
	}

	@Override
	protected void addEntryToImpl(String key, String value) {
	}

	@Override
	protected void deleteEntryFromImpl(String key) {
	}

}
