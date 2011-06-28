package org.rr.jeborker.gui.action;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.event.QueueableAction;

public class OpenFolderAction extends QueueableAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	String folder;

	OpenFolderAction(String text) {
		this.folder = text;
		putValue(Action.NAME, Bundle.getString("OpenFolderAction.name"));
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("folder_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("folder_22.gif")));		
	}
	
	@Override
	public void doAction(ActionEvent e) {
		File file = new File(folder);
		if(!file.isDirectory()) {
			file = file.getParentFile();
		}
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e1) {
			LoggerFactory.logWarning(this, "could not open folder " + file, e1);
		}
	}
	

}
