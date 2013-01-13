package org.rr.jeborker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.ActionUtils;
import org.rr.jeborker.gui.action.ApplicationAction;

public class FileRefreshBackgroundThread extends Thread {

	private static FileRefreshBackgroundThread thread;

	private static final List<EbookPropertyItem> items = Collections.synchronizedList(new ArrayList<EbookPropertyItem>());

	public FileRefreshBackgroundThread(Worker worker) {
		super(worker);
	}

	public static synchronized FileRefreshBackgroundThread getInstance() {
		if (thread == null) {
			thread = new FileRefreshBackgroundThread(new Worker());
			thread.start();
		}
		return thread;
	}

	public void addEbook(EbookPropertyItem item) {
		items.add(item);
	}

	private static class Worker implements Runnable {

		@Override
		public void run() {
			while (true) {
				while (!items.isEmpty()) {
					EbookPropertyItem ebookPropertyItem = items.remove(0);
					this.processItem(ebookPropertyItem);
				}
				
				// wait a moment before restart
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		/**
		 * Handle these items which are added with the {@link FileRefreshBackgroundThread#addEbook(EbookPropertyItem)} method.
		 */
		private void processItem(EbookPropertyItem ebookPropertyItem) {
			IResourceHandler resourceHandler = ebookPropertyItem.getResourceHandler();
			if (!resourceHandler.exists()) {
				// ebook file has been deleted
				ActionUtils.removeEbookPropertyItem(ebookPropertyItem);
				LoggerFactory.getLogger(this).log(Level.INFO, "Removed deleted entry " + ebookPropertyItem.getResourceHandler().getName());
			} else if (isRefreshNeeded(ebookPropertyItem, resourceHandler)) {
				// ebook file has been changed until the last time.
				List<EbookPropertyItem> reloadedItem = EbookPropertyItemUtils.getEbookPropertyItemByResource(ebookPropertyItem.getResourceHandler());
				if(!reloadedItem.isEmpty() && isRefreshNeeded(reloadedItem.get(0), resourceHandler)) {
					ApplicationAction refreshAction = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_ENTRY_ACTION, resourceHandler.toString());
					refreshAction.invokeAction();
					LoggerFactory.getLogger(this).log(Level.INFO, "Changed entry " + ebookPropertyItem.getResourceHandler().getName() + " refreshed.");
				}
			}
		}
		
		private boolean isRefreshNeeded(EbookPropertyItem ebookPropertyItem, IResourceHandler resourceHandler) {
			if(ebookPropertyItem.getTimestamp() > 0l && ebookPropertyItem.getTimestamp() < resourceHandler.getModifiedAt().getTime()) {
				return true;
			}
			return false;
		}
	}
}
