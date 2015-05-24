package org.rr.jeborker.gui;

import static org.rr.commons.utils.StringUtils.EMPTY;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuBar;

import org.apache.commons.lang.BooleanUtils;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.action.ActionFactory;

public class MainMenuBarController {

	/** contains the visibility state for each base path as key. The path statement will be automatically normalized */
	private final Map<String, Boolean> showHideBasePathVisibility = new HashMap<String, Boolean>() {

		private static final long serialVersionUID = 1878898968010941794L;

		@Override
		public Boolean put(String key, Boolean value) {
			return super.put(removeTrailingNameSeparator(key), value);
		}

		@Override
		public Boolean get(Object key) {
			return super.get(removeTrailingNameSeparator((String) key));
		}

		private String removeTrailingNameSeparator(String key) {
			return StringUtils.removeEnd(StringUtils.removeEnd(key, "/"), "\\");
		}

	};

	private static MainMenuBarController controller;

	private MainMenuBarView view;

	private MainMenuBarController() {
		super();
	}

	/**
	 * Gets the controller instance. because we have only one main window
	 * We have a singleton here.
	 * @return The desired EBorkerMainController.
	 */
	public static MainMenuBarController getController() {
		if(controller==null) {
			controller = new MainMenuBarController();
		}
		return controller;
	}

	/**
	 * Get the menu view which is a {@link JMenuBar} instance.
	 * @return The menu view.
	 */
	MainMenuBarView getView() {
		if(view == null) {
			view = new MainMenuBarView();
		}
		return view;
	}

	/**
	 * Removes the menu item having the given path in it's name.
	 * @param path The path entry to be removed.
	 */
	public void removeBasePathMenuEntry(String path) {
		view.removeBasePathMenuEntry(path);
	}

	/**
	 * Tells if the ebook items with the given basePath are shown or not.
	 * @param basePath The pase path for the items.
	 * @return <code>true</code> if the ebook items are shown and <code>false</code> if not.
	 */
	public boolean isShowHideBasePathStatusShow(final String basePath) {
		final Boolean status = showHideBasePathVisibility.get(basePath);
		if(status == null) {
			return true; //show per default
		}
		return status.booleanValue();
	}

	public void setShowHideBasePathStatusShow(final String basePath, final boolean show) {
		showHideBasePathVisibility.put(basePath, Boolean.valueOf(show));
	}

	public boolean containsHiddenBasePathEntry(String entry) {
		Boolean isShow = showHideBasePathVisibility.get(entry);
		return BooleanUtils.isFalse(isShow);
	}

	/**
	 * get all base path entries which are marked as hidden in the file menu.
	 * @return A list of all hidden base path entries.
	 */
	public List<String> getHiddenBasePathEntries() {
		if(showHideBasePathVisibility.isEmpty()) {
			return Collections.emptyList();
		}

		final ArrayList<String> result = new ArrayList<>();
	    for (Map.Entry<String, Boolean> entry : showHideBasePathVisibility.entrySet()) {
	        String basePath = entry.getKey();
	        Boolean isShow = entry.getValue();
	        if(isShow != null && isShow.booleanValue() == false) {
	        	result.add(basePath);
	        }
	    }
	    return result;
	}

	void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<String> hiddenBasePathEntries = getHiddenBasePathEntries();
		if(hiddenBasePathEntries.isEmpty()) {
			preferenceStore.addGenericEntryAsString("mainMenuBasePathHide", EMPTY);
		} else {
			preferenceStore.addGenericEntryAsString("mainMenuBasePathHide", ListUtils.join(hiddenBasePathEntries, String.valueOf(File.pathSeparatorChar)));
		}
	}

	void restoreProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<String> path = preferenceStore.getBasePath();
		final String basePathPropString = preferenceStore.getGenericEntryAsString("mainMenuBasePathHide");

		if(basePathPropString != null && !basePathPropString.isEmpty()) {
			List<String> split = ListUtils.split(basePathPropString, String.valueOf(File.pathSeparatorChar));
			for(String basePath : split) {
				if(!path.contains(basePath)) {
					//there is no base path for the hidden path
					ArrayList<String> s = new ArrayList<>(split);
					boolean remove = s.remove(basePath);
					if(remove) {
						preferenceStore.addGenericEntryAsString("mainMenuBasePathHide", ListUtils.join(s, String.valueOf(File.pathSeparatorChar)));
					}
				} else {
					ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.SHOW_HIDE_BASE_PATH_ACTION, basePath).invokeAction(new ActionEvent(this, 0, "initialize"));
				}
			}
		}
	}
}
