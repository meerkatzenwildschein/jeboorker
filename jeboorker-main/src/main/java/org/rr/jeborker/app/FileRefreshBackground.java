package org.rr.jeborker.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.DateUtils;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.commons.utils.UtilConstants;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.action.ActionFactory;
import org.rr.jeborker.gui.action.ActionUtils;
import org.rr.jeborker.gui.action.ApplicationAction;

public class FileRefreshBackground {

	private static FileRefreshBackground singleton;

	private static final Set<EbookPropertyItem> items = Collections.synchronizedSet(new HashSet<EbookPropertyItem>());

	private static int isDisabled = 0;
	
	private FileRefreshBackground() {
	}
	
	public static synchronized FileRefreshBackground getInstance() {
		if (singleton == null) {
			singleton = new FileRefreshBackground();
			Jeboorker.APPLICATION_THREAD_POOL.submit(new Worker());
		}
		return singleton;
	}

	public void addEbook(EbookPropertyItem item) {
		if(isDisabled == 0) {
			synchronized (items) {
				items.add(item);
			}
		}
	}
	
	public void addEbooks(List<EbookPropertyItem> changedResources) {
		if(isDisabled == 0 && !changedResources.isEmpty()) {
			synchronized (items) {
				items.addAll(changedResources);
			}
		}
	}
	
	/**
	 * Disabled the {@link FileRefreshBackground}. The {@link #addEbook(EbookPropertyItem)} method
	 * did no longer add books if the {@link FileRefreshBackground} is set to disabled.
	 * @param disabled <code>true</code> for disabling the {@link FileRefreshBackground} and <code>false</code>
	 *  to enable it.
	 */
	public static void setDisabled(boolean disabled) {
		if(disabled) {
			isDisabled ++;
		} else {
			isDisabled --;
		}
	}
	
	/**
	 * Starts the given {@link Runnable} and takes sure that the background refresh is disabled while running.
	 * @param run The {@link Runnable} to be started while no file refresh will be detected.
	 */
	public static void runWithDisabledRefresh(Runnable run) {
		setDisabled(true);
		try {
			run.run();
		} finally {
			setDisabled(false);
		}
	}
	
	/**
	 * Tells if the background refresh is currently disabled or not.
	 */
	public static boolean isDisabled() {
		return isDisabled != 0;
	}

	private static class Worker implements Runnable {

		@Override
		public void run() {
			while (true) {
				while (!items.isEmpty() && isDisabled == 0) {
					EbookPropertyItem ebookPropertyItem = null;
					try {
						synchronized (items) {
							List<EbookPropertyItem> processedItems = new ArrayList<>();
							Iterator<EbookPropertyItem> itemsIter = items.iterator();
							while(itemsIter.hasNext()) {
								ebookPropertyItem = itemsIter.next();
								if(ebookPropertyItem != null) {
									this.processItem(ebookPropertyItem);
									processedItems.add(ebookPropertyItem);
								}
							}
							items.removeAll(processedItems);
							items.remove(null);
						}
					} catch(Exception e) {
						LoggerFactory.log(Level.WARNING, this, "Failed to handle " + ebookPropertyItem + " in background process", e);
					}
				}
				
				// wait a moment before restart
				ReflectionUtils.sleepSilent(1000);
			}
		}

		/**
		 * Handle these items which are added with the {@link FileRefreshBackground#addEbook(EbookPropertyItem)} method.
		 */
		private void processItem(EbookPropertyItem ebookPropertyItem) {
			IResourceHandler resourceHandler = ebookPropertyItem.getResourceHandler();
			deleteOldTempFile(resourceHandler);
			
			if (!resourceHandler.exists()) {
				LoggerFactory.getLogger(this).log(Level.INFO, resourceHandler + " no longer exists and will be removed from catalog.");
				handleDeletedItem(ebookPropertyItem);
				return;
			}
			
			if(!isManagedItem(resourceHandler)) {
				LoggerFactory.getLogger(this).log(Level.INFO, resourceHandler + " is not a managed resource and will not be refreshed.");
				return;
			}
			
			if (isRefreshNeeded(ebookPropertyItem, resourceHandler)) {
				handleRefreshItem(ebookPropertyItem, resourceHandler);
			}
		}

		/**
		 * Handle an ebook file that has been changed until the last time.
		 * @param ebookPropertyItem The ebook file to process.
		 * @param resourceHandler
		 */
		private void handleRefreshItem(EbookPropertyItem ebookPropertyItem, IResourceHandler resourceHandler) {
			EbookPropertyItem reloadedItem = EbookPropertyItemUtils.reloadEbookPropertyItem(ebookPropertyItem);
			if(isRefreshNeeded(reloadedItem, resourceHandler)) {
				ApplicationAction refreshAction = ActionFactory.getAction(ActionFactory.COMMON_ACTION_TYPES.REFRESH_ENTRY_ACTION, resourceHandler.toString());
				refreshAction.putValue(ApplicationAction.NON_THREADED_ACTION_KEY, Boolean.TRUE);
				refreshAction.invokeAction();
				LoggerFactory.getLogger(this).log(Level.INFO, "Changed entry " + ebookPropertyItem.getResourceHandler().getName() + " refreshed.");
			}
		}

		/**
		 * Handle an ebook file that has been deleted.
		 * @param ebookPropertyItem The ebook file to process.
		 */
		private void handleDeletedItem(EbookPropertyItem ebookPropertyItem) {
			EbookPropertyItem reloadedItem = EbookPropertyItemUtils.reloadEbookPropertyItem(ebookPropertyItem);
			if(reloadedItem != null) {
				ActionUtils.removeEbookPropertyItem(ebookPropertyItem);
				LoggerFactory.getLogger(this).log(Level.INFO, "Removed deleted entry " + ebookPropertyItem.getResourceHandler().getName());
			}
		}
		
		private boolean isManagedItem(IResourceHandler resourceHandler) {
			return PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePath().containsBasePathFor(resourceHandler.getResourceString());
		}
		
		private boolean isRefreshNeeded(EbookPropertyItem ebookPropertyItem, IResourceHandler resourceHandler) {
			if (ebookPropertyItem != null && ebookPropertyItem.getTimestamp() > 0l
					&& ebookPropertyItem.getTimestamp() < resourceHandler.getModifiedAt().getTime()) {
				return true;
			}
			return false;
		}
		
		/**
		 * Test if the given temp file is old and moves it to the trash if it is.
		 * @param tmpFile The file to be deleted.
		 */
		private void deleteOldTempFile(IResourceHandler ebook) {
			final List<IResourceHandler> tmpFiles = ResourceHandlerFactory.getExistingUniqueResourceHandler(ebook, "tmp");
			for(IResourceHandler tmpFile : tmpFiles) {
				if (tmpFile.exists()) {
					final Date modifiedAt = tmpFile.getModifiedAt();
					try {
						long dateDiff = DateUtils.dateDiff("d", modifiedAt, new Date(), Calendar.MONDAY, UtilConstants.FIRSTJAN1);
						if(dateDiff > 1) {
							tmpFile.moveToTrash();
							LoggerFactory.getLogger().log(Level.INFO, "Moving old temp file " + tmpFile + " to the trash.");
						}
					} catch (IOException e) {
						LoggerFactory.getLogger().log(Level.INFO, "Failed to delete old temp file " + tmpFile, e);
					} catch (Exception e) {
						LoggerFactory.getLogger().log(Level.INFO, "Failed to identify old temp file " + tmpFile, e);
					}
				}
			}
		}
	}

}
