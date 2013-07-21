package org.rr.jeborker;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;

import java.awt.Frame;
import java.io.File;
import java.io.FileFilter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.OceanTheme;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ListUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.commons.utils.compression.rar.RarUtils;
import org.rr.jeborker.app.JeboorkerLogger;
import org.rr.jeborker.gui.MainController;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertGreen;
import com.jgoodies.looks.plastic.theme.DesertRed;
import com.jgoodies.looks.plastic.theme.DesertYellow;
import com.jgoodies.looks.plastic.theme.ExperienceGreen;
import com.jgoodies.looks.plastic.theme.ExperienceRoyale;
import com.jgoodies.looks.plastic.theme.SkyBlue;
import com.jgoodies.looks.plastic.theme.SkyGreen;
import com.jgoodies.looks.plastic.theme.SkyKrupp;
import com.jgoodies.looks.plastic.theme.SkyRed;
import com.jgoodies.looks.plastic.theme.SkyYellow;

public class Jeboorker {

	public static final ExecutorService APPLICATION_THREAD_POOL = new ThreadPoolExecutor(0, 1024,
			60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ApplicationThreadFactory()) {};
			
	public static final Map<String, String> LOOK_AND_FEELS = new LinkedHashMap<String, String>() {
		{
			put("Plastic3D;SkyBlue", Plastic3DLookAndFeel.class.getName() + ";" + SkyBlue.class.getName());
			put("Plastic3D;SkyGreen", Plastic3DLookAndFeel.class.getName() + ";" + SkyGreen.class.getName());
			put("Plastic3D;SkyKrupp", Plastic3DLookAndFeel.class.getName() + ";" + SkyKrupp.class.getName());
			put("Plastic3D;SkyRed", Plastic3DLookAndFeel.class.getName() + ";" + SkyRed.class.getName());
			put("Plastic3D;SkyYello", Plastic3DLookAndFeel.class.getName() + ";" + SkyYellow.class.getName());
			put("Plastic3D;DesertGreen", Plastic3DLookAndFeel.class.getName() + ";" + DesertGreen.class.getName());
			put("Plastic3D;DesertRed", Plastic3DLookAndFeel.class.getName() + ";" + DesertRed.class.getName());
			put("Plastic3D;DesertYellow", Plastic3DLookAndFeel.class.getName() + ";" + DesertYellow.class.getName());
			put("Plastic3D;Ocean", Plastic3DLookAndFeel.class.getName() + ";" + OceanTheme.class.getName());
			put("Plastic3D;ExperienceGreen", Plastic3DLookAndFeel.class.getName() + ";" + ExperienceGreen.class.getName());
			put("Plastic3D;ExperienceRoyale", Plastic3DLookAndFeel.class.getName() + ";" + ExperienceRoyale.class.getName());
			
			put("PlasticXP;SkyBlue", PlasticXPLookAndFeel.class.getName() + ";" + SkyBlue.class.getName());
			put("PlasticXP;SkyGreen", PlasticXPLookAndFeel.class.getName() + ";" + SkyGreen.class.getName());
			put("PlasticXP;SkyKrupp", PlasticXPLookAndFeel.class.getName() + ";" + SkyKrupp.class.getName());
			put("PlasticXP;SkyRed", PlasticXPLookAndFeel.class.getName() + ";" + SkyRed.class.getName());
			put("PlasticXP;SkyYello", PlasticXPLookAndFeel.class.getName() + ";" + SkyYellow.class.getName());
			put("PlasticXP;DesertGreen", PlasticXPLookAndFeel.class.getName() + ";" + DesertGreen.class.getName());
			put("PlasticXP;DesertRed", PlasticXPLookAndFeel.class.getName() + ";" + DesertRed.class.getName());
			put("PlasticXP;DesertYellow", PlasticXPLookAndFeel.class.getName() + ";" + DesertYellow.class.getName());
			put("PlasticXP;Ocean", PlasticXPLookAndFeel.class.getName() + ";" + OceanTheme.class.getName());
			put("PlasticXP;ExperienceGreen", PlasticXPLookAndFeel.class.getName() + ";" + ExperienceGreen.class.getName());
			put("PlasticXP;ExperienceRoyale", PlasticXPLookAndFeel.class.getName() + ";" + ExperienceRoyale.class.getName());
			
			final LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
			for(LookAndFeelInfo laf : installedLookAndFeels) {
				String className = laf.getClassName();
				String name = className.substring(className.lastIndexOf('.') + 1);
				if(name.startsWith("Motif")) {
					continue;
				}
				put(name, className);
			}
		}
	};

	public static boolean isRuntime = false;

	public static String version = "0.3.5";
	
	public static String app = "Jeboorker";

	private static MainController mainController; 
	
	public static long startupTime = System.currentTimeMillis();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		isRuntime = true;
		
		//setup the logger
		LoggerFactory.addHandler(new JeboorkerLogger());

		setupClasspath();
		
		//Setup the location for the rar executables.
		if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
			RarUtils.setRarExecFolder(getAppFolder() + File.separator + "exec");
		} else {
			RarUtils.setRarExecFolder("/usr/bin");
		}
		
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
					    		//seems to be needed to work. Don't know why.
					    		ReflectionUtils.sleepSilent(1000);
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
			try {
				logSystemInfo();
				
				mainController = MainController.getController();
			} catch (Exception e) {
				LoggerFactory.log(Level.SEVERE, null, "Main failed", e); 
				System.exit(-1);
			}
		} else {
			// Sends arguments to the already active instance.
			JUnique.sendMessage(Jeboorker.class.getName(), "");
			
			LoggerFactory.log(Level.INFO, Jeboorker.class, "Jeboorker " + version + " is already running.");
		}
	}
	
	/**
	 * Dumps the application version and the vm start parameters to the log 
	 */
	private static void logSystemInfo() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		List<String> args = bean.getInputArguments();
		String argsString = ListUtils.join(args, " ");
		
		LoggerFactory.getLogger().info("Jeboorker " + Jeboorker.version + " started with " + argsString);
		
		Properties props = System.getProperties();
		Set<Object> keys = props.keySet();
		for(Object key : keys) {
			Object value = props.get(key);
			LoggerFactory.getLogger().info(StringUtils.toString(key) + "=" + StringUtils.toString(value));
		}
		
	}
	
	/**
	 * Get the application folder. Jeboorker must be configured that the app folder
	 * is the current directory. 
	 */
	public static String getAppFolder() {
		return System.getProperties().getProperty("user.dir");
	}

	private static void setupClasspath() {
		final String appFolder = getAppFolder();
		final Set<String> jarFileSet = new HashSet<String>();
		
		try {
			addPath(new File(appFolder + File.separator + "lib/"), jarFileSet);
			addPath(new File(appFolder + File.separator + "lib/orientdb/"), jarFileSet);
			addPath(new File(appFolder + File.separator + "lib/epubcheck/"), jarFileSet);
			addPath(new File(appFolder + File.separator + "lib/epublib/"), jarFileSet);
			addPath(new File(appFolder + File.separator + "lib/dropbox/"), jarFileSet);
			addPath(new File(appFolder + File.separator + "lib/jmupdf/"), jarFileSet);
			
			String nativeLibPath = appFolder + File.separator + "lib/jmupdf/";
			ReflectionUtils.addLibraryPath(nativeLibPath);
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

	private static class ApplicationThreadFactory implements ThreadFactory {
		
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		
		private final ThreadGroup group;
		
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		
		private final String namePrefix;

		ApplicationThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(final Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}
	}	

}
