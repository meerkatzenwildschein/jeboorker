package org.rr.jeborker.gui.action;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.CommonUtils;

class OpenFileAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	String folder;

	OpenFileAction(String text) {
		this.folder = text;
		putValue(Action.NAME, Bundle.getString("OpenFileAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("file_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("file_22.gif")));		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final File file = new File(folder);

		try {
			if(CommonUtils.isLinux() && ResourceHandlerFactory.getResourceLoader("/usr/bin/xdg-open").exists()) {
				//try with xdg-open from freedesktop.org which is installed with the xdg-utils package. 
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							String[] s = new String[] {"/usr/bin/xdg-open", file.toURI().toString()};
							Process p = Runtime.getRuntime().exec(s);
							p.waitFor();						
						} catch (Exception e) {
							LoggerFactory.getLogger(this).log(Level.WARNING, "Open file '" + file + "' with default application has failed.", e);
						}
					}
				}).start();
			} else {
				Desktop.getDesktop().open(file);
			}
		} catch (IOException e1) {
			LoggerFactory.logWarning(this, "could not open file " + file, e1);
		}
	}
	

}
