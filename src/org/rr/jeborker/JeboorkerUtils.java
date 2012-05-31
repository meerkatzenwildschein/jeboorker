package org.rr.jeborker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;

public class JeboorkerUtils {
	
	public static int OS_WINDOWS = 0;
	
	public static int OS_LINUX = 1;
	
	public static int OS_MAC = 2;
	
	public static int OS_UNKNOWN = 99;
	
	private static final ArrayList<IResourceHandler> temporaryResourceLoader = new ArrayList<IResourceHandler>();
	
	/**
	 * Tells the core application to shut down. All opened files
	 * and db connections will be closed. Temporary files will be deleted.
	 */
	public static void shutdown() {
		//delete temporary resources.
		for (int i = 0; i < temporaryResourceLoader.size(); i++) {
			try {
				IResourceHandler resourceHandler = temporaryResourceLoader.get(i);
				if(resourceHandler.exists()) {
					resourceHandler.delete();
				}
			} catch (IOException e) {
				LoggerFactory.log(Level.WARNING, JeboorkerUtils.class, "could not delete temporary file " + temporaryResourceLoader.get(i).getResourceString(), e);
			}
		}
	}

	/**
	 * Adds a {@link IResourceHandler} which should be handled as a temporary 
	 * {@link IResourceHandler}. These resources will be deleted on
	 * application shutdown 
	 * @param resourceLoader {@link IResourceHandler} to be deleted while shutting down the application.
	 */
	public static void addTemporaryResourceLoader(IResourceHandler resourceLoader) {
		temporaryResourceLoader.add(resourceLoader);
	}
	
	/**
	 * Gets the default configuration directory. The path have a trailing / 
	 * 
	 * @return The config file directory.
	 */
	public static String getConfigDirectory() {
		String result = System.getProperties().getProperty("user.home");
		if(String.valueOf(JeboorkerUtils.class.getResource("JeboorkerUtils.class")).indexOf("jar:")==-1) {
			result += File.separator + ".jeboorker.devel" + File.separator;
		} else {		
			result += File.separator + ".jeboorker" + File.separator;
		}
		result += File.separator + ".jeboorker" + File.separator;
		
		return result;
	}
	
    /**
     * Determines the type of operating system.
     * @return One of the constants:
     * 	<ul>
     * 		<li>OS_WINDOWS</li>
     * 		<li>OS_LINUX</li>
     * 		<li>OS_MAC</li>
     * 	</ul>
     */
    public static int getOS() {
    	String osName = System.getProperty("os.name").toLowerCase();
    	if(osName.indexOf("window")!=-1) {
    		return OS_WINDOWS;
    	} else if(osName.indexOf("linux")!=-1) {
    		return OS_LINUX;
    	} else if(osName.indexOf("mac")!=-1) {
    		return OS_LINUX;
    	}
    	
    	try {
    		Class.forName("sun.print.Win32PrintJob"); 
    		return OS_WINDOWS;
    	} catch (Exception e) {
    	}
    	
    	return OS_UNKNOWN;
    }	
}
