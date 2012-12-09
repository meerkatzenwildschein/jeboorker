package org.rr.commons.utils;

import java.awt.Desktop;
import java.io.File;

import org.rr.commons.mufs.ResourceHandlerFactory;

public class DesktopUtils {
	
	/**
	 * Using the freedesktop.org functions to open the given file or folder with the
	 * associated software.
	 * @return <code>true</code> if opening was successfully or <code>false</code> otherwise.
	 */
	public static boolean openFile(final File file) {
		try {
			if(CommonUtils.isLinux() && ResourceHandlerFactory.getResourceLoader("/usr/bin/xdg-open").exists()) {
				//try with xdg-open from freedesktop.org which is installed with the xdg-utils package. 
				String[] s = new String[] {"/usr/bin/xdg-open", file.toURI().toString()};
				Runtime.getRuntime().exec(s);
				return true;
			} else {
				Desktop.getDesktop().open(file);
				return true;
			}
		} catch (Exception e1) {
		}
		return false;
	}
	
	/**
	 * Opens the given folder in the associated software. If a file is given, the folder of 
	 * the file will be opened.
	 * @return <code>true</code> if opening was successfully or <code>false</code> otherwise.
	 */
	public static boolean openFolder(File file) {
		if(!file.isDirectory()) {
			file = file.getParentFile();
		}

		if(!openFile(file)) {
			boolean success = false;
			if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
				success = openLinuxFolder(file);
			} else if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
				success = openWindowsFolder(file);
			}
			return success;
		}
		return false;
	}


	private static boolean openWindowsFolder(File file) {
		if(new File("C:\\Windows\\explorer.exe").exists()) {
			try {
				Runtime.getRuntime().exec(new String[] {"C:\\Windows\\explorer.exe", "/n", "/e", "\"" + file.toString() + "\""});
				return true;
			} catch(Exception e2) {
				e2.printStackTrace(); //debug output
			}		
		}
		return false;
	}
	
	private static boolean openLinuxFolder(File file) {
		if(new File("/usr/bin/nemo").exists()) {
			try {
				Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "/usr/bin/nemo \"" + file.toString() + "\""});
				return true;
			} catch(Exception e2) {
				e2.printStackTrace(); //debug output
			}
		} if(new File("/usr/bin/nautilus").exists()) {
			try {
				//workaround for 6490730. It's already present with my ubuntu
				//http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6490730
			    Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "/usr/bin/nautilus \"" + file.toString() + "\""});
			    return true;
			} catch (Exception e2) {
				e2.printStackTrace(); //debug output
			}
		}		
		return false;
	}	
}
