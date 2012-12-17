package org.rr.jeborker.gui;

import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.rr.jeborker.JeboorkerLogger;
import org.rr.jeborker.JeboorkerPreferences;

public class LogMonitorController {
	
	private LogMonitorView logMonitorView = null;
	
	private static int locationOffset = 0;
	
	public static LogMonitorController getInstance() {
		LogMonitorController controller = new LogMonitorController();
		return controller;
	}	
	
	public void showLogMonitorDialog() {
		LogMonitorView view = getView();
		view.setVisible(true);
	}	
	
	private LogMonitorView getView() {
		if(logMonitorView==null) {
			JFrame mainWindow = MainController.getController().getMainWindow();
			logMonitorView = new LogMonitorView(mainWindow, JeboorkerLogger.log);
			this.initialize();
		}
		return logMonitorView;
	}
	
	private void initialize() {
		JFrame mainWindow = MainController.getController().getMainWindow();
		locationOffset = locationOffset + 10;
		logMonitorView.setLocation(mainWindow.getLocation().x + locationOffset, mainWindow.getLocation().y + locationOffset);
		logMonitorView.setSize(800, 600);
		logMonitorView.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		restorePropeties();
	}	
	
	public void close() {
		storeProperties();
		locationOffset -= 10;
		
		logMonitorView.setVisible(false);
		logMonitorView.dispose();
	}	
	
	private void storeProperties() {
		JeboorkerPreferences.addEntryNumber("logDialogSizeWidth", getView().getSize().width);
		JeboorkerPreferences.addEntryNumber("logDialogSizeHeight", getView().getSize().height);
		JeboorkerPreferences.addEntryNumber("logDialogLocationX", getView().getLocation().x - locationOffset);
		JeboorkerPreferences.addEntryNumber("logDialogLocationY", getView().getLocation().y - locationOffset);
	}	

	private void restorePropeties() {
		//restore the window size from the preferences.
		Number metadataDialogSizeWidth = JeboorkerPreferences.getEntryAsNumber("logDialogSizeWidth");
		Number metadataDialogSizeHeight = JeboorkerPreferences.getEntryAsNumber("logDialogSizeHeight");
		if(metadataDialogSizeWidth!=null && metadataDialogSizeHeight!=null) {
			getView().setSize(metadataDialogSizeWidth.intValue(), metadataDialogSizeHeight.intValue());
		}
		
		//restore window location
		Point entryAsScreenLocation = JeboorkerPreferences.getEntryAsScreenLocation("logDialogLocationX", "logDialogLocationY");
		if(entryAsScreenLocation != null) {
			getView().setLocation(entryAsScreenLocation);
		}		
	}
	
	
}
