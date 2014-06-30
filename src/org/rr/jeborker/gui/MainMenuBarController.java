package org.rr.jeborker.gui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuBar;

import org.rr.commons.utils.ListUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.action.ActionFactory;

public class MainMenuBarController {

	private static MainMenuBarController controller;

	private MainMenuBarView view;

	private Map<String, Boolean> showHideBasePathToggleStatus = new HashMap<String, Boolean>();

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

	public void dispose() {
		this.storeProperties();
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
		final Boolean status = showHideBasePathToggleStatus.get(basePath);
		if(status == null) {
			return true; //show per default
		}
		return status.booleanValue();
	}

	public void setShowHideBasePathStatusShow(final String basePath, final boolean show) {
		showHideBasePathToggleStatus.put(basePath, Boolean.valueOf(show));
	}

	/**
	 * get all base path entries which are marked as hidden in the file menu.
	 * @return A list of all hidden base path entries.
	 */
	public List<String> getHiddenBasePathEntries() {
		if(showHideBasePathToggleStatus.isEmpty()) {
			return Collections.emptyList();
		}

		final ArrayList<String> result = new ArrayList<String>();
	    for (Map.Entry<String, Boolean> entry : showHideBasePathToggleStatus.entrySet()) {
	        String basePath = entry.getKey();
	        Boolean isSHow = entry.getValue();
	        if(isSHow!=null && isSHow.booleanValue() == false) {
	        	result.add(basePath);
	        }
	    }
	    return result;
	}

	private void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final List<String> hiddenBasePathEntries = MainMenuBarController.getController().getHiddenBasePathEntries();
		if(hiddenBasePathEntries.isEmpty()) {
			preferenceStore.addGenericEntryAsString("mainMenuBasePathHide", "");
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
					ArrayList<String> s = new ArrayList<String>(split);
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
