package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.DesktopUtils;

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
		boolean sucess = DesktopUtils.openFile(file);
		if(!sucess) {
			LoggerFactory.getLogger().log(Level.INFO, "Open file in associated application has failed for " + file);
		}
	}
	

}
