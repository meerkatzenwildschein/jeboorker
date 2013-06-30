package org.rr.jeborker;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.rr.commons.collection.Pair;
import org.rr.commons.log.LoggerFactory;
import org.rr.commons.mufs.IResourceHandler;
import org.rr.commons.mufs.ResourceHandlerFactory;
import org.rr.commons.utils.ReflectionUtils;
import org.rr.jeborker.db.DefaultDBManager;
import org.rr.jeborker.db.item.EbookPropertyItem;
import org.rr.jeborker.db.item.EbookPropertyItemUtils;
import org.rr.jeborker.gui.MainController;
import org.rr.jeborker.gui.action.ActionUtils;

public class FileWatchService {

	private static WatchService watchService;

	private static final List<Pair<String, WatchKey>> items = Collections.synchronizedList(new ArrayList<Pair<String, WatchKey>>());

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
	 * Adds the given folder to the watched ones.  
	 */
	public static void addWatchPath(final String path) {
		try {
			File pathFile = new File(path);
			if(!isAlreadyWatched(path) && pathFile.isDirectory()) {
				WatchKey watchKey = Paths.get(path).register(watchService, new Kind<?>[] { ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE });
				items.add(new Pair<String, WatchKey>(path, watchKey));
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(FileWatchService.class).log(Level.WARNING, "Failed to add path " + path + "to file watch service", e);
		}
	}
	
	/**
	 * Tells if the given path is already under watch. 
	 */
	private static boolean isAlreadyWatched(final String path) {
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).getE().equals(path)) {
				return true;
			}
		}		
		return false;
	}
	
	private static class WatchFolderRunnable implements Runnable {

		@Override
		public void run() {
	        while (true) {
	        	try {
	        		WatchKey watchKey = watchService.take();
	        		
	        		List<EbookPropertyItem> changedEbooks = new ArrayList<EbookPropertyItem>();
	        		List<IResourceHandler> addedResources = new ArrayList<IResourceHandler>();
		            for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
		            	for(int i = 0; i < items.size() && !FileRefreshBackground.isDisabled(); i++) {
		            		Pair<String, WatchKey> pair = items.get(i);
		            		if(pair.getF() == watchKey) {
		            			String folder = pair.getE();
		            			String file = watchEvent.context().toString();
		            			IResourceHandler resourceHandler = ResourceHandlerFactory.getResourceHandler(folder + File.separatorChar + file);
		            			List<EbookPropertyItem> ebookPropertyItemByResource = EbookPropertyItemUtils.getEbookPropertyItemByResource(resourceHandler);
		            			if(ebookPropertyItemByResource.isEmpty() && watchEvent.kind() == ENTRY_CREATE) {
		            				addedResources.add(resourceHandler);
		            			} else if(watchEvent.kind() == ENTRY_DELETE || watchEvent.kind() == ENTRY_MODIFY) {
		            				changedEbooks.addAll(ebookPropertyItemByResource);
		            			}
		            		}
		            	}			            	
		            }
		            watchKey.reset();
		            
					//seems the file may be not ready to read, wait...
					ReflectionUtils.sleepSilent(500);
					
		            transferDeleteAndRefresh(changedEbooks);
		            transferNewEbookFiles(addedResources);
	        	} catch(Exception e) {
	        		LoggerFactory.getLogger(FileWatchService.class).log(Level.WARNING, "WatchFolderRunnable", e);	        		
	        	}
	        }
		}
		
		private void transferNewEbookFiles(final List<IResourceHandler> addedResources) {
			for (IResourceHandler resource : addedResources) {
				if(EbookPropertyItemUtils.getEbookPropertyItemByResource(resource).isEmpty()) {
					IResourceHandler basePathForFile = JeboorkerPreferences.getBasePath().getBasePathForFile(resource);
					if( basePathForFile != null && resource.exists() && ActionUtils.isSupportedEbookFormat(resource) ) {
						final EbookPropertyItem item = EbookPropertyItemUtils.createEbookPropertyItem(resource, basePathForFile);
						DefaultDBManager.setDefaultDBThreadInstance();
						DefaultDBManager.getInstance().storeObject(item);		
						ActionUtils.addEbookPropertyItem(item);
						MainController.getController().refreshFileSystemTreeEntry(basePathForFile);
System.out.println("add " + resource);						
					}
				}
			}
		}
		
		private void transferDeleteAndRefresh(List<EbookPropertyItem> ebooks) {
			if(!ebooks.isEmpty()) {
System.out.println("refresh " + ebooks + " " + FileRefreshBackground.isDisabled());				
				FileRefreshBackground.getInstance().addEbooks(ebooks);
			}
		}
		
	}
	
}
