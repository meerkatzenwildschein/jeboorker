package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.mufs.ResourceNameFilter;
import org.rr.commons.swing.SwingUtils;
import org.rr.jeborker.app.preferences.APreferenceStore;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.resources.ImageResourceBundle;

/**
 * Add a folder action.
 */
class AddBasePathAction extends AbstractAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private String path;
	
	AddBasePathAction(String text) {
		String name = Bundle.getString("AddBasePathAction.name");
		if(text==null) {
			putValue(Action.NAME, SwingUtils.removeMnemonicMarker(name));
		} else {
			putValue(Action.NAME, text);
			if(ResourceHandlerFactory.hasResourceHandler(text)) {
				path = text;
			}
		}
		putValue(Action.SMALL_ICON, ImageResourceBundle.getResourceAsImageIcon("add_16.png"));
//		putValue(Action.LARGE_ICON_KEY, ImageResourceBundle.getResourceAsImageIcon("add_22.png"));
		putValue(MNEMONIC_KEY, SwingUtils.getMnemonicKeyCode(name));
		putValue(SHORT_DESCRIPTION, Bundle.getString("AddBasePathAction.tooltip")); //tooltip
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		String path = this.path;
		
		controller.getEbookTableHandler().clearSelection();
		if(this.path != null && this.path.length() > 0) {
			IResourceHandler selectedDirectory = ResourceHandlerFactory.getResourceHandler(path);
			addBasePath(controller, selectedDirectory, path);
		} else {
			final List<File> directorySelections = controller.getDirectorySelection();
			for (File directorySelection : directorySelections) {
				IResourceHandler selectedDirectory = ResourceHandlerFactory.getResourceHandler(directorySelection);
				addBasePath(controller, selectedDirectory, selectedDirectory.toString());
			}
		}
		System.gc();
	}

	private void addBasePath(final MainController controller, final IResourceHandler selectedDirectory, String path) {
		final APreferenceStore preferenceStore = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE);
		
		if(selectedDirectory != null) {
			controller.changeToDatabaseModel();
			
			controller.getProgressMonitor().monitorProgressStart(Bundle.getString("AddBasePathAction.message"));
			String messageFinished = Bundle.getString("AddBasePathAction.finished");
			try {
				//only attach the new path if it is not a part of an already configured path.
				if(!isAlreadyExistingBasePath(preferenceStore.getBasePath(), path)) {
					preferenceStore.addBasePath(path); //add path to preferences
					int count = readEbookFilesToDB(selectedDirectory);
					messageFinished = Bundle.getFormattedString("AddBasePathAction.finishedCount", String.valueOf(count));
				} else {
					messageFinished = Bundle.getFormattedString("AddBasePathAction.duplicatePathEntry", path);
				}
			} catch(Throwable t) {
				LoggerFactory.log(Level.WARNING, this, "Adding " + path + " has failed", t);
			}
			controller.getProgressMonitor().monitorProgressStop(messageFinished);
			
			controller.getEbookTableHandler().refreshTable();
			controller.getMainTreeHandler().refreshBasePathTree();
		}
	}
	
	/**
	 * Tests if the first path is part of the second or the second path is part of the first
	 * @param existing A list of path to be compared
	 * @param path The path to be compared with the list
	 * @return <code>true</code> if the path match to another or <code>false</code> otherwise.
	 */
	private boolean isAlreadyExistingBasePath(List<String> existing, String path) {
		path = path.replace('\\', '/');
		if(!path.endsWith("/")) {
			path += "/";
		}
		for (Iterator<String> iterator = existing.iterator(); iterator.hasNext();) {
			String existingEntry =  iterator.next();
			existingEntry = existingEntry.replace('\\', '/');
			if(!existingEntry.endsWith("/")) {
				existingEntry += "/";
			}
			
			if(path.startsWith(existingEntry) || existingEntry.startsWith(path)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Read all ebook files recursive and stores them directly to the database.
	 * @param baseFolder The folder where the ebook search should be started.
	 */
	static int readEbookFilesToDB(final IResourceHandler baseFolder) {
		final HashSet<String> path = new HashSet<>();
		int count = ResourceHandlerUtils.readAllFilesFromBasePath(baseFolder, new ResourceNameFilter() {
			
			@Override
			public boolean accept(IResourceHandler resource) {
				if(resource.isFileResource() && ActionUtils.isSupportedEbookFormat(resource, true)) {
					try {
						final EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(resource, baseFolder);
						ActionUtils.addAndStoreEbookPropertyItem(item);
						path.add(resource.getParentResource().toString());
						return true;
					} catch(Throwable e) {
						LoggerFactory.getLogger(this).log(Level.SEVERE, "Failed adding resource " + resource, e);
					}
				}
				return false;
			}
		});
		EbookPropertyItemUtils.storePathElements(path);
		return count;
	}

}
