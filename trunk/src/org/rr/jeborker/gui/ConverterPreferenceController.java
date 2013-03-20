package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.commons.swing.dialogs.PreferenceDialog;
import org.rr.jeborker.JeboorkerPreferences;

public class ConverterPreferenceController {
	
	private ConverterPreferenceView preferenceView;
	
	private int actionResult = -1;

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
		JeboorkerPreferences.addGenericEntryAsNumber("prefConverterDialogSizeWidth", getView().getSize().width);
		JeboorkerPreferences.addGenericEntryAsNumber("prefConverterDialogSizeHeight", getView().getSize().height);
		JeboorkerPreferences.addGenericEntryAsNumber("prefConverterDialogLocationX", getView().getLocation().x);
		JeboorkerPreferences.addGenericEntryAsNumber("prefConverterDialogLocationY", getView().getLocation().y);
	}

	private void restorePropeties() {
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = JeboorkerPreferences.getGenericEntryAsNumber("prefConverterDialogSizeWidth");
		Number metadataDialogSizeHeight = JeboorkerPreferences.getGenericEntryAsNumber("prefConverterDialogSizeHeight");
		if(metadataDialogSizeWidth != null && metadataDialogSizeHeight != null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JeboorkerPreferences.getGenericEntryAsScreenLocation("prefConverterDialogLocationX", "prefConverterDialogLocationY");
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
		return this.actionResult == PreferenceDialog.ACTION_RESULT_OK;
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
		String landscapeFormatValue = preferenceView.getStringValue(ConverterPreferenceView.LANDSCAPE_FORMAT_PREFERENCE_NAME);
		if(landscapeFormatValue.equals(Bundle.getString("ConverterPreferenceView.pref.landscape.rotate_clockwise"))) {
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
	
	/**
	 * Tells if the landscape page format options are enabled to show or not.
	 */
	public boolean isShowLandscapePageEntries() {
		return preferenceView.isShowLandscapePageEntries();
	}

	/**
	 * Set the landscape page format options to be shown in the dialog.
	 */
	public void setShowLandscapePageEntries(boolean showLandscapePageEntries) {
		preferenceView.setShowLandscapePageEntries(showLandscapePageEntries);
	}	
}
