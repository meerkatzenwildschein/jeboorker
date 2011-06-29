package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.rr.commons.log.LoggerFactory;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.event.QueueableAction;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMonitor;
import org.rr.jeborker.gui.MainMenuController;

public class RemoveBasePathAction extends QueueableAction {

	private static final long serialVersionUID = 6607018752541779846L;

	RemoveBasePathAction(String text) {
		if(text==null) {
			putValue(Action.NAME, Bundle.getString("ReadEbookFolderAction.name"));
		} else {
			putValue(Action.NAME, text);
		}
		putValue(Action.SMALL_ICON, new ImageIcon(Bundle.getResource("remove_16.gif")));
		putValue(Action.LARGE_ICON_KEY, new ImageIcon(Bundle.getResource("remove_22.gif")));		
	}
	
	@Override
	public void doAction(final ActionEvent e) {
		final String path = (String) getValue(Action.NAME);
		
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final Iterable<EbookPropertyItem> items = defaultDBManager.getItems(EbookPropertyItem.class);
		
		progressMonitor.monitorProgressStart(Bundle.getString("RemoveBasePathAction.message"));
		try {
			for (Iterator<EbookPropertyItem> iterator = items.iterator(); iterator.hasNext();) {
				final EbookPropertyItem item =  iterator.next();
				if(StringUtils.replace(item.getBasePath(), File.separator, "").equals(StringUtils.replace(path, File.separator, ""))) {
					removeEbookPropertyItem(item);
				}
			}
		} catch(Exception ex) {
			LoggerFactory.logWarning(this, "Error while removing ebooks from catalog", ex);
		} finally {
			progressMonitor.monitorProgressStop();
		}
		JeboorkerPreferences.removeBasePath(path);
		MainMenuController.getController().removeBasePathMenuEntry(path);
	}
	
	/**
	 * Deletes the given item from the database and the view.
	 * @param item The item to be deleted.
	 */
	static void removeEbookPropertyItem(final EbookPropertyItem item) {
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		final MainController controller = MainController.getController();
		
		progressMonitor.setMessage(Bundle.getFormattedString("RemoveBasePathAction.deleting", item.getFileName()));
//		defaultDBManager.deleteObject(ebookPropertyItem);
		boolean removed = controller.removeEbookPropertyItem(item);
		if(!removed) {
			DefaultDBManager.getInstance().deleteObject(item);
		}
	}
	
}
