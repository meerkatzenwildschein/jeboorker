package org.rr.jeborker.gui;

import javax.swing.JProgressBar;

public class JEBorkerMainMonitor {

	private JProgressBar progressbar;
	
	private int started = 0;
	
	JEBorkerMainMonitor(JProgressBar progressbar) {
		this.progressbar = progressbar;
	}
	
	public void monitorProgressStart(String message) {
		started++;
		progressbar.setIndeterminate(true);
		this.setMessage(message);
	}
	
	public void monitorProgressStop() {
		this.monitorProgressStop(Bundle.getString("JEBorkerMainMonitor.finished"));
	}
	
	public void monitorProgressStop(String message) {
		started--;
		if(started<=0) {
			started = 0;
			progressbar.setIndeterminate(false);
			this.setMessage(message);
		}
	}	
	
	public void setMessage(String message) {
		progressbar.setString(message != null ? message : "");
		progressbar.setStringPainted(true);
	}
}
