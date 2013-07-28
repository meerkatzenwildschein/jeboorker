package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

public class AboutDialogController {
	
	private AboutDialogView aboutDialogView;	
	
	public static AboutDialogController getInstance() {
		AboutDialogController controller = new AboutDialogController();
		return controller;
	}

	public void showAboutDialog() {
		AboutDialogView view = getView();
		view.setVisible(true);
	}
	
	private AboutDialogView getView() {
		if (aboutDialogView == null) {
			JFrame mainWindow = MainController.getController().getMainWindow();
			aboutDialogView = new AboutDialogView(mainWindow, this);
			this.initialize();
		}
		return aboutDialogView;
	}
	
	private void initialize() {
		aboutDialogView.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		restorePropeties();
	}

	public void close() {
		storeProperties();
		
		aboutDialogView.setVisible(false);
		aboutDialogView.dispose();
	}
	
	private void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("aboutDialogLocationX", getView().getLocation().x);
		preferenceStore.addGenericEntryAsNumber("aboutDialogLocationY", getView().getLocation().y);
	}
	
	private void restorePropeties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		
		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("aboutDialogLocationX", "aboutDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}
}
