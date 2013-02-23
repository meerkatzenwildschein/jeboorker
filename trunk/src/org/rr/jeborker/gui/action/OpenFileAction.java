package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.common.swing.SwingUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.DesktopUtils;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class OpenFileAction extends AbstractAction {

	private static final long serialVersionUID = -6464113132395695332L;
	
	String folder;

	OpenFileAction(String text) {
		this.folder = text;
		String name = Bundle.getString("OpenFileAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("file_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("file_22.png"));		
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
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
