package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.rr.commons.swing.dialogs.PreferenceDialog;
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
		if(actionResult == PreferenceDialog.ACTION_RESULT_OK) {
			this.setPreferenceViewValues(view);
		}
	}
	
	private void setPreferenceViewValues(PreferenceView view) {
		boolean isTreeAutoscrollEnabled = view.getBooleanValue(PreferenceView.AUTO_SCOLL_ITEM_NAME);
		JeboorkerPreferences.setTreeAutoScrollingEnabled(isTreeAutoscrollEnabled);
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
		JFrame mainWindow = MainController.getController().getMainWindow();
		preferenceView.setLocation(mainWindow.getLocation().x, mainWindow.getLocation().y);
		preferenceView.setSize(800, 600);
		preferenceView.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		restorePropeties();		
	}
	
	public void close() {
		storeProperties();
		
		preferenceView.setVisible(false);
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
