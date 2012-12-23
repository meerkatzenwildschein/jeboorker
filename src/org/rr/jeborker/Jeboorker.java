package org.rr.jeborker;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;

import java.awt.EventQueue;
import java.awt.Frame;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.UIManager;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.gui.MainController;

public class Jeboorker {

	public static boolean isRuntime = false;

	public static String version = "0.1.8";

	private static MainController mainController = null; 
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		isRuntime = true;
		
		//setup the logger
		LoggerFactory.addHandler(new JeboorkerLogger());

		setupClasspath();
		
		boolean start = true;
		try {
			JUnique.acquireLock(Jeboorker.class.getName(), new MessageHandler() {
				public String handle(String message) {			
					if (mainController != null) {
						java.awt.EventQueue.invokeLater(new Runnable() {
						    @Override
						    public void run() {
						        int state = mainController.getMainWindow().getExtendedState();
						        state &= ~Frame.ICONIFIED;
						        mainController.getMainWindow().setExtendedState(state);
						        mainController.getMainWindow().setFocusableWindowState(false);
						    	mainController.getMainWindow().toFront();	
						    	try {
						    		//seems to be needed to work. Don't know why.
									Thread.sleep(1000);
								} catch (InterruptedException e) {
								}
						    	mainController.getMainWindow().setFocusableWindowState(true);
						    }
						});
					}
					return null;
				}
			});
		} catch (AlreadyLockedException e) {
			// Application already running.
			start = false;
		}
		
		if(start) {
			setupLookAndFeel();		
	
			//start the application
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						mainController = MainController.getController();
					} catch (Exception e) {
						LoggerFactory.log(Level.SEVERE, null, "Main failed", e); 
						System.exit(-1);
					}
				}
			});
		} else {
			// Sends arguments to the already active instance.
			JUnique.sendMessage(Jeboorker.class.getName(), "");
			
			LoggerFactory.log(Level.INFO, Jeboorker.class, "Jeboorker " + version + " is already running.");
		}
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
		final String result = System.getProperties().getProperty("user.dir");
		final Set<String> jarFileSet = new HashSet<String>();
		
		try {
			addPath(new File(result + File.separator + "lib/"), jarFileSet);
			addPath(new File(result + File.separator + "lib/orientdb/"), jarFileSet);
			addPath(new File(result + File.separator + "lib/epubcheck/"), jarFileSet);
			addPath(new File(result + File.separator + "lib/epublib/"), jarFileSet);
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
	public static void addPath(final File dir, final Set<String> jarFileSet) throws Exception {
		if(dir != null && dir.isDirectory()) {
			final File[] files = dir.listFiles(new FileFilter() {
	
				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(".jar");
				}
			});
	
			if(files != null) {
				final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				final Class<URLClassLoader> urlClass = URLClassLoader.class;
				final Method method = urlClass.getDeclaredMethod("addURL", new Class[] { URL.class });
				method.setAccessible(true);
				for (int i = 0; i < files.length; i++) {
					if(jarFileSet == null || !jarFileSet.contains(files[i].getName())) {
						URL u = files[i].toURI().toURL();
						method.invoke(urlClassLoader, new Object[] { u });
						jarFileSet.add(files[i].getName());
					}
				}
			}
		}
	}

}