package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;

public class LoggerController {

	private static final String LOG_DIALOG_LOCATION_Y = "logDialogLocationY";

	private static final String LOG_DIALOG_LOCATION_X = "logDialogLocationX";

	private static final String LOG_DIALOG_SIZE_HEIGHT = "logDialogSizeHeight";

	private static final String LOG_DIALOG_SIZE_WIDTH = "logDialogSizeWidth";

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
		if (loggerView == null) {
			loggerView = new LoggerView(mainWindow, this);
			this.initialize();
		}
		return loggerView;
	}

	private void initialize() {
		restoreProperties();
	}

	public void close() {
		storeProperties();

		loggerView.setVisible(false);
		loggerView.dispose();
	}

	private void storeProperties() {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		preferenceStore.addGenericEntryAsNumber(LOG_DIALOG_SIZE_WIDTH, getView().getSize().width);
		preferenceStore.addGenericEntryAsNumber(LOG_DIALOG_SIZE_HEIGHT, getView().getSize().height);
		preferenceStore.addGenericEntryAsNumber(LOG_DIALOG_LOCATION_X, getView().getLocation().x);
		preferenceStore.addGenericEntryAsNumber(LOG_DIALOG_LOCATION_Y, getView().getLocation().y);
	}

	private void restoreProperties() {
		restoreWindowSizeProperties();
		restoreWindowLocationProperties();
	}

	private void restoreWindowLocationProperties() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		Point entryAsScreenLocation = preferenceStore.getGenericEntryAsScreenLocation(LOG_DIALOG_LOCATION_X, LOG_DIALOG_LOCATION_Y);
		if (entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}
	}

	private void restoreWindowSizeProperties() {
		APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		Number metadataDialogSizeWidth = preferenceStore.getGenericEntryAsNumber(LOG_DIALOG_SIZE_WIDTH);
		Number metadataDialogSizeHeight = preferenceStore.getGenericEntryAsNumber(LOG_DIALOG_SIZE_HEIGHT);
		if (metadataDialogSizeWidth != null && metadataDialogSizeHeight != null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
	}
}
