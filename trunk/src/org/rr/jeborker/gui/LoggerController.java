package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

public class LoggerController {
	
	private static LoggerView loggerView = null;
	
	private JFrame mainWindow;
	
	public LoggerController(JFrame mainWindow) {
		this.mainWindow = mainWindow;
	}

	static LoggerController getInstance(JFrame mainWindow) {
		LoggerController controller = new LoggerController(mainWindow);
		return controller;
	}	
	
	public void showLoggerDialog() {
		LoggerView view = getView();
		view.setVisible(true);
	}	
	
	private LoggerView getView() {
		if(loggerView == null) {
			loggerView = new LoggerView(mainWindow, this);
			this.initialize();
		}
		return loggerView;
	}
	
	private void initialize() {
		restorePropeties();
	}	
	
	public void close() {
		storeProperties();
		
		loggerView.setVisible(false);
		loggerView.dispose();
	}	
	
	private void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber("logDialogSizeWidth", getView().getSize().width);
		preferenceStore.addGenericEntryAsNumber("logDialogSizeHeight", getView().getSize().height);
		preferenceStore.addGenericEntryAsNumber("logDialogLocationX", getView().getLocation().x);
		preferenceStore.addGenericEntryAsNumber("logDialogLocationY", getView().getLocation().y);
	}	

	private void restorePropeties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = preferenceStore.getGenericEntryAsNumber("logDialogSizeWidth");
		Number metadataDialogSizeHeight = preferenceStore.getGenericEntryAsNumber("logDialogSizeHeight");
		if(metadataDialogSizeWidth!=null && metadataDialogSizeHeight!=null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation("logDialogLocationX", "logDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}
}
