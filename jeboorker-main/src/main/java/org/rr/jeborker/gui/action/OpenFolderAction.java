package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.swing.DesktopUtils;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class OpenFolderAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	String folder;

	OpenFolderAction(String text) {
		this.folder = text;
		String name = Bundle.getString("OpenFolderAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("folder_open_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("folder_open_22.png"));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		File file = new File(folder);
		boolean success = DesktopUtils.openFolder(file);
		if(!success) {
			LoggerFactory.log(Level.INFO, this, "could not open folder " + file);
		}
	}
	
	
}
