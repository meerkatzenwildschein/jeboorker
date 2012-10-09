package org.rr.jeborker.gui;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class MainMonitor {

	private JProgressBar progressbar;
	
	private int started = 0;
	
	MainMonitor(JProgressBar progressbar) {
		this.progressbar = progressbar;
	}
	
	public void monitorProgressStart(final String message) {
		started++;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressbar.setIndeterminate(true);
				setMessage(message);
				MainController.getController().getMainWindow().getGlassPane().setVisible(true);
			}
		});
	}
	
	public void monitorProgressStop() {
		this.monitorProgressStop(Bundle.getString("JEBorkerMainMonitor.finished"));
	}
	
	public void monitorProgressStop(final String message) {
		started--;
		if(started<=0) {
			started = 0;
						
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					progressbar.setIndeterminate(false);
					progressbar.setValue(0);
					if(message != null) {
						setMessage(message);
					}
					MainController.getController().getMainWindow().getGlassPane().setVisible(false);
				}
			});			
		}
	}	
	
	public void setMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressbar.setString(message != null ? message : "");
				progressbar.setStringPainted(true);
				progressbar.setToolTipText(message != null ? message : "");
			}
		});			

	}
	
	public void setProgress(final int progress, final int max) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressbar.setIndeterminate(false);
				progressbar.setMinimum(0);
				progressbar.setMaximum(max);
				progressbar.setValue(progress);
			}
		});			
	}
}
