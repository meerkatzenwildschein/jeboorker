package org.rr.jeborker;

import java.awt.EventQueue;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.gui.MainController;

public class JEBorker {
	
	public static boolean isRuntime = false;
	
	public static String version = "0.1b";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		isRuntime = true;
		LoggerFactory.addHandler(new JEBorkerConsoleLogger());
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
