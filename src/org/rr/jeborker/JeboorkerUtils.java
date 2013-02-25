package org.rr.jeborker;

import java.io.File;

import org.rr.commons.utils.StringUtils;

public class JeboorkerUtils {
	
	public static int OS_WINDOWS = 0;
	
	public static int OS_LINUX = 1;
	
	public static int OS_MAC = 2;
	
	public static int OS_UNKNOWN = 99;
	
	/**
	 * Gets the default configuration directory. The path have a trailing / 
	 * 
	 * @return The config file directory.
	 */
	public static String getConfigDirectory() {
		String result = System.getProperties().getProperty("user.home");
		String suffix = System.getProperties().getProperty("application.suffix");
		
		result += File.separator + ".jeboorker" + (StringUtils.isNotEmpty(suffix) ? "." + suffix : "") + File.separator;
		
		return result;
	}
	
}
