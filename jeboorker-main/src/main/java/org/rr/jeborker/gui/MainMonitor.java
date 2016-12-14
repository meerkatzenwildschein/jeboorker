package org.rr.jeborker.gui;

import static org.rr.commons.utils.StringUtil.EMPTY;

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
							if(!isBlocked()) {
								clearMessage();
								clearTimeout = -1;
							}
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

	public void monitorProgressStart(final String message, final boolean indeterminate) {
		if(isEnabled) {
			started++;
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressbar.setIndeterminate(indeterminate);
					setMessage(message);
					blockMainFrame(true);
				}
			});
		}
	}
	
	public void monitorProgressStart(final String message) {
		monitorProgressStart(message, true);
	}

	public MainMonitor blockMainFrame(boolean block) {
		MainController.getController().getMainWindow().getGlassPane().setVisible(block);
		return this;
	}
	
	private boolean isBlocked() {
		return MainController.getController().getMainWindow().getGlassPane().isVisible();
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
		setMessage(EMPTY);
	}

	public void setMessage(final String message) {
		if(isEnabled) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressbar.setString(message != null ? message : EMPTY);
					progressbar.setStringPainted(true);
					progressbar.setToolTipText(message != null ? message : EMPTY);
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
					progressbar.setString(progressbar.getString());
				}
			});
		}
	}
	
	public void resetProgress() {
		if(isEnabled) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					progressbar.setIndeterminate(false);
					progressbar.setMinimum(0);
					progressbar.setMaximum(100);
					progressbar.setValue(0);
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
