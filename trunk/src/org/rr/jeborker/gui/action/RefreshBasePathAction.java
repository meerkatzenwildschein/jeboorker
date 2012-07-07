package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
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
		putValue(Action.NAME, text);
		if(ResourceHandlerFactory.hasResourceLoader(text)) {
			path = text;
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
			if(path!=null && path.length() > 0) {
				if(!ResourceHandlerFactory.getResourceLoader(path).isDirectoryResource()) {
					messageFinished = "The folder " + path + " did not exists.";
				} else {			
					doRefreshBasePath(path, event, controller.getProgressMonitor());
				}
			}
		} catch (Exception ex) {
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
	static void refreshEbookPropertyItem(EbookPropertyItem item, IResourceHandler resourceLoader) {
		if(resourceLoader==null) {
			resourceLoader = ResourceHandlerFactory.getResourceLoader(item.getFile());
		}
		
		//remove the entry from db and view.
		if(!resourceLoader.exists()) {
			RemoveBasePathAction.removeEbookPropertyItem(item);
			return;
		} else {
			EbookPropertyItemUtils.refreshEbookPropertyItem(item, resourceLoader, true);
			DefaultDBManager.getInstance().updateObject(item);
		}
	}

}
