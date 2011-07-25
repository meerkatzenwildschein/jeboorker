package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.mufs.ResourceHandlerUtils;
import org.rr.commons.mufs.ResourceNameFilter;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.QueueableAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuController;

/**
 * Add a folder action.
 */
class AddBasePathAction extends QueueableAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private String path;
	
	AddBasePathAction(String text) {
		if(text==null) {
			putValue(Action.NAME, Bundle.getString("AddBasePathAction.name"));
		} else {
			putValue(Action.NAME, text);
			if(ResourceHandlerFactory.hasResourceLoader(text)) {
				path = text;
			}
		}
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("add_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("add_22.gif")));
	}
	
	@Override
	public void doAction(ActionEvent e) {
		final MainController controller = MainController.getController();
		final IResourceHandler selectedDirectory;
		String path = this.path;
		
		if(path!=null && path.length()>0) {
			selectedDirectory = ResourceHandlerFactory.getResourceLoader(path);
		} else {
			final File directorySelection = controller.getDirectorySelection();
			selectedDirectory = ResourceHandlerFactory.getResourceLoader(directorySelection);
			path = selectedDirectory.toString();
		}
		
		if(selectedDirectory!=null) {
			controller.getProgressMonitor().monitorProgressStart(Bundle.getString("AddBasePathAction.message"));
			String messageFinished = Bundle.getString("AddBasePathAction.finished");
			try {
				//only attach the new path if it is not a part of an already configured path.
				if(!isAlreadyExistingBasePath(JeboorkerPreferences.getBasePath(), path)) {
					JeboorkerPreferences.addBasePath(path); //add path to preferences
					MainMenuController.getController().addBasePathMenuEntry(path); //add path to ui
					readEbookFilesToDB(selectedDirectory);
				} else {
					messageFinished = Bundle.getFormattedString("AddBasePathAction.duplicatePathEntry", new Object[] {path});
				}
			} catch(Throwable t) {
				LoggerFactory.log(Level.WARNING, this, "Adding " + path + " has failed", t);
			} 
			controller.getProgressMonitor().monitorProgressStop(messageFinished);
			
			controller.refreshTable(false);
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
		for (Iterator<String> iterator = existing.iterator(); iterator.hasNext();) {
			String existingEntry =  iterator.next();
			existingEntry = existingEntry.replace('\\', '/');
			
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
	private void readEbookFilesToDB(final IResourceHandler baseFolder) {
		ResourceHandlerUtils.readAllFilesFromBasePath(baseFolder, new ResourceNameFilter() {
			
			@Override
			public boolean accept(IResourceHandler loader) {
				if(loader.isFileResource() && loader.isEbookFormat()) {
					final EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(loader, baseFolder);
					addEbookPropertyItem(item);
				}
				return false;
			}
		});
	}
	
	

	
	/**
	 * Adds the given item to the database and to the ui.
	 * @param item The item to be added.
	 */
	static void addEbookPropertyItem(final EbookPropertyItem item) {
		MainController.getController().getProgressMonitor().setMessage(Bundle.getFormattedString("AddBasePathAction.add", item.getFileName()));
		DefaultDBManager.getInstance().storeObject(item);
		MainController.getController().addEbookPropertyItem(item);
	}

}
