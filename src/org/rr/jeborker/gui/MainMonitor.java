package org.rr.jeborker.gui;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class MainMonitor {

	private JProgressBar progressbar;

	private int started = 0;

	private boolean isEnabled;

	private static MainMonitor instance;

	private MainMonitor(JProgressBar progressbar) {
		this.progressbar = progressbar;
	}

	static MainMonitor getInstance(JProgressBar progressbar) {
		if(instance == null) {
			instance = new MainMonitor(progressbar);
		}
		return instance;
	}

	public void monitorProgressStart(final String message) {
		if(isEnabled) {
			started++;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressbar.setIndeterminate(true);
					setMessage(message);
					blockMainFrame(true);
				}
			});
		}
	}

	public void blockMainFrame(boolean block) {
		MainController.getController().getMainWindow().getGlassPane().setVisible(block);
	}

	public void monitorProgressStop() {
		this.monitorProgressStop(Bundle.getString("JEBorkerMainMonitor.finished"));
	}

	public void monitorProgressStop(final String message) {
		if(isEnabled) {
			started--;
			if(started <= 0) {
				started = 0;

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						progressbar.setIndeterminate(false);
						progressbar.setValue(0);
						if(message != null) {
							setMessage(message);
						}
						blockMainFrame(false);
					}
				});
			}
		}
	}

	public void clearMessage() {
		setMessage("");
	}

	public void setMessage(final String message) {
		if(isEnabled) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressbar.setString(message != null ? message : "");
					progressbar.setStringPainted(true);
					progressbar.setToolTipText(message != null ? message : "");
				}
			});
		}
	}

	public void setProgress(final int progress, final int max) {
		if(isEnabled) {
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

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}
}
