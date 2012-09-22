package org.rr.jeborker.gui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.utils.StringUtils;
import org.rr.jeborker.JeboorkerPreferences;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.MainMenuController;
import org.rr.jeborker.gui.MainMonitor;

class RemoveBasePathAction extends AbstractAction {

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
	public void actionPerformed(ActionEvent e) {
		final String path = (String) getValue(Action.NAME);
		
		removeBasePathEntries(path, true);
		
		JeboorkerPreferences.removeBasePath(path);
		MainMenuController.getController().removeBasePathMenuEntry(path);
	}

	static void removeBasePathEntries(final String path, boolean refreshTable) {
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		
		try {
			progressMonitor.monitorProgressStart(Bundle.getString("RemoveBasePathAction.message"));
			try {
				ArrayList<EbookPropertyItem> itemsToRemove = getItemsToRemove(path);
				removeAllEbookPropertyItems(itemsToRemove);
			} catch(Exception ex) {
				LoggerFactory.logWarning(RemoveBasePathAction.class, "Error while removing ebooks from catalog", ex);
			}
		} finally {
			progressMonitor.monitorProgressStop();	
			if(refreshTable) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						MainController.getController().refreshTable(false);
					}
				});
			}
		}
	}
	
	/**
	 * Get all items from the database with a fitting base path.
	 * @param basePath The base path string
	 * @return A list over all items with a fitting base path. Never returns null.
	 */
	static ArrayList<EbookPropertyItem> getItemsToRemove(final String basePath) {
		final String normalizedPath = StringUtils.replace(basePath, File.separator, "");
		final DefaultDBManager defaultDBManager = DefaultDBManager.getInstance();
		final ArrayList<EbookPropertyItem> toRemove = new ArrayList<EbookPropertyItem>();
		final Iterable<EbookPropertyItem> items = defaultDBManager.getItems(EbookPropertyItem.class);
		for (EbookPropertyItem item : items) {
			if(StringUtils.replace(item.getBasePath(), File.separator, "").equals(normalizedPath)) {
				toRemove.add(item);
			}
		}
		return toRemove;
	}
	
	/**
	 * Deletes the given items from the database and the view.
	 * @param items The items to be deleted.
	 */
	static void removeAllEbookPropertyItems(final List<EbookPropertyItem> items) {
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		final MainController controller = MainController.getController();
		
		progressMonitor.setMessage(Bundle.getString("RemoveBasePathAction.deletingMany"));
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				controller.removeEbookPropertyItems(items);
			}
		});		
	}
	
	/**
	 * Deletes the given item from the database and the view.
	 * @param item The item to be deleted.
	 */
	static void removeEbookPropertyItem(final EbookPropertyItem item) {
		final MainMonitor progressMonitor = MainController.getController().getProgressMonitor();
		final MainController controller = MainController.getController();
		
		progressMonitor.setMessage(Bundle.getFormattedString("RemoveBasePathAction.deleting", item.getFileName()));
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				boolean removed = controller.removeEbookPropertyItem(item);
				if(!removed) {
					DefaultDBManager.getInstance().deleteObject(item);
				}
			}
		});
	}
	
}
