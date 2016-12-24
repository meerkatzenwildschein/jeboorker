package org.rr.jeborker.gui;

import java.awt.Point;
import java.util.List;

import javax.swing.JFrame;

import org.rr.commons.swing.dialogs.JPreferenceDialog;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

public class ConverterPreferenceController {
	
	private ConverterPreferenceView preferenceView;
	
	private int actionResult = -1;

	private boolean hasShown = false;
	
	private ConverterPreferenceController() {
	}
	
	static ConverterPreferenceController getInstance() {
		ConverterPreferenceController controller = new ConverterPreferenceController();
		return controller;
	}		

	public void showPreferenceDialog() {
		ConverterPreferenceView view = getView();
		view.setVisible(true);
		actionResult = view.getActionResult();
		hasShown = true;
	}
	
	/**
	 * Tells if the dialog has already been shown to the user.
	 */
	public boolean hasShown() {
		return this.hasShown;
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
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("prefConverterDialogSizeWidth", getView().getSize().width);
		preferenceStore.addGenericEntryAsNumber("prefConverterDialogSizeHeight", getView().getSize().height);
		preferenceStore.addGenericEntryAsNumber("prefConverterDialogLocationX", getView().getLocation().x);
		preferenceStore.addGenericEntryAsNumber("prefConverterDialogLocationY", getView().getLocation().y);
	}

	private void restorePropeties() {
		//restore the window size from the preferences.
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		Number metadataDialogSizeWidth = preferenceStore.getGenericEntryAsNumber("prefConverterDialogSizeWidth");
		Number metadataDialogSizeHeight = preferenceStore.getGenericEntryAsNumber("prefConverterDialogSizeHeight");
		if(metadataDialogSizeWidth != null && metadataDialogSizeHeight != null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("prefConverterDialogLocationX", "prefConverterDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}	
	
	/**
	 * Tells if the user has confirmed the preference dialog.
	 * @return <code>true</code> if the user has confirmed the dialog and <code>false</code> if 
	 * he hits abort or just closed the dialog.
	 */
	public boolean isConfirmed() {
		return this.actionResult == JPreferenceDialog.ACTION_RESULT_OK;
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
		if(landscapeFormatValue.equals(Bundle.getString("ConverterPreferenceView.pref.landscape.rotate_clockwise"))) {
			return true;
		} else if(landscapeFormatValue.equals(Bundle.getString("ConverterPreferenceView.pref.landscape.rotate_counterclockwise"))) {
			return true;
		}

		return false;
	}
	
	/**
	 * Tells if the rotation mode is clockwise. Use {@link #isLandscapePageRotate()} first
	 * for a reasonable usage.
	 */
	public boolean isRotateClockwise() {
		String landscapeFormatValue = getView().getStringValue(ConverterPreferenceView.LANDSCAPE_FORMAT_PREFERENCE_NAME);
		if(landscapeFormatValue.equals(Bundle.getString("ConverterPreferenceView.pref.landscape.rotate_clockwise"))) {
			return true;
		} 
		return false;
	}
	
	/**
	 * Tells if the manga mode for landscape pages is set. 
	 */
	public boolean isMangaMode() {
		return getView().getBooleanValue(ConverterPreferenceView.IS_MANGA_PREFERENCE_NAME);
	}	
	
	/**
	 * Sets the visibility of the landscape setup entries. 
	 */
	public void setShowLandscapePageEntries(boolean showLandscapePageEntries) {
		getView().setShowLandscapePageEntries(showLandscapePageEntries);
	}
	
	public void addCommonSlider(String label, int defaultValue) {
		getView().addCommonSlider(label, defaultValue);
	}
	
	public void addCommonListSelection(String label, List<String> entries, String selectedEntry) {
		getView().addCommonListSelection(label, entries, selectedEntry);
	}
	
	public void addCommonCheckBox(String label, boolean checked) {
		getView().addCommonCheckBox(label, checked);
	}
	
	public String getCommonValueAsString(String label) {
		return getView().getStringValue(label);
	}
	
	public boolean getCommonValueAsBoolean(String label) {
		return getView().getBooleanValue(label);
	}
	
	public int getCommonValueAsInt(String label) {
		return getView().getNumericValue(label).intValue();
	}
	
}
