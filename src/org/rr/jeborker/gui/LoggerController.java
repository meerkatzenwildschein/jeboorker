package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JFrame;

import org.rr.jeborker.JeboorkerPreferences;

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
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogSizeWidth", getView().getSize().width);
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogSizeHeight", getView().getSize().height);
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogLocationX", getView().getLocation().x);
		JeboorkerPreferences.addGenericEntryAsNumber("logDialogLocationY", getView().getLocation().y);
	}	

	private void restorePropeties() {
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = JeboorkerPreferences.getGenericEntryAsNumber("logDialogSizeWidth");
		Number metadataDialogSizeHeight = JeboorkerPreferences.getGenericEntryAsNumber("logDialogSizeHeight");
		if(metadataDialogSizeWidth!=null && metadataDialogSizeHeight!=null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JeboorkerPreferences.getGenericEntryAsScreenLocation("logDialogLocationX", "logDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}
}
