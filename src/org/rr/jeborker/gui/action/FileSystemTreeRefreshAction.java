package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class FileSystemTreeRefreshAction extends AbstractAction {

	private String path;
	
	FileSystemTreeRefreshAction(String path) {
		String name = Bundle.getString("FileSystemTreeRefreshAction.name");
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
		putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("refresh_16.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MainController controller = MainController.getController();
		if(path != null) {
			IResourceHandler resourceToRefresh = ResourceHandlerFactory.getResourceHandler(path);
			controller.getMainTreeHandler().refreshFileSystemTreeEntry(resourceToRefresh);
		} else {
			List<IResourceHandler> selectedTreeItems = controller.getMainTreeHandler().getSelectedTreeItems();
			for(IResourceHandler selectedTreeItem : selectedTreeItems) {
				controller.getMainTreeHandler().refreshFileSystemTreeEntry(selectedTreeItem);
			}
		}
	}

}
