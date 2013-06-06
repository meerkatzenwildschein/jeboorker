package org.rr.commons.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.rr.commons.mufs.ResourceHandlerFactory;

public class DesktopUtils {
	
	/**
	 * Using the freedesktop.org functions to open the given file or folder with the
	 * associated software.
	 * @return <code>true</code> if opening was successfully or <code>false</code> otherwise.
	 */
	public static boolean openFile(final File file) {
		try {
			if(CommonUtils.isLinux() && ResourceHandlerFactory.getResourceHandler("/usr/bin/xdg-open").exists()) {
				//try with xdg-open from freedesktop.org which is installed with the xdg-utils package. 
				CommandLine cl = CommandLine.parse("/usr/bin/xdg-open " + file.toURI().toString());
				ProcessExecutor.runProcess(cl, new ProcessExecutor.EmptyProcessExecutorHandler(), ExecuteWatchdog.INFINITE_TIMEOUT);	
				return true;
			} else {
				try {
					Desktop.getDesktop().open(file);
					return true;
				} catch (IOException e) {
					Runtime.getRuntime().exec(new String[] { "rundll32.exe", "url.dll,FileProtocolHandler", file.getAbsolutePath() });
					return false;
				}
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
		} else {
			return true;
		}
	}


	private static boolean openWindowsFolder(File file) {
		final String windir = System.getenv("WINDIR");
		try {
			Desktop.getDesktop().open(new File(file.toString()));
			return true;
		} catch (Exception e) {
		}
		
		if(new File(windir + File.separator + "explorer.exe").exists()) {
			try {
				CommandLine cl = new CommandLine("C:\\Windows\\explorer.exe");
				cl.addArgument("/e");
				cl.addArgument("/select,\"" + file.toString() + "\"", false);
				ProcessExecutor.runProcessAsScript(cl, new ProcessExecutor.EmptyProcessExecutorHandler(), ExecuteWatchdog.INFINITE_TIMEOUT);	
				return true;
			} catch(Exception e2) {
				e2.printStackTrace(); //debug output
			}		
		}
		return false;
	}
	
	private static boolean openLinuxFolder(File file) {
		if(new File("/usr/bin/gnome-open").exists()) {
			try {
				CommandLine cl = CommandLine.parse("/usr/bin/gnome-open \"" + file.toString() + "\"");
				ProcessExecutor.runProcessAsScript(cl, new ProcessExecutor.EmptyProcessExecutorHandler(), ExecuteWatchdog.INFINITE_TIMEOUT);	
				return true;
			} catch(Exception e2) {
				e2.printStackTrace(); //debug output
			}			
		} else if(new File("/usr/bin/kde-open").exists()) {
			try {
				CommandLine cl = CommandLine.parse("/usr/bin/kde-open \"" + file.toString() + "\"");
				ProcessExecutor.runProcessAsScript(cl, new ProcessExecutor.EmptyProcessExecutorHandler(), ExecuteWatchdog.INFINITE_TIMEOUT);	
				return true;
			} catch(Exception e2) {
				e2.printStackTrace(); //debug output
			}
		}	
		return false;
	}	
}
