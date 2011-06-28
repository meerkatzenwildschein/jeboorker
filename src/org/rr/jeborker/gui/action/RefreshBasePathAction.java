package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
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
import org.rr.jeborker.JEBorkerPreferences;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.event.QueueableAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;

/**
 * Add a folder action.
 */
class RefreshBasePathAction extends QueueableAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private String path;
	
	RefreshBasePathAction(String text) {
		super();
		if(text==null) {
			putValue(Action.NAME, Bundle.getString("RefreshBasePathAction.name"));
		} else {
			putValue(Action.NAME, text);
			if(ResourceHandlerFactory.hasResourceLoader(text)) {
				path = text;
			}
		}
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("refresh_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("refresh_22.gif")));
	}
	
	@Override
	public void doAction(ActionEvent event) {
		final MainController controller = MainController.getController();
		
		controller.getProgressMonitor().monitorProgressStart(Bundle.getString("AddBasePathAction.message"));
		String messageFinished = Bundle.getString("RefreshBasePathAction.finished");
		try {
			if(path!=null && path.length()>0) {
				doRefresh(path, event, controller.getProgressMonitor());
			} else {
				final List<String> basePath = JEBorkerPreferences.getBasePath();
				for (Iterator<String> iterator = basePath.iterator(); iterator.hasNext();) {
					final String basePathEntry = iterator.next();
					doRefresh(basePathEntry, event, controller.getProgressMonitor());
				}
			}
		} catch (Exception ex) {
			LoggerFactory.log(Level.WARNING, this, "Path " + path, ex);
		} finally {
			controller.refreshTable();
			controller.getProgressMonitor().monitorProgressStop(messageFinished);
		}
	}
	
	/**
	 * Performs a refresh to the given path. Deletes and reloads all given entries.
	 * @param path The path to be refreshed.
	 */
	private void doRefresh(String path, ActionEvent e, MainMonitor monitor) {
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final Iterable<EbookPropertyItem> items = defaultDBManager.getObject(EbookPropertyItem.class, "basePath", path);
		
		for (EbookPropertyItem item : items) {
			final IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceLoader(item.getFile());
			
			if(!resourceLoader.exists()) {
				//remove non existing ebook files
				monitor.setMessage(Bundle.getFormattedString("RefreshBasePathAction.remove", new Object[] {resourceLoader.getName()}));
				RemoveBasePathAction.removeEbookPropertyItem(item);
			} else {
				//refresh the existing ones
				monitor.setMessage(Bundle.getFormattedString("RefreshBasePathAction.refresh", new Object[] {resourceLoader.getName()}));
				refreshEbookPropertyItem(item, resourceLoader);
				defaultDBManager.updateObject(item);
			}
		}
		
		//scan all files in the base paths and add these ones which not already in the database.
		readEbookFilesToDB(ResourceHandlerFactory.getResourceLoader(path), monitor);
	}
	
	/**
	 * Refresh the given {@link EbookPropertyItem}. Does not persist it to the db!
	 * @param item The item to be refreshed
	 * @param resourceLoader The IResourceHandler for the given item. Can be <code>null</code>.
	 */
	static void refreshEbookPropertyItem(EbookPropertyItem item, IResourceHandler resourceLoader) {
		if(resourceLoader==null) {
			resourceLoader = ResourceHandlerFactory.getResourceLoader(item.getFile());
		}
		
		//remove the entry from db and view.
		if(!resourceLoader.exists()) {
			RemoveBasePathAction.removeEbookPropertyItem(item);
			return;
		}
		
		EbookPropertyItemUtils.refreshEbookPropertyItem(item, resourceLoader, true);
	}
	
	/**
	 * Read all ebook files recursive and stores them directly to the database if they are not already be stored.
	 * @param baseFolder The folder where the ebook search should be started.
	 */
	private void readEbookFilesToDB(final IResourceHandler baseFolder, final MainMonitor monitor) {
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		ResourceHandlerUtils.readAllFilesFromBasePath(baseFolder, new ResourceNameFilter() {
			
			@Override
			public boolean accept(IResourceHandler loader) {
				try {
					if(loader.isFileResource() && loader.isEbookFormat()) {			
						final List<EbookPropertyItem> alreadyStoredOne = defaultDBManager.getObject(EbookPropertyItem.class, "file", loader.getResourceString());
						if(alreadyStoredOne==null || alreadyStoredOne.isEmpty()) {
							EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(loader, baseFolder);
							AddBasePathAction.addEbookPropertyItem(item);
							monitor.setMessage(Bundle.getFormattedString("AddBasePathAction.add", new Object[] {loader.getName()}));
						}
					}
				} catch (Throwable e) {
					LoggerFactory.log(Level.WARNING, RefreshBasePathAction.class, "Could not add file " + loader.getResourceString(), e);
				}
				return false;
			}
		});
	}	
}
