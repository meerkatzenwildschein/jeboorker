package org.rr.jeborker.app;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.Jeboorker;
import org.rr.jeborker.app.preferences.PreferenceStoreFactory;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.action.ActionUtils;

public class FileWatchService {

	private static WatchService watchService;

	private static final HashMap<String, WatchKey> items = new HashMap<String, WatchKey>();

	static {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			Jeboorker.APPLICATION_THREAD_POOL.submit(new WatchFolderRunnable());
		} catch (IOException e) {
			LoggerFactory.getLogger(FileWatchService.class).log(Level.WARNING, "Failed to add file watch service", e);
		}
	}

	private FileWatchService() {
	}

	/**
	 * Adds the given folders to the watched ones.
	 */
	public static void addWatchPath(String path) {
		addWatchPath(Collections.singletonList(path));
	}

	/**
	 * Removes the given path from the watch
	 * @param path The path to be removed from watch.
	 */
	public static void removeWatchPath(String path) {
		path = new File(path).getAbsolutePath();
		WatchKey watchKey = items.remove(path);
		if(watchKey != null) {
			watchKey.cancel();
			LoggerFactory.getLogger(FileWatchService.class).log(Level.INFO, "Removing " + path + " from watch service.");
		}
	}

	/**
	 * Adds the given folders to the watched ones.
	 */
	public static void addWatchPath(final Collection<String> p) {
		LoggerFactory.getLogger(FileWatchService.class).log(Level.INFO, "Adding " + p.size() + " folders to watch service.");
		for(String path : p) {
			try {
				path = new File(path).getAbsolutePath();
				File pathFile = new File(path);
				if(!isAlreadyWatched(path) && pathFile.isDirectory()) {
					WatchKey watchKey = Paths.get(path).register(watchService, new Kind<?>[] { ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE });
					items.put(path, watchKey);
					LoggerFactory.getLogger(FileWatchService.class).log(Level.INFO, "Added " + path + " to watch service.");
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(FileWatchService.class).log(Level.WARNING, "Failed to add path " + path + " to file watch service. Stopping to add watches.", e);
				break;
			}
		}
	}

	/**
	 * Shutdown the watch service. No file change is detected after shutting down the service.
	 */
	public static void shutdownWatchService() {
		for(WatchKey key : items.values()) {
			key.cancel();
		}
		items.clear();
	}

	/**
	 * Tells if the given path is already under watch.
	 */
	private static boolean isAlreadyWatched(final String path) {
		return items.containsKey(path);
	}

	private static class WatchFolderRunnable implements Runnable {

		@Override
		public void run() {
	        while (true) {
	        	try {
	        		WatchKey watchKey = watchService.take();

	        		final List<EbookPropertyItem> changedEbooks = new ArrayList<EbookPropertyItem>();
	        		final List<IResourceHandler> addedResources = new ArrayList<IResourceHandler>();
		            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
		            	if(!FileRefreshBackground.isDisabled()) {
			            	final Path fullPath = ((Path)watchKey.watchable()).resolve((Path)watchEvent.context());
			            	final IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(fullPath.toFile());
	            			final List<EbookPropertyItem> ebookPropertyItemByResource = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceHandler);

	            			if(ebookPropertyItemByResource.isEmpty() && watchEvent.kind() == ENTRY_CREATE) {
	            				addedResources.add(resourceHandler);
	            			} else if(watchEvent.kind() == ENTRY_DELETE || watchEvent.kind() == ENTRY_MODIFY) {
	            				changedEbooks.addAll(ebookPropertyItemByResource);
	            			}
		            	}
		            }
		            watchKey.reset();

					FileRefreshBackground.runWithDisabledRefresh(new Runnable() {

						@Override
						public void run() {
							//seems the file may be not ready to read, wait...
							ReflectionUtils.sleepSilent(500);

				            transferDeleteAndRefresh(changedEbooks);
				            transferNewEbookFiles(addedResources);
						}
					});
	        	} catch(Exception e) {
	        		LoggerFactory.getLogger(FileWatchService.class).log(Level.WARNING, "WatchFolderRunnable", e);
	        	}
	        }
		}

		private void transferNewEbookFiles(final List<IResourceHandler> addedResources) {
			for (IResourceHandler resource : addedResources) {
				if(EbookPropertyItemUtils.getEbookPropertyItemByResource(resource).isEmpty()) {
					IResourceHandler basePathForFile = PreferenceStoreFactory.getPreferenceStore(PreferenceStoreFactory.DB_STORE).getBasePath().getBasePathForFile(resource);
					if( basePathForFile != null && resource.exists() && ActionUtils.isSupportedEbookFormat(resource, true) ) {
						final EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(resource, basePathForFile);
						DefaultDBManager.getInstance().storeObject(item);
						ActionUtils.addEbookPropertyItem(item);
						MainController.getController().getMainTreeHandler().refreshFileSystemTreeEntry(basePathForFile);
						LoggerFactory.getLogger().log(Level.INFO, "add " + resource);
					}
				}
			}
		}

		private void transferDeleteAndRefresh(final List<EbookPropertyItem> ebooks) {
			final MainController controller = MainController.getController();
			for(final EbookPropertyItem item : ebooks) {
				//do not process items younger than 10 seconds since jeboorker has touched them.
				if(isTimeLeft(item, 10000)) {
					final IResourceHandler resourceHandler = item.getResourceHandler();
					if(!resourceHandler.exists()) {
						//remove
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								boolean removed = controller.removeEbookPropertyItem(item);
								controller.getMainTreeHandler().refreshFileSystemTreeEntry(item.getResourceHandler());
								if(!removed) {
									DefaultDBManager.getInstance().deleteObject(item);
								}
								LoggerFactory.getLogger().log(Level.INFO, "remove " + resourceHandler + " " + removed);
							}
						});
					} else {
						//refresh
						EbookPropertyItemUtils.refreshEbookPropertyItem(item, resourceHandler, true);
						DefaultDBManager.getInstance().updateObject(item);
						LoggerFactory.getLogger().log(Level.INFO, "refresh " + resourceHandler);
					}

					FileRefreshBackground.getInstance().addEbooks(ebooks);
				}
			}
		}

		private boolean isTimeLeft(EbookPropertyItem item, long time) {
			long timestamp = item.getTimestamp();
			if(System.currentTimeMillis() - timestamp < time) {
				return false;
			}
			return true;
		}

	}

}
