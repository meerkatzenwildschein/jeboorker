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

class OpenFileAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	private String file;

	OpenFileAction(String text) {
		this.file = text;
		String name = Bundle.getString("OpenFileAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("file_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("file_22.png"));		
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final File fileToOpen = new File(file);
		boolean sucess = DesktopUtils.openFile(fileToOpen);
		if(!sucess) {
			LoggerFactory.getLogger().log(Level.INFO, "Open file in associated application has failed for " + fileToOpen);
		}
	}
	

}
