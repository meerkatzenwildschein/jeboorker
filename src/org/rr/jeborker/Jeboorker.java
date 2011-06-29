package org.rr.jeborker;

import java.awt.EventQueue;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.gui.MainController;

public class Jeboorker {
	
	public static boolean isRuntime = false;
	
	public static String version = "0.1.1";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		isRuntime = true;
		LoggerFactory.addHandler(new JeboorkerLogger());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainController.getController();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
}
