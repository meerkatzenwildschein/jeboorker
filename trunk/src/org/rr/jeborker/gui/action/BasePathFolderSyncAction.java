package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class BasePathFolderSyncAction extends AbstractAction {

	private String path;
	
	BasePathFolderSyncAction(String path) {
		this.path = path;
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("sync_16.png"));
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		MainController controller = MainController.getController();
		if(path != null) {
			IResourceHandler resource = ResourceHandlerFactory.getResourceHandler(path);
			String basePathFor = JeboorkerPreferences.getBasePathFor(resource);
			if(basePathFor != null) {
				controller.addExpandedTreeItems(Arrays.asList(resource));
			}
		}
	}

}
