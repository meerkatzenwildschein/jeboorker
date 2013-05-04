package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.commons.swing.dialogs.JPreferenceDialog;
import org.rr.jeborker.JeboorkerPreferences;

public class PreferenceController {
	
	private PreferenceView preferenceView;

	private PreferenceController() {}
	
	static PreferenceController getInstance() {
		PreferenceController controller = new PreferenceController();
		return controller;
	}		

	public void showPreferenceDialog() {
		PreferenceView view = getView();
		view.setVisible(true);
		
		int actionResult = view.getActionResult();
		if(actionResult == JPreferenceDialog.ACTION_RESULT_OK) {
			this.setPreferenceViewValues(view);
		}
	}
	
	/**
	 * Push the preference dialog values into the application.
	 */
	private void setPreferenceViewValues(PreferenceView view) {
		boolean isTreeAutoscrollEnabled = view.getBooleanValue(PreferenceView.AUTO_SCOLL_ITEM_PREFERENCE_NAME);
		JeboorkerPreferences.setTreeAutoScrollingEnabled(isTreeAutoscrollEnabled);
		
		boolean isEbookDeleteAfterImport = view.getBooleanValue(PreferenceView.DELETE_AFTER_IMPORT_PREFERENCE_NAME);
		JeboorkerPreferences.addGenericEntryBoolean(JeboorkerPreferences.PREFERENCE_KEYS.DELETE_EBOOK_AFTER_IMPORT, isEbookDeleteAfterImport);
	}
	
	private PreferenceView getView() {
		if(preferenceView == null) {
			JFrame mainWindow = MainController.getController().getMainWindow();
			this.preferenceView = new PreferenceView(mainWindow, this);
			this.initialize();
		}
		return preferenceView;
	}

	private void initialize() {
		restorePropeties();		
	}
	
	public void close() {
		storeProperties();
		
//		preferenceView.setVisible(false);
		preferenceView.dispose();
	}		

	private void storeProperties() {
		JeboorkerPreferences.addGenericEntryAsNumber("prefDialogSizeWidth", getView().getSize().width);
		JeboorkerPreferences.addGenericEntryAsNumber("prefDialogSizeHeight", getView().getSize().height);
		JeboorkerPreferences.addGenericEntryAsNumber("prefDialogLocationX", getView().getLocation().x);
		JeboorkerPreferences.addGenericEntryAsNumber("prefDialogLocationY", getView().getLocation().y);
	}

	private void restorePropeties() {
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = JeboorkerPreferences.getGenericEntryAsNumber("prefDialogSizeWidth");
		Number metadataDialogSizeHeight = JeboorkerPreferences.getGenericEntryAsNumber("prefDialogSizeHeight");
		if(metadataDialogSizeWidth != null && metadataDialogSizeHeight != null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JeboorkerPreferences.getGenericEntryAsScreenLocation("prefDialogLocationX", "prefDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}	
}