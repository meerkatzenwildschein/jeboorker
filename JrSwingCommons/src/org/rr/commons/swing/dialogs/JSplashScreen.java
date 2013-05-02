package org.rr.commons.swing.dialogs;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

import org.rr.common.swing.SwingUtils;

public class JSplashScreen extends JDialog {

	private static JSplashScreen splashScreenInstance;
	
	private static Thread worker;
	
	private JLabel lblLoad;
	
	private JProgressBar progressBar;
	
	JSplashScreen() {
		setUndecorated(true);
		setModal(true);
		setSize(400, 50);
		setResizable(false);
		setAlwaysOnTop(true);
		SwingUtils.centerOnScreen(this);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Loading...", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setValue(0);
		panel.add(progressBar, BorderLayout.CENTER);
		
		lblLoad = new JLabel();
		panel.add(lblLoad, BorderLayout.SOUTH);
	}

	/**
	 * Get the {@link JSplashScreen} singleton instance.
	 */
	public static JSplashScreen getInstance() {
		if(splashScreenInstance == null) {
			splashScreenInstance = new JSplashScreen();
		}
		return splashScreenInstance;
	}
	
	public void startProgress(final String text) {
		if(worker == null) {
			worker = new Thread(new Runnable() {
				
				@Override
				public void run() {
					JSplashScreen splash = getInstance();
					splash.lblLoad.setText(text);
					splash.setVisible(true);
					worker = null;
				}
			});
			worker.start();
		}
	}
	
	public void endProgress() {
		getInstance().setVisible(false);
	}
	
	/**
	 * Set the loading text displayed at the bottom of the splash screen.
	 */
	public void setLoadingText(String text) {
		lblLoad.setText(text);
	}

	/**
	 * Set the progressbar value.
	 * @param value A value between 0 and 100.
	 */
	public void setLoadingValue(int value) {
		progressBar.setValue(value);
	}
	
}
