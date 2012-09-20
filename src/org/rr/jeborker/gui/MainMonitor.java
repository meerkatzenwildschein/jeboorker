package org.rr.jeborker.gui;

import javax.swing.JProgressBar;

public class MainMonitor {

	private JProgressBar progressbar;
	
	private int started = 0;
	
	MainMonitor(JProgressBar progressbar) {
		this.progressbar = progressbar;
	}
	
	public void monitorProgressStart(String message) {
		started++;
		progressbar.setIndeterminate(true);
		this.setMessage(message);
		MainController.getController().getMainWindow().getGlassPane().setVisible(true);
	}
	
	public void monitorProgressStop() {
		this.monitorProgressStop(Bundle.getString("JEBorkerMainMonitor.finished"));
	}
	
	public void monitorProgressStop(String message) {
		started--;
		if(started<=0) {
			started = 0;
			progressbar.setIndeterminate(false);
			progressbar.setValue(0);
			this.setMessage(message);
			MainController.getController().getMainWindow().getGlassPane().setVisible(false);
		}
	}	
	
	public void setMessage(String message) {
		progressbar.setString(message != null ? message : "");
		progressbar.setStringPainted(true);
		progressbar.setToolTipText(message != null ? message : "");
	}
	
	public void setProgress(int progress, int max) {
		progressbar.setIndeterminate(false);
		progressbar.setMinimum(0);
		progressbar.setMaximum(max);
		progressbar.setValue(progress);
	}
}
