package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class FileSystemImportAction extends AbstractAction {

	private String targetFolder;
	
	FileSystemImportAction(String targetFolder) {
		this.targetFolder = targetFolder;
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("import_16.png"));
		putValue(Action.NAME, targetFolder);
		putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE); //No threading
		putValue(SHORT_DESCRIPTION, Bundle.getString("FileSystemCollapseAllAction.tooltip")); //tooltip
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		final MainController controller = MainController.getController();
		final List<IResourceHandler> selectedTreeItems = controller.getMainTreeController().getSelectedTreeItems();
		final IResourceHandler targetRecourceDirectory = ResourceHandlerFactory.getResourceHandler(targetFolder);
		final String targetBasePath = JeboorkerPreferences.getBasePathFor(targetRecourceDirectory);
		
		try {
			ActionUtils.importEbookResources(Integer.MIN_VALUE, targetBasePath, targetRecourceDirectory, selectedTreeItems);
		} catch(Exception ex) {
			LoggerFactory.log(Level.WARNING, this, "Failed to import file to " + targetFolder, ex);
		}
	}

}