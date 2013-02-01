package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.DesktopUtils;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class OpenFolderAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	String folder;

	OpenFolderAction(String text) {
		this.folder = text;
		String name = Bundle.getString("OpenFolderAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, new ImageIcon(ImageResourceBundle.getResource("folder_16.png")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(ImageResourceBundle.getResource("folder_22.png")));
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
