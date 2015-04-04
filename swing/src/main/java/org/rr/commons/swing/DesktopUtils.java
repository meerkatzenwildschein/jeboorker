package org.rr.commons.swing;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.ExecuteWatchdog;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.CommonUtils;
import org.rr.commons.utils.ProcessExecutor;
import org.rr.commons.utils.ProcessExecutorHandler;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.StringUtils;

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

	public static Object showSelectionDialog(Component parent, Object[] possibilities, Object defaultValue) {
		Icon icon = null;
		return JOptionPane.showInputDialog(parent, "Complete the sentence:\n" + "\"Green eggs and...\"", "Customized Dialog",
				JOptionPane.PLAIN_MESSAGE, icon, possibilities, "ham");
	}

	public static String showInputDialog(Component parent, String message, String title, String defaultValue) {
		File zenityBinary = getZenityBinary();
		if (zenityBinary != null) {
			CommandLine cl = CommandLine.parse(DesktopUtils.getZenityBinary().getAbsolutePath());
			cl.addArgument("--entry", false);
			cl.addArgument("--title=" + title, false);
			cl.addArgument("--text=" + message, false);
			if(StringUtils.isNotEmpty(defaultValue)) {
				cl.addArgument("--entry-text");
				cl.addArgument(defaultValue);
			}
			try {
				final StringBuilder fileName = new StringBuilder();
				Future<Long> p = ProcessExecutor.runProcess(cl, new ProcessExecutorHandler() {

					@Override
					public void onStandardOutput(String msg) {
						fileName.setLength(0);
						fileName.append(msg);
					}

					@Override
					public void onStandardError(String msg) {
						LoggerFactory.getLogger().log(Level.WARNING, msg);
					}
				}, ExecuteWatchdog.INFINITE_TIMEOUT);

				p.get(); // wait
				return fileName.toString();
			} catch (Exception e) {
				LoggerFactory.getLogger().log(Level.WARNING, "Failed to execute zenity", e);
				return null;
			}
		} else {
			return JOptionPane.showInputDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Get the zenity binary if zenity is installed on the operating system.
	 * @return The location of the zenity binary or <code>null</code> if no zenity can be found.
	 */
	public static File getZenityBinary() {
		if(CommonUtils.isLinux()) {
			File zenityBin = new File("/usr/bin/zenity");
			if(zenityBin.exists()) {
				return zenityBin;
			}
		}
		return null;
	}

    /**
     * Open a URL in the default web browser.
     *
     * @param a URL to open in a web browser.
     * @return true if a browser has been launched.
     */
    public static boolean launchBrowser(URL url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(url.toURI());
                return true;
            } catch (Exception e) {
            	e.printStackTrace(); //debug output
            }
        }
        return false;
    }
}
