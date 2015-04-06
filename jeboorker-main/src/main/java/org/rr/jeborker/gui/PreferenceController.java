package org.rr.jeborker.gui;

import java.awt.Point;
import java.util.List;

import javax.swing.JFrame;

import org.rr.commons.swing.dialogs.JPreferenceDialog;
import org.rr.commons.swing.dialogs.JPreferenceDialog.PreferenceEntry;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

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
		List<PreferenceEntry> preferenceEntries = view.getPreferenceEntries();
		for(PreferenceEntry entry : preferenceEntries) {
			Runnable okAction = entry.getOkAction();
			if(okAction != null) {
				okAction.run();
			}
		}
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
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("prefDialogSizeWidth", getView().getSize().width);
		preferenceStore.addGenericEntryAsNumber("prefDialogSizeHeight", getView().getSize().height);
		preferenceStore.addGenericEntryAsNumber("prefDialogLocationX", getView().getLocation().x);
		preferenceStore.addGenericEntryAsNumber("prefDialogLocationY", getView().getLocation().y);
	}

	private void restorePropeties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = preferenceStore.getGenericEntryAsNumber("prefDialogSizeWidth");
		Number metadataDialogSizeHeight = preferenceStore.getGenericEntryAsNumber("prefDialogSizeHeight");
		if(metadataDialogSizeWidth != null && metadataDialogSizeHeight != null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("prefDialogLocationX", "prefDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}	
}
