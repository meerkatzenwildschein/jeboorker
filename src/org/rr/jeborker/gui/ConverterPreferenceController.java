package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.commons.swing.dialogs.PreferenceDialog;
import org.rr.jeborker.JeboorkerPreferences;

public class ConverterPreferenceController {
	
	private ConverterPreferenceView preferenceView;

	private ConverterPreferenceController() {
		
	}
	
	static ConverterPreferenceController getInstance() {
		ConverterPreferenceController controller = new ConverterPreferenceController();
		return controller;
	}		

	public void showPreferenceDialog() {
		ConverterPreferenceView view = getView();
		view.setVisible(true);
		
		int actionResult = view.getActionResult();
		if(actionResult == PreferenceDialog.ACTION_RESULT_OK) {
			this.setPreferenceViewValues(view);
		}
	}
	
	private void setPreferenceViewValues(ConverterPreferenceView view) {
		boolean isTreeAutoscrollEnabled = view.getBooleanValue(PreferenceView.AUTO_SCOLL_ITEM_PREFERENCE_NAME);
		JeboorkerPreferences.setTreeAutoScrollingEnabled(isTreeAutoscrollEnabled);
	}
	
	private ConverterPreferenceView getView() {
		if(preferenceView == null) {
			JFrame mainWindow = MainController.getController().getMainWindow();
			this.preferenceView = new ConverterPreferenceView(this, mainWindow);
			this.initialize();
		}
		return preferenceView;
	}

	private void initialize() {
		restorePropeties();		
	}
	
	public void close() {
		storeProperties();
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
	
	
	/**
	 * Tells if the split mode for landscape pages is set. 
	 */
	public boolean isLandscapePageSplit() {
		String landscapeFormatValue = preferenceView.getStringValue(ConverterPreferenceView.LANDSCAPE_FORMAT_PREFERENCE_NAME);
		if(landscapeFormatValue.equals(Bundle.getString("ConverterPreferenceView.pref.landscape.split"))) {
			return true;
		}
		return false;
	}
	
	/**
	 * Tells if the rotate mode for landscape pages is set. 
	 */
	public boolean isLandscapePageRotate() {
		String landscapeFormatValue = preferenceView.getStringValue(ConverterPreferenceView.LANDSCAPE_FORMAT_PREFERENCE_NAME);
		if(landscapeFormatValue.equals(Bundle.getString("ConverterPreferenceView.pref.landscape.rotate"))) {
			return true;
		}
		return false;
	}
	
	/**
	 * Tells if the manga mode for landscape pages is set. 
	 */
	public boolean isMangaMode() {
		boolean isManga = preferenceView.getBooleanValue(ConverterPreferenceView.IS_MANGA_PREFERENCE_NAME);
		return isManga;
	}	
}
