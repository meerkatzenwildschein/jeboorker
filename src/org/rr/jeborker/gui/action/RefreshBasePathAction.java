package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
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
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;

/**
 * Add a folder action.
 */
class RefreshBasePathAction extends AbstractAction {

	private static final long serialVersionUID = -9066575818229620987L;
	
	private static final String REFRESH_ALL = "refreshAll";
	
	private String path;
	
	RefreshBasePathAction(String text) {
		super();
		putValue(Action.NAME, text);
		if(REFRESH_ALL.equals(text)) {
			path = text;
			putValue(Action.NAME, Bundle.getString("RefreshBasePathAction.refreshAll.name"));
		} else if(ResourceHandlerFactory.hasResourceLoader(text)) {
			path = text;
		}
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("refresh_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("refresh_22.gif")));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final MainController controller = MainController.getController();
		
		controller.getProgressMonitor().monitorProgressStart(Bundle.getString("AddBasePathAction.message"));
		String messageFinished = Bundle.getString("RefreshBasePathAction.finished");
		try {
			if(REFRESH_ALL.equals(path)) {
				final List<String> basePaths = JeboorkerPreferences.getBasePath();
				for (String basePath : basePaths) {
					if(ResourceHandlerFactory.getResourceLoader(basePath).isDirectoryResource()) {
						doRefreshBasePath(basePath, e, controller.getProgressMonitor());
					}
				}				
			} else if(path != null && path.length() > 0) {
				if(!ResourceHandlerFactory.getResourceLoader(path).isDirectoryResource()) {
					messageFinished = "The folder " + path + " did not exists.";
				} else {			
					doRefreshBasePath(path, e, controller.getProgressMonitor());
				}
			}
		} catch (Throwable ex) {
			LoggerFactory.log(Level.WARNING, this, "Path " + path, ex);
		} finally {
			controller.refreshTable(true);
			controller.getProgressMonitor().monitorProgressStop(messageFinished);
		}
		System.gc();
	}
	
	private void doRefreshBasePath(String path, ActionEvent e, MainMonitor monitor) {
		IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceLoader(path);
		removeDeletedFiles(resourceLoader);
		refreshEbookFiles(resourceLoader);
		MainController.getController().refreshTable(true);
	}
	
	/**
	 * Removes all deleted files from the database.
	 * @param basePath The folder to be processed.
	 */
	static void removeDeletedFiles(final IResourceHandler basePath) {
		final DefaultDBManager db = DefaultDBManager.getInstance();
		final ArrayList<EbookPropertyItem> itemsToTest = RemoveBasePathAction.getItemsByBasePath(basePath.toString());
		for(EbookPropertyItem item : itemsToTest) {
			final IResourceHandler itemResourceHandler = item.getResourceHandler();
			if(!itemResourceHandler.exists()) {
				db.deleteObject(item);
			}
		}
	}
	
	/**
	 * Read all ebook files recursive and stores them directly to the database.
	 * @param basePath The folder where the ebook search should be started.
	 */
	static void refreshEbookFiles(final IResourceHandler basePath) {
		final DefaultDBManager db = DefaultDBManager.getInstance();
		ResourceHandlerUtils.readAllFilesFromBasePath(basePath, new ResourceNameFilter() {
			
			@Override
			public boolean accept(IResourceHandler resourceLoader) {
				if(resourceLoader.isFileResource() && ActionUtils.isSupportedEbookFormat(resourceLoader)) {
					try {
						List<EbookPropertyItem> ebookPropertyItems = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceLoader);
						if(!ebookPropertyItems.isEmpty()) {
							for(EbookPropertyItem item : ebookPropertyItems) {
								long fileTimeStamp = resourceLoader.getModifiedAt().getTime();
								if(item.getTimestamp() == 0 || item.getTimestamp() != fileTimeStamp) {
									//file has changed
									EbookPropertyItemUtils.refreshEbookPropertyItem(item, resourceLoader, true);
									db.updateObject(item);
								}
							}
						} else {
							//new ebook
							final EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(resourceLoader, basePath);
							db.storeObject(item);
						}
						
						return true;
					} catch(Throwable e) {
						LoggerFactory.getLogger(this).log(Level.SEVERE, "Failed adding resource " + resourceLoader, e);
					}
				}
				return false;
			}
		});
System.out.println("finish");		
	}	
	
}
