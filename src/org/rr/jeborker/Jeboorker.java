package org.rr.jeborker;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;

import javax.swing.UIManager;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.gui.MainController;

public class Jeboorker {

	public static boolean isRuntime = false;

	public static String version = "0.1.5";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		isRuntime = true;
		
		//setup the logger
		LoggerFactory.addHandler(new JeboorkerLogger());

		setupClasspath();
		setupLookAndFeel();		

		//start the application
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainController.getController();
				} catch (Exception e) {
					LoggerFactory.log(Level.SEVERE, null, "Main failed", e); 
					System.exit(-1);
				}
			}
		});
	}

	private static void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			LoggerFactory.logWarning(MainController.class, "Could not set system look and feel");
		}
	}

	private static void setupClasspath() {
		String result = System.getProperties().getProperty("user.dir");
		try {
			addPath(new File(result + File.separator + "lib/"));
			addPath(new File(result + File.separator + "lib/orientdb/"));
			addPath(new File(result + File.separator + "lib/epubcheck/"));
		} catch (Exception e1) {
			LoggerFactory.log(Level.SEVERE, null, "Classpath failed", e1); 
			System.exit(-1);
		}
	}

	/**
	 * Add a classpath to the default system classloader.
	 * @param dir The dir with jars to be added (not recursively)
	 * @throws Exception
	 */
	public static void addPath(File dir) throws Exception {
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});

		if(files != null) {
			for (int i = 0; i < files.length; i++) {
				URL u = files[i].toURL();
				URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				Class<URLClassLoader> urlClass = URLClassLoader.class;
				Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
				method.setAccessible(true);
				method.invoke(urlClassLoader, new Object[] { u });
			}
		}
	}

}
