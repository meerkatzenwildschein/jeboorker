package org.rr.jeborker.gui;

import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.Jeboorker;

public class MainMonitor {
	
	private static final int DEFAULT_CLEAN_TIMEOUT = 5000;

	private static MainMonitor instance;
	
	private static int clearTimeout = -1;
	
	private JProgressBar progressbar;

	private int started = 0;

	private boolean isEnabled;


	private MainMonitor(JProgressBar progressbar) {
		this.progressbar = progressbar;
		startMessageCleanerThread();
	}
	
	private void startMessageCleanerThread() {
		Jeboorker.APPLICATION_THREAD_POOL.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				while(true) {
					if(clearTimeout == -1) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							LoggerFactory.getLogger().log(Level.WARNING, "Sleep InterruptedException", e);
						}
					} else {
						try {
							Thread.sleep(clearTimeout);
							setMessage("");
							clearTimeout = -1;
						} catch (InterruptedException e) {
							LoggerFactory.getLogger().log(Level.WARNING, "Sleep InterruptedException", e);
						}
					}
				}
			}
		});
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
					clearTimeout = DEFAULT_CLEAN_TIMEOUT;
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
