package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
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
			} else if(path!=null && path.length() > 0) {
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
	}
	
	private void doRefreshBasePath(String path, ActionEvent e, MainMonitor monitor) {
		IResourceHandler resourceLoader = ResourceHandlerFactory.getResourceLoader(path);
		
		ArrayList<EbookPropertyItem> itemsToRemove = RemoveBasePathAction.getItemsToRemove(path);
		RemoveBasePathAction.removeAllEbookPropertyItems(itemsToRemove);
		
		AddBasePathAction.readEbookFilesToDB(resourceLoader);
	}
	
	/**
	 * Refresh the given {@link EbookPropertyItem}. Does also persist it to the db!
	 * @param item The item to be refreshed
	 * @param resourceLoader The IResourceHandler for the given item. Can be <code>null</code>.
	 */
	static void refreshEbookPropertyItem(final EbookPropertyItem item, IResourceHandler resourceLoader) {
		if(resourceLoader == null) {
			resourceLoader = ResourceHandlerFactory.getResourceLoader(item.getFile());
		}
		
		//remove the entry from db and view.
		if(!resourceLoader.exists()) {
			RemoveBasePathAction.removeEbookPropertyItem(item);
			return;
		} else {
			EbookPropertyItemUtils.refreshEbookPropertyItem(item, resourceLoader, true);
			DefaultDBManager.getInstance().updateObject(item);
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					MainController.getController().refreshTableSelectedItem(true);
				}
			});
			
		}
	}

}