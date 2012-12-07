package org.rr.jeborker.gui.action;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.ReflectionUtils;

class OpenFolderAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	String folder;

	OpenFolderAction(String text) {
		this.folder = text;
		putValue(Action.NAME, Bundle.getString("OpenFolderAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("folder_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("folder_22.gif")));		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		File file = new File(folder);
		if(!file.isDirectory()) {
			file = file.getParentFile();
		}
		try {
			Desktop.getDesktop().open(file);
		} catch (Exception e1) {
			boolean success = false;
			if(ReflectionUtils.getOS() == ReflectionUtils.OS_LINUX) {
				success = openLinuxFolder(file);
			} else if(ReflectionUtils.getOS() == ReflectionUtils.OS_WINDOWS) {
				success = openWindowsFolder(file);
			}
			
			if(!success) {
				LoggerFactory.logWarning(this, "could not open folder " + file, e1);
			}
		}
	}
	
	private boolean openWindowsFolder(File file) {
		if(new File("C:\\Windows\\explorer.exe").exists()) {
			try {
				//C:\Windows\explorer.exe /n,/e,/root,C:\Users\guru
				Runtime.getRuntime().exec(new String[] {"C:\\Windows\\explorer.exe", "/n", "/e", "\"" + file.toString() + "\""});
				return true;
			} catch(Exception e2) {
				e2.printStackTrace(); //debug output
			}		
		}
		return false;
	}
	
	private boolean openLinuxFolder(File file) {
		if(new File("/usr/bin/nemo").exists()) {
			try {
				Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "/usr/bin/nautilus \"" + file.toString() + "\""});
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
