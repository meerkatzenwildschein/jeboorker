package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

public class FileSystemImportAction extends AbstractAction {

	private String targetFolder;
	
	FileSystemImportAction(String targetFolder) {
		this.targetFolder = targetFolder;
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("import_16.png"));
		putValue(Action.NAME, targetFolder);
		putValue(SHORT_DESCRIPTION, Bundle.getString("FileSystemImportAction.tooltip")); //tooltip
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		final MainController controller = MainController.getController();
		final List<IResourceHandler> selectedTreeItems = controller.getMainTreeHandler().getSelectedTreeItems();
		final IResourceHandler targetRecourceDirectory = ResourceHandlerFactory.getResourceHandler(targetFolder);
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		final String targetBasePath = preferenceStore.getBasePathFor(targetRecourceDirectory);

		MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		try {
			progressMonitor.blockMainFrame(true).setMessage("Importing files");
			
			boolean delete = preferenceStore.getEntryAsBoolean(PreferenceStoreFactory.PREFERENCE_KEYS.DELETE_EBOOK_AFTER_IMPORT).booleanValue();
			List<IResourceHandler> importEbookResources = ActionUtils.importEbookResources(selectedTreeItems, targetBasePath, delete);
			if(!importEbookResources.isEmpty()) {
				ActionUtils.applyFileNameFilter(selectedTreeItems, true);
			}
		} catch(Exception ex) {
			LoggerFactory.log(Level.WARNING, this, "Failed to import file to " + targetFolder, ex);
		} finally {
			progressMonitor.blockMainFrame(false);
		}
	}

}
