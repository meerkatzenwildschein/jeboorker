package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.swing.SwingUtils;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

class NewFolderAction extends AbstractAction {

	private String location;

	NewFolderAction(String text) {
		this.location = text;
		String name = Bundle.getString("NewFolderAction.name");
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("folder_new_16.png"));
		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("folder_new_22.png"));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MainController controller = MainController.getController();
		String newFolderName = JOptionPane.showInputDialog(controller.getMainWindow(), Bundle.getString("NewFolderAction.text"),
				Bundle.getString("NewFolderAction.title"), JOptionPane.INFORMATION_MESSAGE);

		newFolderName = ResourceHandlerUtils.removeInvalidCharacters(newFolderName);
		if(StringUtils.isNotEmpty(newFolderName)) {
			IResourceHandler newFolderResource = ResourceHandlerFactory.getResourceHandler(new File(location, newFolderName));
			try {
				newFolderResource.mkdirs();
			} catch (IOException ex) {
				LoggerFactory.getLogger(this).log(Level.WARNING, "Failed to create folder " + newFolderResource, ex);
			}
		}
	}


}
